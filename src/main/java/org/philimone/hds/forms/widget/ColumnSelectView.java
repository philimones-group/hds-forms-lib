package org.philimone.hds.forms.widget;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import org.philimone.hds.forms.R;
import org.philimone.hds.forms.model.Column;
import org.philimone.hds.forms.model.enums.ColumnType;

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

    public ColumnSelectView(ColumnGroupView view, @Nullable AttributeSet attrs, @NonNull Column column) {
        super(view, R.layout.column_select_item, attrs, column);

        this.rdbOptions = new ArrayList<>();

        createView();
    }

    public ColumnSelectView(ColumnGroupView view, @NonNull Column column) {
        this(view, null, column);
    }

    private void createView() {

        this.txtColumnRequired = findViewById(R.id.txtColumnRequired);
        this.txtName = findViewById(R.id.txtColumnName);
        this.rdgColumnRadioGroup = findViewById(R.id.rdgColumnRadioGroup);

        txtColumnRequired.setVisibility(this.column.isRequired() ? VISIBLE : GONE);
        txtName.setText(column.getLabel());

        fillOptions();
    }

    private void fillOptions(){
        Map<String,String> options = this.column.getTypeOptions();

        for (String value : options.keySet()){
            String label = options.get(value);


            RadioButton button = new RadioButton(this.getContext());
            button.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
            button.setText(label);
            button.setTextSize(this.getContext().getResources().getDimension(R.dimen.column_value_textsize));
            button.setTextColor(this.getContext().getResources().getColor(R.color.black, null));

            this.rdgColumnRadioGroup.addView(button);

            this.rdbOptions.add(new SelectOption(value, label, button));
        }
    }

    private String getSelectedValue(){

        int id = this.rdgColumnRadioGroup.getCheckedRadioButtonId();

        SelectOption sop = this.rdbOptions.stream().filter( op -> op.button.getId()==id).findFirst().orElse(null);

        return sop==null ? null : sop.value;
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

        public SelectOption(String value, String label, RadioButton button) {
            this.value = value;
            this.label = label;
            this.button = button;
        }

        @Override
        public String toString() {
            return "" + label +"";
        }
    }
}
