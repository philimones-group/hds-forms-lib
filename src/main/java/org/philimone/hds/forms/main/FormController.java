package org.philimone.hds.forms.main;

import android.util.Log;

import org.philimone.hds.forms.model.FormExpressionEvaluator;
import org.philimone.hds.forms.model.listeners.ExternalMethodCallListener;
import org.philimone.hds.forms.model.Column;
import org.philimone.hds.forms.model.ColumnGroup;
import org.philimone.hds.forms.model.ColumnGroupModel;
import org.philimone.hds.forms.model.ColumnModel;
import org.philimone.hds.forms.model.ColumnRepeatGroup;
import org.philimone.hds.forms.model.ColumnRepeatModel;
import org.philimone.hds.forms.model.HForm;
import org.philimone.hds.forms.model.PreloadMap;
import org.philimone.hds.forms.model.RepeatObject;
import org.philimone.hds.forms.model.enums.ColumnType;
import org.philimone.hds.forms.model.enums.ColumnValueStatus;
import org.philimone.hds.forms.model.enums.RepeatCountType;
import org.philimone.hds.forms.model.parsers.form.model.FormOptions;

import mz.betainteractive.utilities.DateUtil;
import mz.betainteractive.utilities.StringUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * FormController manages the runtime state of the form.
 * It handles the initialization of ColumnGroupModels and ColumnModels,
 * manages Repeat Group expansion, and will eventually centralize expression evaluation.
 */
public class FormController implements Serializable {

    private HForm form;
    private List<ColumnGroupModel> groupModels;
    private PreloadMap preloadedValues;
    private FormContext formContext;
    private FormExpressionEvaluator evaluator;
    private String instanceUUID;
    private boolean editingFormInstance;

    private ColumnGroupModel headerGroupModel;

    private OnFormStateListener stateListener;

    public FormController(HForm form, boolean editingFormInstance, PreloadMap preloadedValues, FormContext formContext, ExternalMethodCallListener methodCallListener) {
        this.form = form;
        this.preloadedValues = (preloadedValues != null) ? preloadedValues : new PreloadMap();
        this.formContext = formContext;
        this.editingFormInstance = editingFormInstance;
        this.evaluator = new FormExpressionEvaluator(methodCallListener);
        this.groupModels = new ArrayList<>();
        initializeModels();
    }

    public void setStateListener(OnFormStateListener stateListener) {
        this.stateListener = stateListener;
    }

    public ColumnGroupModel getHeaderGroupModel() {
        return headerGroupModel;
    }

    private void initializeModels() {
        groupModels.clear();

        // 1. Handle Header if exists
        if (form.hasHeader()) {
            ColumnGroup headerGroup = form.getHeader();
            this.headerGroupModel = createGroupModel(headerGroup, null, null, null, null, -1);
            this.headerGroupModel.setHeader(true);
            this.headerGroupModel.setHidden(true);
        }

        // 2. Handle Body Columns
        for (ColumnGroup group : form.getColumns()) {
            if (group.isHeader()) continue;

            if (group instanceof ColumnRepeatGroup) {
                expandRepeatGroup((ColumnRepeatGroup) group, null, -1);
            } else {
                createGroupModel(group, null, null, null, null, -1);

            }
        }

        relinkAll();
        evaluateAll();
    }

