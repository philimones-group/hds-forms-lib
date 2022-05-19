package org.philimone.hds.forms.widget;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import org.philimone.hds.forms.R;
import org.philimone.hds.forms.listeners.ExternalMethodCallListener;
import org.philimone.hds.forms.model.Column;
import org.philimone.hds.forms.model.enums.ColumnType;
import org.philimone.hds.forms.parsers.form.model.FormOptions;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ColumnSelectView extends ColumnView {

    private TextView txtName;
    private RadioGroup rdgColumnRadioGroup;
    private List<SelectOption> rdbOptions;

    public ColumnSelectView(ColumnGroupView view, @Nullable AttributeSet attrs, @NonNull Column column, ExternalMethodCallListener callListener) {
        super(view, R.layout.column_select_item, attrs, column, callListener);

        this.rdbOptions = new ArrayList<>();

        createView();
    }

    public ColumnSelectView(ColumnGroupView view, @NonNull Column column, ExternalMethodCallListener callListener) {
        this(view, null, column, callListener);
    }

    private void createView() {

        this.txtColumnRequired = findViewById(R.id.txtColumnRequired);
        this.txtName = findViewById(R.id.txtColumnName);
        this.rdgColumnRadioGroup = findViewById(R.id.rdgColumnRadioGroup);

        txtColumnRequired.setVisibility(this.column.isRequired() ? VISIBLE : GONE);
        txtName.setText(column.getLabel());

        this.rdgColumnRadioGroup.setEnabled(!this.column.isReadOnly());

        fillOptions();

        updateValues();
    }

    private void fillOptions(){
        Map<String, FormOptions.OptionValue> options = this.column.getTypeOptions();

        for (String value : options.keySet()){
            FormOptions.OptionValue optionValue = options.get(value);


            RadioButton button = new RadioButton(this.getContext());
            button.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
            button.setText(optionValue.label);
            button.setTextSize(this.getContext().getResources().getDimension(R.dimen.column_value_textsize));
            button.setTextColor(this.getContext().getResources().getColor(R.color.black));
            button.setEnabled(!optionValue.readonly);

            if (column.isReadOnly()) {
                button.setClickable(false);
            }


            this.rdgColumnRadioGroup.addView(button);

            this.rdbOptions.add(new SelectOption(value, optionValue.label, button, optionValue.readonly));
        }
        //Log.d("readonly-"+column.getName(), ""+column.isReadOnly());
        this.rdgColumnRadioGroup.setEnabled(!column.isReadOnly());
    }

    private String getSelectedValue(){

        int id = this.rdgColumnRadioGroup.getCheckedRadioButtonId();

        SelectOption sop = this.rdbOptions.stream().filter( op -> op.button.getId()==id).findFirst().orElse(null);

        return sop==null ? null : sop.value;
    }

    @Override
    public void updateValues() {
        txtColumnRequired.setVisibility(this.column.isRequired() ? VISIBLE : GONE);
        txtName.setText(column.getLabel());

        String value = column.getValue();

        if (value != null) {
            SelectOption sop = this.rdbOptions.stream().filter( op -> op.value.equalsIgnoreCase(value)).findFirst().orElse(null);

            if (sop != null) {
                this.rdgColumnRadioGroup.check(sop.button.getId());

                //like HOH selected, if a readonly value is checked the other options should be disabled
                if (sop.readonly) {
                    this.rdbOptions.forEach( selectOption -> {
                        selectOption.button.setClickable(false);
                    });
                }

                //hide all non selected if display_style = selected_only
                if (this.column.getDisplayStyle().equals(Column.DISPLAY_STYLE_SELECTED_ONLY)){
                    this.rdbOptions.forEach(selectOption -> {
                        if (!selectOption.value.equals(value)) {
                            selectOption.button.setVisibility(GONE);
                        }
                    });
                }

                sop.button.setEnabled(!sop.readonly);
            }

        }

    }

    @Override
    public void setValue(String value) {
        this.column.setValue(value);
        updateValues();
    }

    @Override
    public String getValue() {
        return getSelectedValue();
    }

    @Override
    public String getValueAsXml() {
        String value = getValue();
        String name = this.column.getName();

        return value==null ? "<"+ name + " />" : "<"+name+">"+value+"</ "+name+">";
    }

    class SelectOption {
        public String value;
        public String label;
        public RadioButton button;
        public boolean readonly;

        public SelectOption(String value, String label, RadioButton button, boolean readonly) {
            this.value = value;
            this.label = label;
            this.button = button;
            this.readonly = readonly;
        }

        @Override
        public String toString() {
            return "" + label +"";
        }
    }
}
