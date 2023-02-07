package org.philimone.hds.forms.listeners;

public interface ExternalMethodCallListener {
    /**
     * Calls a method containing args and execute it, the result must explicit specify the value type by adding quotes if is a string or without it for numbers
     * @param methodExpression
     * @param args
     * @return
     */
    public String onCallMethod(String methodExpression, String[] args);
}