    /*
     *  if expandInsertIndex is -1 - means that it must do the regular insert at the end of the groupModels
     *  the expandRepeatGroup must return a ExpandRepeatGroupResult - shows what was created (getInsertedGroupsCount)
     */
    private ExpandRepeatGroupResult expandRepeatGroup(ColumnRepeatGroup repeatGroup, ColumnGroupModel parentContext, int expandInsertIndex) {

        ExpandRepeatGroupResult result = new ExpandRepeatGroupResult();
        RepeatCountType repeatCountType = repeatGroup.getRepeatCountType();
        Integer repeatSize = repeatGroup.getRepeatSize(preloadedValues);

        if (repeatCountType == RepeatCountType.VARIABLE) {
            ColumnRepeatModel anchor = new ColumnRepeatModel(repeatGroup, repeatCountType);
            anchor.setParentGroupModel(parentContext);
            anchor.setHidden(true);
            anchor.setDisplayable(false);

            if (expandInsertIndex == -1) {
                groupModels.add(anchor);
            } else {
                groupModels.add(expandInsertIndex, anchor);
            }

            result.createdGroupModels.add(anchor);
            return result; //only add 1 addition

        } else if (repeatCountType == RepeatCountType.EMPTY) {
            ColumnRepeatModel anchor = new ColumnRepeatModel(repeatGroup, repeatCountType);
            anchor.setParentGroupModel(parentContext);
            anchor.setHidden(false);
            anchor.setDisplayable(true);

            if (expandInsertIndex == -1) {
                groupModels.add(anchor);
            } else {
                groupModels.add(expandInsertIndex, anchor);
            }

            // Sync preloaded/saved data or initialize with 1 instance for EMPTY type
            int targetCount = (repeatSize==null || repeatSize==0) ? 1 : repeatSize;

            //add anchor at the end of the repeat instances to ask for more instances
            SyncRepeatInstanceResult syncRepeatResult = syncRepeatInstances(anchor, targetCount);
            int totalInsertedGroups = syncRepeatResult.getInsertedGroupsCount();

            result.createdGroupModels.add(anchor);
            result.createdGroupModels.addAll(syncRepeatResult.createdGroupModels);
            return result; //add 1 anchor inserted + totalInsertedGroups
        }

        // Handles EXTERNAL_LOADER and CONSTANT_VALUE
        for (int index = 0; index < repeatSize; index++) {
            ColumnGroupModel firstModelInInstance = null;

            for (ColumnGroup innerGroup : repeatGroup.getColumnsGroups()) {
                ColumnGroup clonedInner = innerGroup.clone();

                if (clonedInner instanceof ColumnRepeatGroup) {
                    ExpandRepeatGroupResult expandRepeatResult = expandRepeatGroup((ColumnRepeatGroup) clonedInner, firstModelInInstance, expandInsertIndex); //but this is a nested repeat group
                    expandInsertIndex = (expandInsertIndex != -1) ? (expandInsertIndex + expandRepeatResult.getInsertedGroupsCount()) : -1;

                    result.createdGroupModels.addAll(expandRepeatResult.createdGroupModels);
                } else {
                    ColumnGroupModel model = createGroupModel(clonedInner, parentContext, repeatGroup, index, repeatSize, expandInsertIndex);
                    if (firstModelInInstance == null) firstModelInInstance = model;
                    expandInsertIndex = (expandInsertIndex != -1) ? expandInsertIndex+1 : -1;
                    result.createdGroupModels.add(model);
                }
            }
        }

        return result;
    }

