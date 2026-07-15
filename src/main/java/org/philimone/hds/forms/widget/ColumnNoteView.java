package org.philimone.hds.forms.widget;

import android.util.AttributeSet;
import android.widget.TextView;

import org.philimone.hds.forms.R;
import org.philimone.hds.forms.listeners.ExternalMethodCallListener;
import org.philimone.hds.forms.model.ColumnModel;
import org.philimone.hds.forms.model.enums.ColumnType;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.math.BigDecimal;

public class ColumnNoteView extends ColumnView {

    private TextView txtName;
    private TextView txtValue;

    public ColumnNoteView(ColumnGroupView view, @Nullable AttributeSet attrs, @NonNull ColumnModel columnModel, ExternalMethodCallListener callListener) {
        super(view, R.layout.column_string_note_item, attrs, columnModel, callListener);

        createView();
    }

    public ColumnNoteView(ColumnGroupView view, @NonNull ColumnModel columnModel, ExternalMethodCallListener callListener) {
        this(view, null, columnModel, callListener);
    }

    private void createView() {
        this.txtColumnRequired = findViewById(R.id.txtColumnRequired);
        this.txtName = findViewById(R.id.txtColumnName);
        this.txtValue = findViewById(R.id.txtColumnValue);

        refreshModelToUI();
    }

    @Override
    public void refreshLabels() {
        setTextHtml(txtName, column.getLabel());
    }

    @Override
    public void refreshModelToUI(){
        txtValue.setText("");
    }

    @Override
    public void refreshInteractionState() {
    }

    @Override
    public void setValue(String value) {
        this.columnModel.setValue("");
        refreshModelToUI();
    }

    @Override
    public String getValue() {
        return this.columnModel.getValue();
    }

    public Integer getValueAsInt(){
        if (column.getType() == ColumnType.INTEGER) {
            try {
                return Integer.parseInt(getValue());
            } catch (Exception ex) {
                return null;
            }
        }

        return null;
    }

    public BigDecimal getValueDecimal(){
        if (column.getType() == ColumnType.DECIMAL) {
            try {
                return new BigDecimal(getValue());
            } catch (Exception ex) {
                return null;
            }
        }

        return null;
    }

    @Override
    public String getValueAsXml() {
        String name = this.column.getName();
        return "<"+ name + " />";
    }

}
