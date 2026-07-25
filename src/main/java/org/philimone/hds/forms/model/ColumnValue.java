package org.philimone.hds.forms.model;

import android.net.Uri;

import org.philimone.hds.forms.model.enums.ColumnType;
import org.philimone.hds.forms.utilities.GpsFormatter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import mz.betainteractive.utilities.DateUtil;

public class ColumnValue implements Serializable {

    private String columnGroupUuid;
    private int columnGroupId;
    private int columnId;
    private Column column;
    private String value; //value for STRING,SELECT,
    private String valueLabel;
    private Integer integerValue; //value for INTEGER
    private BigDecimal decimalValue; //value for DECIMAL
    private Date dateValue; //value for DATE
    private List<String> multiSelectValues; //value for MULTISELECT
    private Map<String,Double> gpsValues = new LinkedHashMap<>(); //value for GPS

    private boolean errors;
    private String errorMessage;

    public ColumnValue() {

    }

    public ColumnValue(ColumnGroupModel groupModel, ColumnModel columnModel) {
        this.columnGroupUuid = groupModel.getUuid();
        this.column = columnModel.getColumn();
        this.value = columnModel.getValue();
        this.valueLabel = this.value;
        this.gpsValues = columnModel.getGpsValues();
        this.multiSelectValues = columnModel.getMultiSelectValues();

        // Populate typed values based on ColumnType
        ColumnType type = column.getType();

        if (type == ColumnType.INTEGER && value != null) {
            this.integerValue = columnModel.getIntegerValue();
        }

        if (type == ColumnType.DECIMAL && value != null) {
            this.decimalValue = columnModel.getDecimalValue();
        }

        if (type == ColumnType.DATE && value != null) {
            DateUtil dateUtil = new DateUtil(groupModel.getSupportedCalendar());
            this.dateValue = DateUtil.toDateYMD(value);
            this.valueLabel = this.dateValue != null ? dateUtil.formatYMD(this.dateValue) : this.value;
        }

        if (type == ColumnType.DATETIME && value != null) {
            DateUtil dateUtil = new DateUtil(groupModel.getSupportedCalendar());
            this.dateValue = DateUtil.toDateYMDHMS(value);
            this.valueLabel = this.dateValue != null ? dateUtil.formatYMDHMS(this.dateValue) : this.value;
        }

        if (type == ColumnType.TIME && value != null) {
            this.dateValue = DateUtil.toDateYMDHMS(value);
            if (this.dateValue != null) {
                this.valueLabel = DateUtil.formatTimeHM(this.dateValue);
            }
        }

        if (type == ColumnType.TIMESTAMP && value != null) {
            DateUtil dateUtil = new DateUtil(groupModel.getSupportedCalendar());
            this.dateValue = DateUtil.toDatePrecise(value);
            this.valueLabel = this.dateValue != null ? dateUtil.formatPrecise(this.dateValue) : this.value;
        }

        // For GPS and MULTISELECT, they are already populated from columnModel
        // For SELECT, we might not have the label here without the ColumnSelectView or the options map
        // However, we can try to retrieve the label if options are available in Column
        if (type == ColumnType.SELECT && value != null) {
            this.valueLabel = columnModel.getSelectedValueLabel();
        }

        if (type == ColumnType.MULTI_SELECT) {
            this.valueLabel = columnModel.getSelectedValuesLabels();
        }

        if (type == ColumnType.GPS) {
            this.gpsValues = columnModel.getGpsValues();
            if (this.gpsValues != null && !this.gpsValues.isEmpty()) {
                this.valueLabel = GpsFormatter.formatDMS(getColumnName(), this.gpsValues);
            }
        }

        if (type == ColumnType.AUDIO || type == ColumnType.IMAGE || type == ColumnType.VIDEO) {
            if (this.value != null && this.value.startsWith("file:")) {
                this.valueLabel = Uri.parse(this.value).getLastPathSegment();
            }
        }

        // For backwards compatibility with code that uses integer IDs if any
        this.columnGroupId = columnGroupUuid.hashCode();
    }

    public int getColumnGroupId() {
        return columnGroupId;
    }

    public void setColumnGroupId(int columnGroupId) {
        this.columnGroupId = columnGroupId;
    }

    public String getColumnGroupUuid() {
        return columnGroupUuid;
    }

    public int getColumnId() {
        return columnId;
    }

    public void setColumnId(int columnId) {
        this.columnId = columnId;
    }

    public Column getColumn() {
        return column;
    }

    public void setColumn(Column column) {
        this.column = column;
    }

    public ColumnType getColumnType(){
        return this.column.getType();
    }

    public String getColumnName(){
        return this.column.getName();
    }

    public String getValue() {
        return value;
    }

    public String getValueLabel() {
        return valueLabel;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Integer getIntegerValue() {
        return integerValue;
    }

    public void setIntegerValue(Integer integerValue) {
        this.integerValue = integerValue;
    }

    public BigDecimal getDecimalValue() {
        return decimalValue;
    }

    public void setDecimalValue(BigDecimal decimalValue) {
        this.decimalValue = decimalValue;
    }

    public Date getDateValue() {
        return dateValue;
    }

    public void setDateValue(Date dateValue) {
        this.dateValue = dateValue;
    }

    public List<String> getMultiSelectValues() {
        return multiSelectValues;
    }

    public void setMultiSelectValues(List<String> multiSelectValues) {
        this.multiSelectValues = multiSelectValues;
    }

    public Map<String, Double> getGpsValues() {
        return gpsValues;
    }

    public void setGpsValues(Map<String, Double> gpsValues) {
        this.gpsValues = gpsValues;
    }

    public boolean hasErrors() {
        return errors;
    }

    public void setErrors(boolean errors) {
        this.errors = errors;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
        this.errors = errorMessage != null || errorMessage.isEmpty();
    }
}