    /**
     * This method expands or shrinks a repeat group inner Column Groups
     * @param anchor
     * @param targetCount
     * @return the number of inserted/removed groups models
     */
    private SyncRepeatInstanceResult syncRepeatInstances(ColumnRepeatModel anchor, int targetCount) {
        SyncRepeatInstanceResult result = new SyncRepeatInstanceResult();
        int currentCount = anchor.getCurrentInstanceCount();
        if (targetCount == currentCount) return result; //nothing inserted/removed

        boolean structureChanged = false;
        ColumnRepeatGroup repeatGroup = anchor.getRepeatDefinition();
        //Log.d("repeat group", repeatGroup.getGroupName()+", currCount="+currentCount+", targetCount="+targetCount);

        ColumnGroupModel parentContext = anchor.getParentGroupModel();

        if (targetCount > currentCount) {
            // Expand
            int anchorIndex = groupModels.indexOf(anchor);

            if (anchor.getRepeatCountType() == RepeatCountType.VARIABLE) {
                int insertIndex = anchorIndex + 1;
                insertIndex += anchor.getInstanceModels().size(); //adds to the end

                for (int i = currentCount; i < targetCount; i++) {
                    ColumnGroupModel firstModelInInstance = null;

                    for (ColumnGroup innerGroup : repeatGroup.getColumnsGroups()) {
                        ColumnGroup clonedInner = innerGroup.clone();

                        if (clonedInner instanceof ColumnRepeatGroup) {
                            //if the inner group is a ColumnRepeatGroup we must expand it too
                            //but should be added at insertIndex - but after expand we must sync a new insertIndex
                            //the expandRepeatGroup must return the new the number of insertedColumnGroups
                            //inside expandRepeatGroups the groupModels already added
                            ExpandRepeatGroupResult expandResult = expandRepeatGroup((ColumnRepeatGroup) innerGroup, firstModelInInstance, insertIndex);
                            int totalInsertedGroups = expandResult.getInsertedGroupsCount();
                            insertIndex += totalInsertedGroups;

                            for (ColumnGroupModel createdModel : expandResult.createdGroupModels) {
                                anchor.addInstanceModel(createdModel);
                                result.createdGroupModels.add(createdModel);
                            }
                            result.expanded = true;
                            continue;
                        }

                        ColumnGroupModel newModel = initializeGroupModel(clonedInner, parentContext, repeatGroup, i, targetCount);
                        if (firstModelInInstance == null) firstModelInInstance = newModel;

                        //adding new groupModel to the end of existing models
                        groupModels.add(insertIndex++, newModel);
                        anchor.addInstanceModel(newModel);
                        structureChanged = true;

                        result.createdGroupModels.add(newModel);
                    }
                }
            } else if (anchor.getRepeatCountType() == RepeatCountType.EMPTY){
                //the anchor is the last item so we insert before the anchor
                int insertIndex = anchorIndex;

                for (int i = currentCount; i < targetCount; i++) {
                    ColumnGroupModel firstModelInInstance = null;

                    for (ColumnGroup innerGroup : repeatGroup.getColumnsGroups()) {
                        ColumnGroup clonedInner = innerGroup.clone();

                        if (clonedInner instanceof ColumnRepeatGroup) {
                            //if the inner group is a ColumnRepeatGroup we must expand it too
                            //but should be added at insertIndex - but after expand we must sync a new insertIndex
                            //the expandRepeatGroup must return the new the number of insertedColumnGroups
                            //inside expandRepeatGroups the groupModels already added
                            ExpandRepeatGroupResult expandResult = expandRepeatGroup((ColumnRepeatGroup) innerGroup, firstModelInInstance, insertIndex);
                            int totalInsertedGroups = expandResult.getInsertedGroupsCount();
                            insertIndex += totalInsertedGroups;

                            for (ColumnGroupModel createdModel : expandResult.createdGroupModels) {
                                anchor.addInstanceModel(createdModel);
                                result.createdGroupModels.add(createdModel);
                            }
                            result.expanded = true;
                            continue;
                        }

                        ColumnGroupModel newModel = initializeGroupModel(clonedInner, parentContext, repeatGroup, i, targetCount);
                        if (firstModelInInstance == null) firstModelInInstance = newModel;

                        groupModels.add(insertIndex++, newModel);
                        anchor.addInstanceModel(newModel);
                        structureChanged = true;

                        result.createdGroupModels.add(newModel);
                    }
                }
            }
        } else {
            // Shrink
            int instancesToRemove = currentCount - targetCount;
            for (int i = 0; i < instancesToRemove; i++) {
                int lastInstanceIdx = anchor.getCurrentInstanceCount() - 1;

                // Use an iterator to safely remove all models belonging to this specific instance
                Iterator<ColumnGroupModel> it = anchor.getInstanceModels().iterator();
                while (it.hasNext()) {
                    ColumnGroupModel m = it.next();
                    if (m.getRepeatIndex() != null && m.getRepeatIndex() == lastInstanceIdx) {
                        if (m instanceof ColumnRepeatModel) {
                            removeModels((ColumnRepeatModel) m); // Clean up the nested tree
                        }
                        groupModels.remove(m); // Remove from UI
                        it.remove(); // Remove from parent tracking
                        structureChanged = true;
                    }
                }
                anchor.setCurrentInstanceCount(lastInstanceIdx);
            }
        }

        if (structureChanged) {
            anchor.setCurrentInstanceCount(targetCount);
            for (ColumnGroupModel gm : anchor.getInstanceModels()) {
                gm.setRepeatSize(targetCount);
            }
            
            relinkAll();
            
            if (stateListener != null) {
                stateListener.onFormStructureChanged();
            }
        }

        return result;
    }

