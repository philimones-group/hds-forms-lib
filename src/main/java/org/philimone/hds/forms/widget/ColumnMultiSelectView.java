package org.philimone.hds.forms.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import org.philimone.hds.forms.R;
import org.philimone.hds.forms.model.Column;

import java.util.ArrayList;
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

    public ColumnMultiSelectView(ColumnGroupView view, @Nullable AttributeSet attrs, @NonNull Column column) {
        super(view, R.layout.column_select_item, attrs, column);

        this.rdbOptions = new ArrayList<>();

        createView();
    }

    public ColumnMultiSelectView(ColumnGroupView view, @NonNull Column column) {
        this(view, null, column);
    }

    private void createView() {

        this.txtColumnRequired = findViewById(R.id.txtColumnRequired);
        this.txtName = findViewById(R.id.txtColumnName);
        this.rdgColumnRadioGroup = findViewById(R.id.rdgColumnRadioGroup);

        fillOptions();

        updateValues();
    }

    private void fillOptions(){
        Map<String,String> options = this.column.getTypeOptions();

        for (String value : options.keySet()){
            String label = options.get(value);

            CheckBox button = new CheckBox(this.getContext());
            button.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
            button.setText(label);
            button.setTextSize(this.getContext().getResources().getDimension(R.dimen.column_value_textsize));
            button.setTextColor(this.getContext().getResources().getColor(R.color.black, null));

            this.rdgColumnRadioGroup.addView(button);

            this.rdbOptions.add(new SelectOption(value, label, button));
        }
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

            for (String optionValue : values) {
                SelectOption sop = this.rdbOptions.stream().filter(op -> op.value==optionValue).findFirst().orElse(null);

                if (sop != null) {
                    this.rdgColumnRadioGroup.check(sop.button.getId());
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

        public SelectOption(String value, String label, CheckBox button) {
            this.value = value;
            this.label = label;
            this.button = button;
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
