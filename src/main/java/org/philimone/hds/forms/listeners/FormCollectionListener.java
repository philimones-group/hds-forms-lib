package org.philimone.hds.forms.listeners;

import org.philimone.hds.forms.model.ColumnValue;
import org.philimone.hds.forms.model.HForm;
import org.philimone.hds.forms.model.ValidationResult;
import org.philimone.hds.forms.model.XmlFormResult;

import java.util.List;

public interface FormCollectionListener {

    /*
     * Will be called after clicking save/finish
     */
    ValidationResult onFormValidate(HForm form, List<ColumnValue> collectedValues);

    /*
     * will be called after validation
     */
    void onFormFinished(HForm form, List<ColumnValue> collectedValues, XmlFormResult result);

}