    private void removeModels(ColumnRepeatModel anchor){
        while (!anchor.getInstanceModels().isEmpty()) {
            ColumnGroupModel m = anchor.getInstanceModels().remove(anchor.getInstanceModels().size() - 1);
            if (m instanceof ColumnRepeatModel) {
                removeModels((ColumnRepeatModel) m);
            }
            groupModels.remove(m);
        }
        anchor.clearInstanceModels();
    }

    private void relinkAll() {
        ColumnGroupModel lastGM = null;
        ColumnModel lastCM = null;

        for (ColumnGroupModel gm : groupModels) {
            gm.setPreviousGroupModel(lastGM);
            lastGM = gm;

            for (ColumnModel cm : gm.getColumnModels()) {
                cm.setPreviousModel(lastCM);
                lastCM = cm;
            }
        }
    }

    private ColumnGroupModel createGroupModel(ColumnGroup group, ColumnGroupModel parentContext, ColumnRepeatGroup repeatParent, Integer repeatIndex, Integer repeatSize, int insertIndex) {
        ColumnGroupModel groupModel = initializeGroupModel(group, parentContext, repeatParent, repeatIndex, repeatSize);

        if (insertIndex == -1) {
            groupModels.add(groupModel);
        } else {
            groupModels.add(insertIndex, groupModel);
        }

        return groupModel;
    }

    private ColumnGroupModel initializeGroupModel(ColumnGroup group, ColumnGroupModel parentContext, ColumnRepeatGroup repeatParent, Integer repeatIndex, Integer repeatSize) {
        ColumnGroupModel groupModel;
        
        if (repeatParent != null) {
            //Log.d("creating inner group", repeatParent.getGroupName()+", index="+repeatIndex+", size="+repeatSize);
            groupModel = new ColumnGroupModel(repeatParent, group, repeatIndex, repeatSize);
        } else {
            groupModel = new ColumnGroupModel(group);
        }

        groupModel.setParentGroupModel(parentContext);
        groupModel.setSupportedCalendar(formContext.supportedCalendar);

        for (Column column : group.getColumns()) {
            ColumnModel columnModel = new ColumnModel(column, groupModel);
            loadInitialValue(columnModel, repeatParent, repeatIndex);
            groupModel.addColumnModel(columnModel);
        }

        return groupModel;
    }

    private void loadInitialValue(ColumnModel columnModel, ColumnRepeatGroup repeatParent, Integer repeatIndex) {
        Column column = columnModel.getColumn();
        String columnName = column.getName();
        ColumnType type = column.getType();

        // 1. Handle standard preloaded values
        if (preloadedValues.containsKey(columnName)) {
            String value = preloadedValues.getStringValue(columnName);
            columnModel.setValue(value, ColumnValueStatus.FROM_XML);
            if (type == ColumnType.INSTANCE_UUID) this.instanceUUID = value;
        }

        // 2. Handle Repeat Group values from PreloadMap
        if (repeatParent != null && repeatIndex != null) {
            if (preloadedValues.containsKey(repeatParent.getName())) {
                RepeatObject repeatObject = preloadedValues.getRepeatObject(repeatParent.getName());
                String value = repeatObject.get(repeatIndex, columnName);
                if (value != null) columnModel.setValue(value, ColumnValueStatus.FROM_XML);
            }
        }

        // 3. Handle GPS preloaded values
        if (type == ColumnType.GPS) {
            loadGpsValue(columnModel);
        }

        // 4. Handle special system types
        if (columnModel.getValue() == null || columnModel.getValue().isEmpty()) {
            if (type == ColumnType.INSTANCE_UUID) {
                this.instanceUUID = UUID.randomUUID().toString().replace("-", "");
                columnModel.setValue(this.instanceUUID, ColumnValueStatus.FROM_CALCULATION);
            }
            if (type == ColumnType.DEVICE_ID) {
                columnModel.setValue(formContext.deviceId, ColumnValueStatus.FROM_CALCULATION);
            }
            if (type == ColumnType.COLLECTED_BY) {
                columnModel.setValue(formContext.username, ColumnValueStatus.FROM_CALCULATION);
            }
            if (type == ColumnType.START_TIMESTAMP) {
                columnModel.setValue(formContext.startTimestamp, ColumnValueStatus.FROM_CALCULATION);
            }
            if (type == ColumnType.TIMESTAMP) {
                columnModel.setValue(formContext.startTimestamp, ColumnValueStatus.FROM_CALCULATION);
            }
        }

        if (type == ColumnType.END_TIMESTAMP && (columnModel.getValue() == null || columnModel.getValue().isEmpty())) {
            columnModel.setValue(formContext.endTimestamp, ColumnValueStatus.FROM_CALCULATION);
        }
    }

