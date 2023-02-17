package org.philimone.hds.forms.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.philimone.hds.forms.listeners.ExternalMethodCallListener;
import org.philimone.hds.forms.main.FormFragment;
import org.philimone.hds.forms.model.Column;
import org.philimone.hds.forms.model.ColumnValue;
import org.philimone.hds.forms.model.enums.ColumnType;
import org.philimone.hds.forms.parsers.form.model.FormOptions;
import org.philimone.hds.forms.utilities.StringTools;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    protected ExternalMethodCallListener methodCallListener;

    public ColumnView(ColumnGroupView view, @LayoutRes int resource, @Nullable AttributeSet attrs, @NonNull Column column, ExternalMethodCallListener callListener) {
        super(view.getContext(), attrs);
        this.columnGroupView = view;
        this.column = column;

        this.methodCallListener = callListener;

        buildViews(resource);
    }

    public ColumnView(ColumnGroupView view, @LayoutRes int resource, @NonNull Column column, ExternalMethodCallListener callListener) {
        this(view, resource,null, column, callListener);
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

    protected void afterUserInput() {
        List<ColumnView> columnViews = columnGroupView.getColumnViews();

        int i = columnViews.indexOf(this);

        //calculate and evaluate display on the next column views of this groupview
        for (int j = i+1; j < columnViews.size(); j++) {
            ColumnView columnView = columnViews.get(j);
            columnView.evaluateCalculation();
            columnView.evaluateDisplayCondition();
        }
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

    private String translateExpression(String expression) {
        Map<String, String> previousValues = new LinkedHashMap<>();
        ColumnView parent = parentColumn;
        while (parent != null) {
            //Log.d("parent-ev", ""+parent);

            String name = parent.getName();
            String value = parent.getValue();
            previousValues.put(parent.getName(), parent.getValue());

            //replace variables with values

            expression = expression.replaceAll("\\$\\{"+name+"\\}", "'" + value + "'");            
            
            parent = parent.parentColumn;
        }
        
        expression = expression.replace("and", "&&");
        expression = expression.replace("or", "||");
        expression = expression.replace("!=", "<>"); //to avoid !==
        expression = expression.replace("=", "==");
        expression = expression.replace("<>", "!="); //return to normal after =
        
        
        return expression;
    }

    private List<String> getExpressionCalls(String expression){
        ArrayList<String> list = new ArrayList<>();

        String callRegex = "call:(.*?)\\)";
        Pattern pattern_call = Pattern.compile(callRegex);
        Matcher matcher_call = pattern_call.matcher(expression);
        while (matcher_call.find()){
            String method = matcher_call.group(1)+")";
            list.add(method);
        }

        return list;
    }

    private String[] getMethodArgs(String methodCall){
        ArrayList<String> list = new ArrayList<>();

        String callRegex = "'(.*?)'";
        Pattern pattern_call = Pattern.compile(callRegex);
        Matcher matcher_call = pattern_call.matcher(methodCall);
        while (matcher_call.find()){
            String arg = matcher_call.group(1);
            if (arg != null && arg.equalsIgnoreCase("null")){
                arg = null;
            }
            list.add(arg);
        }

        return list.toArray(new String[list.size()]);
    }

    public void evaluateDisplayCondition(){

        String displayCondition = column.getDisplayCondition();

        //Log.d("evaluating", getName()+" -> "+displayCondition);

        if (StringTools.isBlank(displayCondition)) {
            setDisplayable(true);
        } else {
            //get all column values (previous)

            displayCondition = translateExpression(displayCondition);

            //Log.d("displaycondition", ""+displayCondition);
            //evaluate expression on a script engine
            String result = getActivity().evaluateExpression(displayCondition).toString();
            boolean visible = StringTools.isBlank(result) ? true : result.equals("true");
            //Log.d("evaluation", ""+result);

            setDisplayable(visible);

        }

        //evaluate also select options display conditions
        if (column.isOptionsConditionallyDisplayable() && (this instanceof ColumnSelectView || this instanceof ColumnMultiSelectView)) {

            for (FormOptions.OptionValue optionValue : column.getTypeOptions().values()){
                if (StringTools.isBlank(optionValue.displayCondition)){
                    optionValue.displayable = true;
                } else {
                    displayCondition = translateExpression(optionValue.displayCondition);
                    String result = getActivity().evaluateExpression(displayCondition).toString();
                    boolean visible = StringTools.isBlank(result) ? true : result.equals("true");
                    optionValue.displayable = visible;
                }
            }

            if (this instanceof  ColumnSelectView) {
                ((ColumnSelectView) this).refillOptions();
            } else {
                ((ColumnMultiSelectView) this).refillOptions();
            }
        }

    }

    public void evaluateCalculation(){
        String calculation = column.getCalculation();

        if (StringTools.isBlank(calculation)) return;

        //replace variables with values
        calculation = translateExpression(calculation);

        //find method calls, call:methodName()
        List<String> methodCalls = getExpressionCalls(calculation);

        for (String methodCall : methodCalls) {
            //execute method calls -> listener call and return
            String[] methodArgs = getMethodArgs(methodCall);
            if (methodCallListener != null) {
                String result = methodCallListener.onCallMethod(methodCall, methodArgs);
                if (result != null) {
                    calculation = calculation.replace("call:" + methodCall, result);
                } else {
                    calculation = calculation.replace("call:" + methodCall, "");;
                }
            }
        }

        //Log.d("expression", calculation);
        Object objResult = getActivity().evaluateExpression(calculation);
        String calculationResult = objResult==null ? "" : objResult.toString();

        //Log.d("expression-calc", "result: "+calculationResult);

        //set column value
        setValue(calculationResult); //Update the value according to the type
    }

    public boolean isHidden() {
        return column.isHidden();
    }

    @Override
    public String toString() {
        return "ColumnView{"+ getName() +"}";
    }
}
