package org.philimone.hds.forms.parsers.form.model;

import java.util.LinkedHashMap;
import java.util.Map;

public class FormSettings {
    public String formId;
    public String formName;
    public String formVersion;
    public Map<String, String> formRepeatNodes = new LinkedHashMap<>();

    public FormSettings(String formId, String formName, String formVersion) {
        this.formId = formId;
        this.formName = formName;
        this.formVersion = formVersion;
    }

    public FormSettings(String formId, String formName, String formVersion, Map<String,String> nameNodes) {
        this.formId = formId;
        this.formName = formName;
        this.formVersion = formVersion;
        this.formRepeatNodes.putAll(nameNodes);
    }

    public String getRepeatNodeName(String groupName){
        return this.formRepeatNodes.get(groupName);
    }
}