    private void loadGpsValue(ColumnModel columnModel) {
        String[] gps_cols = new String[]{ "Lat", "Lon", "Alt", "Acc" };
        String baseName = columnModel.getName();
        Map<String, Double> gpsValues = new java.util.LinkedHashMap<>();

        for (String ext : gps_cols) {
            String colName = baseName + ext;
            if (preloadedValues.containsKey(colName)) {
                try {
                    String val = preloadedValues.getStringValue(colName);
                    gpsValues.put(colName, Double.parseDouble(val));
                } catch (Exception e) {}
            }
        }
        
        if (!gpsValues.isEmpty()) {
            columnModel.setGpsValues(gpsValues);
        }
    }

    public FormContext getFormContext() {
        return formContext;
    }

    public String getInstanceUUID() {
        return instanceUUID;
    }

    public List<ColumnGroupModel> getGroupModels() {
        return groupModels;
    }

    public List<ColumnGroupModel> getVisibleGroupModels() {
        List<ColumnGroupModel> visible = new ArrayList<>();
        for (ColumnGroupModel gm : groupModels) {
            if (!gm.isHidden() && gm.isDisplayable()) {
                visible.add(gm);
            }
        }
        return visible;
    }

    public void finalizeForm(String endTimestamp) {
        this.formContext.endTimestamp = endTimestamp;

        boolean mediaCollected = false;
        outer: for (ColumnGroupModel gm : groupModels) {
            for (ColumnModel cm : gm.getColumnModels()) {
                if (cm.getColumn().isMediaColumn() && !cm.isValueBlank()) {
                    mediaCollected = true;
                    break outer;
                }
            }
        }
        String mediaCollectedValue = Boolean.toString(mediaCollected);

        for (ColumnGroupModel gm : groupModels) {
            for (ColumnModel cm : gm.getColumnModels()) {
                ColumnType type = cm.getType();
                if (type == ColumnType.END_TIMESTAMP) {
                    cm.setValue(endTimestamp, ColumnValueStatus.FROM_CALCULATION);
                }
                if (type == ColumnType.MEDIA_COLLECTED) {
                    cm.setValue(mediaCollectedValue, ColumnValueStatus.FROM_CALCULATION);
                }
            }
        }
    }

    public void onModelValueChanged(ColumnModel columnModel) {
        onModelValueChanged(columnModel.getParentGroupModel());
    }

    public void onModelValueChanged(ColumnGroupModel groupModel) {
        int startIndex = groupModels.indexOf(groupModel);
        if (startIndex < 0) startIndex = 0;

        for (int i = startIndex; i < groupModels.size(); i++) {
            ColumnGroupModel gm = groupModels.get(i);
            if (gm instanceof ColumnRepeatModel) {
                evaluateRepeatModel((ColumnRepeatModel) gm);
            } else {
                evaluateGroup(gm);
            }
        }

        if (stateListener != null) {
            stateListener.onFormStructureChanged();
        }
    }

