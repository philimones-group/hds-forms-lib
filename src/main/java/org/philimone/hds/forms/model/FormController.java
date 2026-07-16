package org.philimone.hds.forms.model;

import android.util.Log;

import org.philimone.hds.forms.listeners.ExternalMethodCallListener;
import org.philimone.hds.forms.main.FormFragment;
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

    private ColumnGroupModel headerGroupModel;

    private ColumnModel lastColumnModel;
    private ColumnGroupModel lastGroupModel;

    private OnFormStateListener stateListener;

    public FormController(HForm form, PreloadMap preloadedValues, FormContext formContext, ExternalMethodCallListener methodCallListener) {
        this.form = form;
        this.preloadedValues = (preloadedValues != null) ? preloadedValues : new PreloadMap();
        this.formContext = formContext;
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
        // 1. Handle Header if exists
        if (form.hasHeader()) {
            ColumnGroup headerGroup = form.getHeader();
            this.headerGroupModel = createGroupModel(headerGroup, null, null, null);
            this.headerGroupModel.setHeader(true);
            this.headerGroupModel.setHidden(true);
        }

        // 2. Handle Body Columns
        for (ColumnGroup group : form.getColumns()) {
            if (group.isHeader()) continue; // Already handled or ignored if redundant

            if (group instanceof ColumnRepeatGroup) {
                expandRepeatGroup((ColumnRepeatGroup) group);
            } else {
                createGroupModel(group, null, null, null);
            }
        }
    }

    private void expandRepeatGroup(ColumnRepeatGroup repeatGroup) {
        Integer repeatSize = repeatGroup.getRepeatSize(preloadedValues);

        if (repeatSize == null || repeatSize <= 0) {

            RepeatCountType repeatCountType = repeatGroup.getRepeatCountType(preloadedValues);

            // TODO: Handle dynamic/variable repeat count evaluation
            return;
        }

        for (int i = 0; i < repeatSize; i++) {
            for (ColumnGroup innerGroup : repeatGroup.getColumnsGroups()) {
                // For repeats, we use clones of the inner groups as per current implementation
                ColumnGroup clonedInner = innerGroup.clone();
                createGroupModel(clonedInner, repeatGroup, i, repeatSize);
            }
        }
    }

    private ColumnGroupModel createGroupModel(ColumnGroup group, ColumnRepeatGroup repeatParent, Integer repeatIndex, Integer repeatSize) {
        ColumnGroupModel groupModel;
        
        if (repeatParent != null) {
            groupModel = new ColumnGroupModel(repeatParent, group, repeatIndex, repeatSize);
        } else {
            groupModel = new ColumnGroupModel(group);
        }

        groupModel.setSupportedCalendar(formContext.supportedCalendar);

        // Link groups
        if (lastGroupModel != null) {
            groupModel.setPreviousGroupModel(lastGroupModel);
        }
        lastGroupModel = groupModel;

        for (Column column : group.getColumns()) {
            ColumnModel columnModel = new ColumnModel(column, groupModel);
            
            // Link columns
            if (lastColumnModel != null) {
                columnModel.setPreviousModel(lastColumnModel);
            }
            lastColumnModel = columnModel;

            // Initial value loading (logic from loadColumnValues)
            loadInitialValue(columnModel, repeatParent, repeatIndex);
            
            groupModel.addColumnModel(columnModel);
        }

        groupModels.add(groupModel);
        return groupModel;
    }

    private void loadInitialValue(ColumnModel columnModel, ColumnRepeatGroup repeatParent, Integer repeatIndex) {
        Column column = columnModel.getColumn();
        String columnName = column.getName();
        ColumnType type = column.getType();

        // 1. Handle standard preloaded values (highest priority)
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

        // 4. Handle special system types (only if still empty/not preloaded)
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

        // END_TIMESTAMP is always updated via finalizeForm, but we can set initial if available
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
            // Also set the main string value if it exists or construct it
            //if (preloadedValues.containsKey(baseName)) {
            //    columnModel.setValue(preloadedValues.getStringValue(baseName));
            //}
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
            //Log.d("cgm "+gm.getUuid(), "header="+gm.isHeader()+", hidden="+gm.isHidden()+", displayable="+gm.isDisplayable());
            if (!gm.isHidden() && gm.isDisplayable()) {
                visible.add(gm);
            }
        }
        return visible;
    }

    /**
     * Updates the end timestamp for all relevant columns before saving the form.
     * @param endTimestamp the final timestamp to record
     */
    public void finalizeForm(String endTimestamp) {
        this.formContext.endTimestamp = endTimestamp;

        boolean mediaCollected = false;
        for (ColumnGroupModel gm : groupModels) {
            for (ColumnModel cm : gm.getColumnModels()) {
                ColumnType type = cm.getType();
                if (!mediaCollected && (type == ColumnType.IMAGE || type == ColumnType.VIDEO || type == ColumnType.AUDIO) && !cm.isValueBlank()) {
                    mediaCollected = true;
                }

                if (type == ColumnType.END_TIMESTAMP) {
                    cm.setValue(endTimestamp, ColumnValueStatus.FROM_CALCULATION);
                }
                if (type == ColumnType.MEDIA_COLLECTED && mediaCollected) {
                    cm.setValue(mediaCollected+"", ColumnValueStatus.FROM_CALCULATION);
                }
            }
        }
    }

    public void evaluateAll() {
        for (ColumnGroupModel gm : groupModels) {
            evaluateGroup(gm);
        }
    }

    public void onModelValueChanged(ColumnModel columnModel) {
        // 1. Evaluate All Calculations, Display, ReadOnly and Required conditions
        // Optimization: Start evaluating from the group containing the changed column,
        // as dependencies in this engine only point to previous models.
        int startIndex = groupModels.indexOf(columnModel.getParentGroupModel());
        if (startIndex < 0) startIndex = 0;

        for (int i = startIndex; i < groupModels.size(); i++) {
            evaluateGroup(groupModels.get(i));
        }

        // 2. Notify UI
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

    private void updateGroupVisibility(ColumnGroupModel groupModel) {
        // Update group visibility based on child columns
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
        Column column = columnModel.getColumn();

        // 1. Evaluate Calculation
        evaluateColumnCalculation(columnModel);

        // 2. Evaluate Display Condition
        evaluateColumnDisplayCondition(columnModel);

        // 3. Evaluate Read Only Condition
        evaluateColumnReadOnlyCondition(columnModel);

        // 4. Evaluate Required Condition
        evaluateColumnRequiredCondition(columnModel);
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
