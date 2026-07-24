package org.philimone.hds.forms.main;

import org.philimone.hds.forms.listeners.ExternalMethodCallListener;
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

import mz.betainteractive.utilities.DateUtil;
import mz.betainteractive.utilities.StringUtil;

import java.io.Serializable;
import java.util.ArrayList;
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
            this.headerGroupModel = createGroupModel(headerGroup, null, null, null);
            this.headerGroupModel.setHeader(true);
            this.headerGroupModel.setHidden(true);
        }

        // 2. Handle Body Columns
        for (ColumnGroup group : form.getColumns()) {
            if (group.isHeader()) continue;

            if (group instanceof ColumnRepeatGroup) {
                expandRepeatGroup((ColumnRepeatGroup) group);
            } else {
                createGroupModel(group, null, null, null);
            }
        }

        relinkAll();
        evaluateAll();
    }

    private void expandRepeatGroup(ColumnRepeatGroup repeatGroup) {
        RepeatCountType repeatCountType = repeatGroup.getRepeatCountType();
        Integer repeatSize = repeatGroup.getRepeatSize(preloadedValues);

        if (repeatCountType == RepeatCountType.VARIABLE) {
            ColumnRepeatModel anchor = new ColumnRepeatModel(repeatGroup, repeatCountType);
            anchor.setHidden(true);
            anchor.setDisplayable(false);
            groupModels.add(anchor);

            //DONT INITIALIZE ANYTHING WAIT FOR THE repeat_count expression calculation
            /*int targetSize = 0;
            if (targetSize > 0) {
                syncRepeatInstances(anchor, targetSize);
            }*/

            return;
        } else if (repeatCountType == RepeatCountType.EMPTY) {
            ColumnRepeatModel anchor = new ColumnRepeatModel(repeatGroup, repeatCountType);
            anchor.setHidden(false);
            anchor.setDisplayable(true);
            groupModels.add(anchor);

            // Sync preloaded/saved data or initialize with 1 instance for EMPTY type
            int targetCount = (repeatSize==null || repeatSize==0) ? 1 : repeatSize;

            //add anchor at the end of the repeat instances to ask for more instances
            syncRepeatInstances(anchor, targetCount);

            return;
        }

        // Handles EXTERNAL_LOADER and CONSTANT_VALUE
        for (int index = 0; index < repeatSize; index++) {
            for (ColumnGroup innerGroup : repeatGroup.getColumnsGroups()) {
                ColumnGroup clonedInner = innerGroup.clone();
                createGroupModel(clonedInner, repeatGroup, index, repeatSize);
            }
        }
    }

    private void syncRepeatInstances(ColumnRepeatModel anchor, int targetCount) {
        int currentCount = anchor.getCurrentInstanceCount();
        if (targetCount == currentCount) return;

        boolean structureChanged = false;
        ColumnRepeatGroup repeatGroup = anchor.getRepeatDefinition();
        //Log.d("repeat group", repeatGroup.getGroupName()+", currCount="+currentCount+", targetCount="+targetCount);

        if (targetCount > currentCount) {
            // Expand
            int anchorIndex = groupModels.indexOf(anchor);

            if (anchor.getRepeatCountType() == RepeatCountType.VARIABLE) {
                int insertIndex = anchorIndex + 1;
                insertIndex += anchor.getInstanceModels().size();

                for (int i = currentCount; i < targetCount; i++) {
                    for (ColumnGroup innerGroup : repeatGroup.getColumnsGroups()) {
                        ColumnGroup clonedInner = innerGroup.clone();
                        ColumnGroupModel newModel = initializeGroupModel(clonedInner, repeatGroup, i, targetCount);

                        groupModels.add(insertIndex++, newModel);
                        anchor.addInstanceModel(newModel);
                        structureChanged = true;
                    }
                }
            } else if (anchor.getRepeatCountType() == RepeatCountType.EMPTY){
                //the anchor is the last item so we insert before the anchor
                int insertIndex = anchorIndex;

                for (int i = currentCount; i < targetCount; i++) {
                    for (ColumnGroup innerGroup : repeatGroup.getColumnsGroups()) {
                        ColumnGroup clonedInner = innerGroup.clone();
                        ColumnGroupModel newModel = initializeGroupModel(clonedInner, repeatGroup, i, targetCount);

                        groupModels.add(insertIndex++, newModel);
                        anchor.addInstanceModel(newModel);
                        structureChanged = true;
                    }
                }
            }
        } else {
            // Shrink
            int groupsPerInstance = repeatGroup.getColumnsGroups().size();
            int instancesToRemove = currentCount - targetCount;
            
            for (int i = 0; i < instancesToRemove; i++) {
                for (int j = 0; j < groupsPerInstance; j++) {
                    ColumnGroupModel modelToRemove = anchor.getInstanceModels().remove(anchor.getInstanceModels().size() - 1);
                    groupModels.remove(modelToRemove);
                    structureChanged = true;
                }
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

    private ColumnGroupModel createGroupModel(ColumnGroup group, ColumnRepeatGroup repeatParent, Integer repeatIndex, Integer repeatSize) {
        ColumnGroupModel groupModel = initializeGroupModel(group, repeatParent, repeatIndex, repeatSize);
        groupModels.add(groupModel);
        return groupModel;
    }

    private ColumnGroupModel initializeGroupModel(ColumnGroup group, ColumnRepeatGroup repeatParent, Integer repeatIndex, Integer repeatSize) {
        ColumnGroupModel groupModel;
        
        if (repeatParent != null) {
            //Log.d("creating inner group", repeatParent.getGroupName()+", index="+repeatIndex+", size="+repeatSize);
            groupModel = new ColumnGroupModel(repeatParent, group, repeatIndex, repeatSize);
        } else {
            groupModel = new ColumnGroupModel(group);
        }

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

    public void evaluateGroup(ColumnGroupModel groupModel) {
        for (ColumnModel cm : groupModel.getColumnModels()) {
            evaluateColumn(cm);
        }
        updateGroupVisibility(groupModel);
    }

    public void addRepeatInstance(ColumnRepeatModel anchor) {
        syncRepeatInstances(anchor, anchor.getCurrentInstanceCount() + 1);
    }

    public void removeLastRepeatInstance(ColumnRepeatModel anchor) {
        if (anchor.getCurrentInstanceCount() > 1) {
            syncRepeatInstances(anchor, anchor.getCurrentInstanceCount() - 1);
        }
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
            columnModel.setDisplayable(result == null || "true".equals(result.toString()));
        } else {
            columnModel.setDisplayable(true);
        }
    }

    private void evaluateColumnReadOnlyCondition(ColumnModel columnModel) {
        Column column = columnModel.getColumn();
        String readOnlyCondition = column.getReadOnlyCondition();
        if (!StringUtil.isBlank(readOnlyCondition)) {
            Object result = evaluator.evaluate(readOnlyCondition, columnModel);
            columnModel.setReadOnly("true".equals(result != null ? result.toString() : ""));
        }
    }

    private void evaluateColumnRequiredCondition(ColumnModel columnModel) {
        Column column = columnModel.getColumn();
        String requiredCondition = column.getRequiredCondition();
        if (!StringUtil.isBlank(requiredCondition)) {
            Object result = evaluator.evaluate(requiredCondition, columnModel);
            columnModel.setRequired("true".equals(result != null ? result.toString() : ""));
        }
    }

    private void evaluateColumnValidation(ColumnModel columnModel) {
        Column column = columnModel.getColumn();
        String validation = column.getValidation();
        if (!StringUtil.isBlank(validation)) {
            Object result = evaluator.evaluate(validation, columnModel);
            boolean isValid = result == null || "true".equals(result.toString());
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
