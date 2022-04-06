package org.philimone.hds.forms.model;

import org.philimone.hds.forms.model.enums.ColumnType;
import org.philimone.hds.forms.utilities.StringTools;
import org.philimone.hds.forms.widget.ColumnDateTimeView;
import org.philimone.hds.forms.widget.ColumnDateView;
import org.philimone.hds.forms.widget.ColumnGpsView;
import org.philimone.hds.forms.widget.ColumnGroupView;
import org.philimone.hds.forms.widget.ColumnMultiSelectView;
import org.philimone.hds.forms.widget.ColumnSelectView;
import org.philimone.hds.forms.widget.ColumnTextView;
import org.philimone.hds.forms.widget.ColumnTextboxView;
import org.philimone.hds.forms.widget.ColumnView;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ColumnValue implements Serializable {

    private int columnGroupId;
    private int columnId;
    private Column column;
    private String value; //value for STRING,SELECT,
    private Integer integerValue; //value for INTEGER
    private BigDecimal decimalValue; //value for DECIMAL
    private Date dateValue; //value for DATE
    private List<String> multiSelectValues; //value for MULTISELECT
    private Map<String,Double> gpsValues = new LinkedHashMap<>(); //value for GPS

    private boolean errors;
    private String errorMessage;

    public ColumnValue() {

    }

    public ColumnValue(int columnGroupId, int columnId, Column column) {
        this.columnGroupId = columnGroupId;
        this.columnId = columnId;
        this.column = column;
    }

    public ColumnValue(ColumnGroupView columnGroupView, ColumnView columnView) {
        this.columnGroupId = columnGroupView.getId();
        this.columnId = columnView.getId();
        this.column = columnView.getColumn();

        if (columnGroupView.isDisplayable()) { //if it is not displayable just be null/empty
            retrieveValues(columnView);
        }
    }

    private void retrieveValues(ColumnView columnView) {
        if (columnView instanceof ColumnTextView || columnView instanceof ColumnSelectView || columnView.getType()==ColumnType.STRING) { //TEXTBOX.STRING
            this.value = columnView.getValue();
        }
        if (columnView instanceof ColumnTextboxView) {
            ColumnTextboxView columntxt = (ColumnTextboxView) columnView;
            if (columntxt.getType()==ColumnType.DECIMAL) { this.decimalValue = columntxt.getValueDecimal(); }
            if (columntxt.getType()==ColumnType.INTEGER) { this.integerValue = columntxt.getValueAsInt(); }
            this.value = columnView.getValue();
        }
        if (columnView instanceof ColumnDateView) {
            this.dateValue = ((ColumnDateView) columnView).getValueAsDate();
            this.value = columnView.getValue();
        }
        if (columnView instanceof ColumnDateTimeView) {
            this.dateValue = ((ColumnDateTimeView) columnView).getValueAsDate();
            this.value = columnView.getValue();
        }
        if (columnView instanceof ColumnMultiSelectView) {
            ColumnMultiSelectView column = ((ColumnMultiSelectView) columnView);
            this.multiSelectValues = column.getValues();
            this.value = column.getSelectedValue();
        }
        if (columnView instanceof ColumnGpsView) {
            ColumnGpsView gpsView = (ColumnGpsView) columnView;
            this.gpsValues = gpsView.getValues();
            this.value = gpsView.getValue();
        }
    }

    public int getColumnGroupId() {
        return columnGroupId;
    }

    public void setColumnGroupId(int columnGroupId) {
        this.columnGroupId = columnGroupId;
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

    public boolean isRequired(){
        return this.column.isRequired();
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

