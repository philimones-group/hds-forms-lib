package org.philimone.hds.forms.listeners;

import org.philimone.hds.forms.model.ColumnValue;
import org.philimone.hds.forms.model.HForm;
import org.philimone.hds.forms.model.ValidationResult;
import org.philimone.hds.forms.model.XmlFormResult;

import java.util.Map;

public interface FormCollectionListener {

    /*
     * Will be called after clicking save/finish
     */
    ValidationResult onFormValidate(HForm form, Map<String, ColumnValue> collectedValues);

    /*
     * will be called after validation
     */
    void onFormFinished(HForm form, Map<String, ColumnValue> collectedValues, XmlFormResult result);

    void onFormCancelled();

    String onFormCallMethod(String methodExpression, String[] args);

}
