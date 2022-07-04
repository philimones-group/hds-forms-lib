package org.philimone.hds.forms.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import org.apache.poi.util.StringUtil;
import org.philimone.hds.forms.R;
import org.philimone.hds.forms.listeners.ExternalMethodCallListener;
import org.philimone.hds.forms.model.Column;
import org.philimone.hds.forms.parsers.form.model.FormOptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

public class ColumnMultiSelectView extends ColumnView {

    private TextView txtName;
    private RadioGroup rdgColumnRadioGroup;
    private List<SelectOption> rdbOptions;

    public ColumnMultiSelectView(ColumnGroupView view, @Nullable AttributeSet attrs, @NonNull Column column, ExternalMethodCallListener callListener) {
        super(view, R.layout.column_select_item, attrs, column, callListener);

        this.rdbOptions = new ArrayList<>();

        createView();
    }

    public ColumnMultiSelectView(ColumnGroupView view, @NonNull Column column, ExternalMethodCallListener callListener) {
        this(view, null, column, callListener);
    }

    private void createView() {

        this.txtColumnRequired = findViewById(R.id.txtColumnRequired);
        this.txtName = findViewById(R.id.txtColumnName);
        this.rdgColumnRadioGroup = findViewById(R.id.rdgColumnRadioGroup);

        this.rdgColumnRadioGroup.setEnabled(!this.column.isReadOnly());

        fillOptions();

        updateValues();
    }

    public void refillOptions(){
        for (ColumnMultiSelectView.SelectOption selectOption : this.rdbOptions) {
            CheckBox button = selectOption.button;
            button.setVisibility(selectOption.optionValue.displayable ? VISIBLE : GONE);
        }
    }

    private void fillOptions(){
        Map<String, FormOptions.OptionValue> options = this.column.getTypeOptions();

        this.rdgColumnRadioGroup.removeAllViews();
        this.rdbOptions.clear();

        for (String value : options.keySet()){

            FormOptions.OptionValue optionValue = options.get(value);
            String label = optionValue.label;

            if (!optionValue.displayable) return;

            CheckBox button = new CheckBox(this.getContext());
            button.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
            button.setText(label);
            button.setTextSize(this.getContext().getResources().getDimension(R.dimen.column_value_textsize));
            button.setTextColor(this.getContext().getResources().getColor(R.color.black, null));
            button.setEnabled(!optionValue.readonly);

            if (column.isReadOnly()) {
                button.setClickable(false);
            }

            this.rdgColumnRadioGroup.addView(button);

            this.rdbOptions.add(new SelectOption(optionValue, value, label, button, optionValue.readonly));
        }

        this.rdgColumnRadioGroup.setEnabled(!column.isReadOnly());
    }

    public String getSelectedValue(){

        Set<SelectOption> sop = this.rdbOptions.stream().filter(op -> op.button.isChecked()).collect(toSet());

        if (sop.size()>0) {
            String result = "";
            for (SelectOption opt : sop){
                result += ";" + opt.value;
            }

            return result.substring(1);
        }

        return null;
    }

    @Override
    public void updateValues() {
        txtColumnRequired.setVisibility(this.column.isRequired() ? VISIBLE : GONE);
        txtName.setText(column.getLabel());

        String value = column.getValue();

        if (value != null) {
            String[] values = value.split(";");
            boolean readonlyChecked = false;

            for (String optionValue : values) {
                SelectOption sop = this.rdbOptions.stream().filter(op -> op.value.equalsIgnoreCase(optionValue)).findFirst().orElse(null);

                if (sop != null) {
                    this.rdgColumnRadioGroup.check(sop.button.getId());
                    if (sop.readonly){
                        readonlyChecked = true;
                    }
                }
            }

            if (readonlyChecked) { //any readonly checked
                for (SelectOption selectOption : this.rdbOptions) {
                    selectOption.button.setClickable(false);
                }
            }

            //hide all non selected if display_style = selected_only
            if (this.column.getDisplayStyle().equals(Column.DISPLAY_STYLE_SELECTED_ONLY)){
                List<String> valuesList = Arrays.asList(values);
                for (SelectOption selectOption : this.rdbOptions) {
                    if (!valuesList.contains(selectOption.value)) {
                        selectOption.button.setVisibility(GONE);
                    }
                }
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

    public List<String> getValues(){
        List<String> list = this.rdbOptions.stream().filter(op -> op.button.isChecked()).map(SelectOption::getValue).collect(toList());

        return list;
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
        public CheckBox button;
        public boolean readonly;
        public FormOptions.OptionValue optionValue;

        public SelectOption(FormOptions.OptionValue optionValue, String value, String label, CheckBox button, boolean readonly) {
            this.optionValue = optionValue;
            this.value = value;
            this.label = label;
            this.button = button;
            this.readonly = readonly;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public CheckBox getButton() {
            return button;
        }

        public void setButton(CheckBox button) {
            this.button = button;
        }

        @Override
        public String toString() {
            return "" + label +"";
        }
    }
}
