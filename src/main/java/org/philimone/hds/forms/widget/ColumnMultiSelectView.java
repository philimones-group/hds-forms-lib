package org.philimone.hds.forms.widget;

import android.util.AttributeSet;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import org.philimone.hds.forms.R;
import org.philimone.hds.forms.listeners.ExternalMethodCallListener;
import org.philimone.hds.forms.model.Column;
import org.philimone.hds.forms.model.ColumnModel;
import org.philimone.hds.forms.model.parsers.form.model.FormOptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import static java.util.stream.Collectors.toSet;

public class ColumnMultiSelectView extends ColumnView {

    private TextView txtName;
    private RadioGroup rdgColumnRadioGroup;
    private List<SelectOption> rdbOptions;
    private CompoundButton.OnCheckedChangeListener onCheckedChangeListener = (buttonView, isChecked) -> onSelectedItem();

    public ColumnMultiSelectView(ColumnGroupView view, @Nullable AttributeSet attrs, @NonNull ColumnModel columnModel, ExternalMethodCallListener callListener) {
        super(view, R.layout.column_select_item, attrs, columnModel, callListener);

        this.rdbOptions = new ArrayList<>();

        createView();
    }

    public ColumnMultiSelectView(ColumnGroupView view, @NonNull ColumnModel columnModel, ExternalMethodCallListener callListener) {
        this(view, null, columnModel, callListener);
    }

    private void createView() {

        this.txtColumnRequired = findViewById(R.id.txtColumnRequired);
        this.txtName = findViewById(R.id.txtColumnName);
        this.rdgColumnRadioGroup = findViewById(R.id.rdgColumnRadioGroup);

        this.rdgColumnRadioGroup.setEnabled(!columnModel.isReadOnly());

        fillOptions();

        refreshModelToUI();
    }

    private void onSelectedItem() {
        Log.d("multi select", "selection changed");
        this.setValue(getSelectedValue());
        afterUserInput();
    }

    @Override
    public void refreshLabels() {
        setTextHtml(txtName, column.getLabel());

        for (SelectOption selectOption : this.rdbOptions) {
            setTextHtml(selectOption.button, selectOption.optionValue.label);
        }
    }

    public void refillOptions(){
        for (SelectOption selectOption : this.rdbOptions) {
            CheckBox button = selectOption.button;
            button.setVisibility(selectOption.optionValue.displayable ? VISIBLE : GONE);

            button.setEnabled(!selectOption.optionValue.readonly);

            if (columnModel.isReadOnly()) {
                button.setClickable(false);
            }
        }

        this.rdgColumnRadioGroup.setEnabled(!columnModel.isReadOnly());
    }

    private void fillOptions(){
        Map<String, FormOptions.OptionValue> options = this.column.getTypeOptions();

        this.rdgColumnRadioGroup.removeAllViews();
        this.rdbOptions.clear();

        for (String value : options.keySet()){

            FormOptions.OptionValue optionValue = options.get(value);
            String label = optionValue.label;

            if (!optionValue.displayable) continue;

            CheckBox button = new CheckBox(this.getContext());
            button.setLayoutParams(new RadioGroup.LayoutParams(RadioGroup.LayoutParams.MATCH_PARENT, RadioGroup.LayoutParams.WRAP_CONTENT));
            setTextHtml(button, label);
            button.setTextColor(this.getContext().getResources().getColor(R.color.black));
            button.setOnCheckedChangeListener(onCheckedChangeListener);
            button.setEnabled(!optionValue.readonly);

            if (columnModel.isReadOnly()) {
                button.setClickable(false);
            }

            this.rdgColumnRadioGroup.addView(button);

            this.rdbOptions.add(new SelectOption(optionValue, value, label, button, optionValue.readonly));
        }

        this.rdgColumnRadioGroup.setEnabled(!columnModel.isReadOnly());
    }

    public String getSelectedValue(){
        Set<SelectOption> sop = this.rdbOptions.stream().filter(op -> op.button.isChecked()).collect(toSet());

        if (!sop.isEmpty()) {
            StringBuilder result = new StringBuilder();
            for (SelectOption opt : sop){
                result.append(ColumnModel.DELIMITER).append(opt.value);
            }

            return result.substring(1);
        }

        return null;
    }

    public String getSelectedValueLabel(){
        Set<SelectOption> sop = this.rdbOptions.stream().filter(op -> op.button.isChecked()).collect(toSet());

        if (sop.size()>0) {
            StringBuilder result = new StringBuilder();
            for (SelectOption opt : sop){
                result.append(ColumnModel.DELIMITER).append(opt.label);
            }

            return result.substring(1);
        }

        return null;
    }

    @Override
    public void refreshModelToUI() {

        String value = columnModel.getValue();
        Log.d("multiselect", "value = " + value);

        List<String> valuesList = value != null ? Arrays.asList(value.split(ColumnModel.DELIMITER)) : new ArrayList<>();
        boolean readonlyChecked = false;

        for (SelectOption selectOption : this.rdbOptions) {
            boolean shouldBeChecked = valuesList.contains(selectOption.value);

            // 1. Temporarily remove listener to prevent feedback loops and "live selection" logic during sync
            selectOption.button.setOnCheckedChangeListener(null);

            // 2. Only call setChecked if the state actually needs to change (avoids visual flicker)
            if (selectOption.button.isChecked() != shouldBeChecked) {
                selectOption.button.setChecked(shouldBeChecked);
            }

            if (shouldBeChecked && selectOption.readonly) {
                readonlyChecked = true;
            }

            // 3. Re-attach the single shared listener
            selectOption.button.setOnCheckedChangeListener(onCheckedChangeListener);
        }

        if (readonlyChecked) {
            for (SelectOption selectOption : this.rdbOptions) {
                selectOption.button.setClickable(false);
            }
        }

        if (Column.DISPLAY_STYLE_SELECTED_ONLY.equals(this.column.getDisplayStyle())) {
            for (SelectOption selectOption : this.rdbOptions) {
                if (!valuesList.contains(selectOption.value)) {
                    selectOption.button.setVisibility(GONE);
                } else {
                    selectOption.button.setVisibility(VISIBLE);
                }
            }
        }
    }

    @Override
    public void refreshInteractionState() {
        refillOptions();
    }

    @Override
    public void setValue(String value) {
        this.columnModel.setValue(value);
        refreshModelToUI();
    }

    @Override
    public String getValue() {
        return this.columnModel.getValue();
    }

    public List<String> getValues(){
        String value = columnModel.getValue();
        if (value == null) return new ArrayList<>();
        return Arrays.asList(value.split(ColumnModel.DELIMITER));
    }

    @Override
    public String getValueAsXml() {
        String value = getValue();
        String name = this.column.getName();

        return value==null ? "<"+ name + " />" : "<"+name+">"+value+"</ "+name+">";
    }

    static class SelectOption {
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

        @Override
        public String toString() {
            return "" + label +"";
        }
    }
}
