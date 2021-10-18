package org.philimone.hds.forms.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.philimone.hds.forms.main.FormFragment;
import org.philimone.hds.forms.model.Column;
import org.philimone.hds.forms.model.ColumnValue;
import org.philimone.hds.forms.model.enums.ColumnType;
import org.philimone.hds.forms.utilities.StringTools;

import java.util.LinkedHashMap;
import java.util.Map;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public abstract class ColumnView extends LinearLayout {

    protected ColumnGroupView columnGroupView;
    protected Column column;
    protected TextView txtColumnRequired;
    protected ColumnView parentColumn;
    protected ColumnView nextColumn;
    protected boolean displayable;

    public ColumnView(ColumnGroupView view, @LayoutRes int resource, @Nullable AttributeSet attrs, @NonNull Column column) {
        super(view.getContext(), attrs);
        this.columnGroupView = view;
        this.column = column;

        buildViews(resource);
    }

    public ColumnView(ColumnGroupView view, @LayoutRes int resource, @NonNull Column column) {
        this(view, resource,null, column);
    }

    public FormFragment getActivity() {
        return this.columnGroupView.getFormPanel();
    }

    public String getLabel() {
        return this.column.getLabel();
    }

    public String getName() {
        return this.column.getName();
    }

    public ColumnType getType(){
        return this.column.getType();
    }

    public abstract String getValue();

    public abstract String getValueAsXml();

    public abstract void setValue(String value);

    public abstract void updateValues();

    public Column getColumn() {
        return this.column;
    }

    private void buildViews(@LayoutRes int resource) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(resource, this);
    }

    public ColumnValue getColumnValue(){
        ColumnValue columnValue = new ColumnValue(columnGroupView, this);
        return columnValue;
    }

    public boolean isDisplayable() {
        return displayable;
    }

    public void setDisplayable(boolean displayable) {
        this.displayable = displayable;

        columnGroupView.updateVisibility();
    }

    public ColumnView getNextColumn() {
        return nextColumn;
    }

    public void setNextColumn(ColumnView nextColumn) {
        this.nextColumn = nextColumn;
    }

    public ColumnView getParentColumn() {
        return parentColumn;
    }

    public void setParentColumn(ColumnView parentColumn) {
        this.parentColumn = parentColumn;
    }

    public void evaluateDisplayCondition(){

        String displayCondition = column.getDisplayCondition();

        //Log.d("evaluating", getName()+" -> "+displayCondition);

        if (StringTools.isBlank(displayCondition)) {
            setDisplayable(true);
        } else {
            //get all column values (previous)

            Map<String, String> previousValues = new LinkedHashMap<>();
            ColumnView parent = parentColumn;
            while (parent != null) {
                //Log.d("parent-ev", ""+parent);

                String name = parent.getName();
                String value = parent.getValue();
                previousValues.put(parent.getName(), parent.getValue());

                //replace variables with values

                displayCondition = displayCondition.replaceAll("\\$\\{"+name+"\\}", "'" + value + "'");

                parent = parent.parentColumn;
            }


            displayCondition = displayCondition.replaceAll("and", "&&");
            displayCondition = displayCondition.replaceAll("or", "||");
            displayCondition = displayCondition.replaceAll("!=", "<>"); //to avoid !==
            displayCondition = displayCondition.replaceAll("=", "==");
            displayCondition = displayCondition.replaceAll("<>", "!="); //return to normal after =



            //evaluate expression on a script engine
            String result = getActivity().evaluateExpression(displayCondition).toString();
            boolean visible = StringTools.isBlank(result) ? true : result.equals("true");
            //Log.d("evaluation", ""+result);

            setDisplayable(visible);

        }
    }

    @Override
    public String toString() {
        return "ColumnView{"+ getName() +"}";
    }
}
