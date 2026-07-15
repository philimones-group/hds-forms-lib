package org.philimone.hds.forms.model;

import android.util.Log;
import org.apache.commons.jexl3.*;
import org.philimone.hds.forms.listeners.ExternalMethodCallListener;
import mz.betainteractive.utilities.StringUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Handles the translation and evaluation of ODK-style expressions using JEXL.
 * Decoupled from the View hierarchy.
 */
public class FormExpressionEvaluator {

    private JexlEngine jexlEngine;
    private ExternalMethodCallListener methodCallListener;

    public FormExpressionEvaluator(ExternalMethodCallListener methodCallListener) {
        this.jexlEngine = new JexlBuilder().create();
        this.methodCallListener = methodCallListener;
    }

    public Object evaluate(String expression, ColumnModel contextModel) {
        if (StringUtil.isBlank(expression)) return null;

        String translated = translateExpression(expression, contextModel);
        translated = translateMethodCalls(translated);

        try {
            JexlExpression jexlExpr = jexlEngine.createExpression(translated);
            JexlContext jexlContext = new MapContext();
            return jexlExpr.evaluate(jexlContext);
        } catch (Exception e) {
            Log.e("FormExpressionEvaluator", "Error evaluating: " + translated, e);
            return null;
        }
    }

    private String translateExpression(String expression, ColumnModel contextModel) {
        ColumnModel parent = contextModel.getPreviousModel();
        
        while (parent != null) {
            String name = parent.getName();
            // Matching existing logic: if not displayable, value is empty
            String value = parent.isDisplayable() ? parent.getValue() : "";
            if (value == null) value = "";

            // Escape single quotes if we are going to wrap in quotes, 
            // but the original code doesn't seem to do advanced escaping
            expression = expression.replaceAll("\\$\\{" + name + "\\}", "'" + value + "'");
            
            parent = parent.getPreviousModel();
        }

        // Standard operator replacements from original ColumnView
        expression = expression.replaceAll("(?i)\\band\\b", "&&");
        expression = expression.replaceAll("(?i)\\bor\\b", "||");
        expression = expression.replaceAll("(?<![<>=!])=(?![=])", "==");
        expression = expression.replaceAll("<>", "!=");
        
        return expression;
    }

    private String translateMethodCalls(String expression) {
        List<String> methodCalls = getExpressionCalls(expression);

        for (String methodCall : methodCalls) {
            String[] methodArgs = getMethodArgs(methodCall);
            if (methodCallListener != null) {
                String result = methodCallListener.onCallMethod(methodCall, methodArgs);
                expression = expression.replace("call:" + methodCall, result != null ? result : "");
            }
        }

        return expression;
    }

    private List<String> getExpressionCalls(String expression) {
        List<String> list = new ArrayList<>();
        String callRegex = "call:(.*?)\\)";
        Pattern pattern = Pattern.compile(callRegex);
        Matcher matcher = pattern.matcher(expression);
        while (matcher.find()) {
            list.add(matcher.group(1) + ")");
        }
        return list;
    }

    private String[] getMethodArgs(String methodCall) {
        List<String> list = new ArrayList<>();
        String argRegex = "'(.*?)'";
        Pattern pattern = Pattern.compile(argRegex);
        Matcher matcher = pattern.matcher(methodCall);
        while (matcher.find()) {
            String arg = matcher.group(1);
            if ("null".equalsIgnoreCase(arg)) arg = null;
            list.add(arg);
        }
        return list.toArray(new String[0]);
    }
}