    public void addRepeatInstance(ColumnRepeatModel anchor) {
        syncRepeatInstances(anchor, anchor.getCurrentInstanceCount() + 1);
    }

    public void removeLastRepeatInstance(ColumnRepeatModel anchor) {
        if (anchor.getCurrentInstanceCount() > 1) {
            syncRepeatInstances(anchor, anchor.getCurrentInstanceCount() - 1);
        }
    }

    //region Evaluation Methods
    public void evaluateAll() {
        for (int i = 0; i < groupModels.size(); i++) {
            ColumnGroupModel gm = groupModels.get(i);
            if (gm instanceof ColumnRepeatModel) {
                evaluateRepeatModel((ColumnRepeatModel) gm);
            } else {
                evaluateGroup(gm);
            }
        }
    }

    public void evaluateGroup(ColumnGroupModel groupModel) {
        for (ColumnModel cm : groupModel.getColumnModels()) {
            evaluateColumn(cm);
        }
        updateGroupVisibility(groupModel);
    }

    private void evaluateRepeatModel(ColumnRepeatModel anchor) {
        if (anchor.getRepeatCountType() == RepeatCountType.VARIABLE) {
            String expression = anchor.getRepeatDefinition().getRepeatCount();
            
            // Context is the last column before the anchor
            ColumnGroupModel prevGroup = anchor.getPreviousGroupModel();
            while (prevGroup != null && (prevGroup.getColumnModels().isEmpty() || prevGroup instanceof ColumnRepeatModel)) {
                prevGroup = prevGroup.getPreviousGroupModel();
            }
            
            ColumnModel context = (prevGroup != null && !prevGroup.getColumnModels().isEmpty()) ? 
                                  prevGroup.getColumnModels().get(prevGroup.getColumnModels().size() - 1) : 
                                  null;
            
            Object result = evaluator.evaluate(expression, context); //The expression ${counts} + 1 - should not run

            //Log.d("tag repeat "+anchor.getName(), "expression: "+result+"");
            if (result instanceof Number) {
                syncRepeatInstances(anchor, ((Number) result).intValue());
            } else if (result instanceof String) {
                try {
                    syncRepeatInstances(anchor, Integer.parseInt((String) result));
                } catch (Exception ignored) {}
            }
        } else if (anchor.getRepeatCountType() == RepeatCountType.EMPTY) {

        }
    }

    private void updateGroupVisibility(ColumnGroupModel groupModel) {

        if (groupModel instanceof ColumnRepeatModel) return;

        boolean anyVisible = false;
        for (ColumnModel cm : groupModel.getColumnModels()) {
            if (cm.isDisplayable() && !cm.getColumn().isHidden()) {
                anyVisible = true;
                break;
            }
        }
        groupModel.setDisplayable(anyVisible);
    }

    public void evaluateColumn(ColumnModel columnModel) {
        evaluateColumnCalculation(columnModel);
        evaluateColumnDisplayCondition(columnModel);
        evaluateColumnReadOnlyCondition(columnModel);
        evaluateColumnRequiredCondition(columnModel);
        evaluateColumnValidation(columnModel);
        evaluateOptionsDisplayCondition(columnModel);
        evaluateOptionsReadOnlyCondition(columnModel);
    }

    private void evaluateOptionsDisplayCondition(ColumnModel columnModel) {
        Column column = columnModel.getColumn();
        if (column.isOptionsConditionallyDisplayable()) {
            for (FormOptions.OptionValue optionValue : column.getTypeOptions().values()) {
                if (!StringUtil.isBlank(optionValue.displayCondition)) {
                    Object result = evaluator.evaluate(optionValue.displayCondition, columnModel);
                    optionValue.displayable = StringUtil.getBooleanValue(result+"");
                }
            }
        }
    }

