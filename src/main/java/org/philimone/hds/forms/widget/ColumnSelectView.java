package org.philimone.hds.forms.widget;

import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import org.philimone.hds.forms.R;
import org.philimone.hds.forms.model.enums.ColumnDisplayStyle;
import org.philimone.hds.forms.model.listeners.ExternalMethodCallListener;
import org.philimone.hds.forms.model.Column;
import org.philimone.hds.forms.model.ColumnModel;
import org.philimone.hds.forms.model.parsers.form.model.FormOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ColumnSelectView extends ColumnView {

    private TextView txtName;
    private RadioGroup rdgColumnRadioGroup;
    private List<SelectOption> rdbOptions;

    public ColumnSelectView(ColumnGroupView view, @Nullable AttributeSet attrs, @NonNull ColumnModel columnModel, ExternalMethodCallListener callListener) {
        super(view, R.layout.column_select_item, attrs, columnModel, callListener);

        this.rdbOptions = new ArrayList<>();

        createView();
    }

    public ColumnSelectView(ColumnGroupView view, @NonNull ColumnModel columnModel, ExternalMethodCallListener callListener) {
        this(view, null, columnModel, callListener);
    }

    private void createView() {

        this.txtColumnRequired = findViewById(R.id.txtColumnRequired);
        this.txtName = findViewById(R.id.txtColumnName);
        this.rdgColumnRadioGroup = findViewById(R.id.rdgColumnRadioGroup);

        txtColumnRequired.setVisibility(columnModel.isRequired() ? VISIBLE : GONE);
        refreshLabels();

        this.rdgColumnRadioGroup.setEnabled(!columnModel.isReadOnly());

        this.rdgColumnRadioGroup.setOnCheckedChangeListener((group, checkedId) -> onSelectedItem(group, checkedId));

        fillOptions();

        refreshModelToUI();
    }

    private void onSelectedItem(RadioGroup group, int checkedId) {
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
            RadioButton button = selectOption.button;
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

        for (String value : options.keySet()){
            FormOptions.OptionValue optionValue = options.get(value);

            if (!optionValue.displayable) continue;

            RadioButton button = new RadioButton(this.getContext());
            button.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
            setTextHtml(button, optionValue.label);
            button.setTextColor(this.getContext().getResources().getColor(R.color.black));
            button.setEnabled(!optionValue.readonly);

            if (columnModel.isReadOnly()) {
                button.setClickable(false);
            }

            this.rdgColumnRadioGroup.addView(button);

            this.rdbOptions.add(new SelectOption(optionValue, value, optionValue.label, button, optionValue.readonly));
        }
        this.rdgColumnRadioGroup.setEnabled(!columnModel.isReadOnly());
    }

    private String getSelectedValue(){
        int id = this.rdgColumnRadioGroup.getCheckedRadioButtonId();

        SelectOption sop = this.rdbOptions.stream().filter( op -> op.button.getId()==id).findFirst().orElse(null);

        return sop==null ? null : sop.value;
    }

    public String getSelectedValueLabel(){
        int id = this.rdgColumnRadioGroup.getCheckedRadioButtonId();
        SelectOption sop = this.rdbOptions.stream().filter( op -> op.button.getId()==id).findFirst().orElse(null);
        return sop==null ? null : sop.label;
    }

    @Override
    public void refreshModelToUI() {
        String value = columnModel.getValue();

        if (value != null) {
            SelectOption sop = this.rdbOptions.stream().filter( op -> op.value.equalsIgnoreCase(value)).findFirst().orElse(null);

            if (sop != null) {
                this.rdgColumnRadioGroup.check(sop.button.getId());

                if (sop.readonly) {
                    for (SelectOption selectOption : this.rdbOptions) {
                        selectOption.button.setClickable(false);
                    }
                }

                if (ColumnDisplayStyle.SELECTED_ONLY.getCode().equals(this.column.getDisplayStyle())){
                    for (SelectOption selectOption : this.rdbOptions) {
                        if (!selectOption.value.equals(value)) {
                            selectOption.button.setVisibility(GONE);
                        }
                    }
                }

                sop.button.setEnabled(!sop.readonly);
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

    @Override
    public String getValueAsXml() {
        String value = getValue();
        String name = this.column.getName();

        return value==null ? "<"+ name + " />" : "<"+name+">"+value+"</ "+name+">";
    }

    static class SelectOption {
        public String value;
        public String label;
        public RadioButton button;
        public boolean readonly;
        public FormOptions.OptionValue optionValue;

        public SelectOption(FormOptions.OptionValue optionValue, String value, String label, RadioButton button, boolean readonly) {
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