    private void evaluateOptionsReadOnlyCondition(ColumnModel columnModel) {
        Column column = columnModel.getColumn();
        if (column.isOptionsConditionallyReadOnly()) {
            for (FormOptions.OptionValue optionValue : column.getTypeOptions().values()) {
                if (!StringUtil.isBlank(optionValue.readonlyCondition)) {
                    Object result = evaluator.evaluate(optionValue.readonlyCondition, columnModel);
                    optionValue.readonly = StringUtil.getBooleanValue(result+"");
                }
            }
        }
    }

    private void evaluateColumnCalculation(ColumnModel columnModel) {
        Column column = columnModel.getColumn();
        String calculation = column.getCalculation();
        if (!StringUtil.isBlank(calculation)) {
            boolean canCalculate = columnModel.isReadOnly() || columnModel.getValueStatus() != ColumnValueStatus.FROM_USER_INPUT;
            if (canCalculate) {
                Object result = evaluator.evaluate(calculation, columnModel);
                columnModel.setValue(result != null ? result.toString() : "", ColumnValueStatus.FROM_CALCULATION);
            }
        }
    }

    private void evaluateColumnDisplayCondition(ColumnModel columnModel) {
        Column column = columnModel.getColumn();
        String displayCondition = column.getDisplayCondition();
        if (!StringUtil.isBlank(displayCondition)) {
            Object result = evaluator.evaluate(displayCondition, columnModel);
            columnModel.setDisplayable(StringUtil.getBooleanValue(result+""));
        } else {
            columnModel.setDisplayable(true);
        }
    }

    private void evaluateColumnReadOnlyCondition(ColumnModel columnModel) {
        Column column = columnModel.getColumn();
        String readOnlyCondition = column.getReadOnlyCondition();
        if (!StringUtil.isBlank(readOnlyCondition)) {
            Object result = evaluator.evaluate(readOnlyCondition, columnModel);
            columnModel.setReadOnly(StringUtil.getBooleanValue(result+""));
            //Log.d("col "+columnModel.getName(), "result = "+result);
        }
    }

    private void evaluateColumnRequiredCondition(ColumnModel columnModel) {
        Column column = columnModel.getColumn();
        String requiredCondition = column.getRequiredCondition();
        if (!StringUtil.isBlank(requiredCondition)) {
            Object result = evaluator.evaluate(requiredCondition, columnModel);
            columnModel.setRequired(StringUtil.getBooleanValue(result+""));
        }
    }

    private void evaluateColumnValidation(ColumnModel columnModel) {
        Column column = columnModel.getColumn();
        String validation = column.getValidation();
        if (!StringUtil.isBlank(validation)) {
            Object result = evaluator.evaluate(validation, columnModel);
            boolean isValid = StringUtil.getBooleanValue(result+"");
            columnModel.setValid(isValid);
            if (!isValid) {
                columnModel.setResolvedValidationMessage(column.getValidationMessage());
            } else {
                columnModel.setResolvedValidationMessage(null);
            }
        } else {
            columnModel.setValid(true);
            columnModel.setResolvedValidationMessage(null);
        }
    }
    //endregion

    class ExpandRepeatGroupResult {

        public List<ColumnGroupModel> createdGroupModels = new ArrayList<>();

        public int getInsertedGroupsCount() {
            return createdGroupModels.size();
        }
    }

    class SyncRepeatInstanceResult {
        public List<ColumnGroupModel> createdGroupModels = new ArrayList<>();
        public boolean expanded = false;

        public int getInsertedGroupsCount() {
            return createdGroupModels.size();
        }
    }

    public static interface OnFormStateListener {
        void onFormStructureChanged();
    }

    public static class FormContext implements Serializable {
        public DateUtil.SupportedCalendar supportedCalendar;
        public String username;
        public String deviceId;
        public String startTimestamp;
        public String endTimestamp;

        public FormContext(DateUtil.SupportedCalendar calendar, String username, String deviceId, String startTimestamp) {
            this.supportedCalendar = calendar;
            this.username = username;
            this.deviceId = deviceId;
            this.startTimestamp = startTimestamp;
        }
    }
}
