package org.philimone.hds.forms.parsers.form.model;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class FormOptions {
    private Map<String, LinkedHashMap<String, OptionValue>> mapOptions;

    public FormOptions() {
        this.mapOptions = new HashMap<>();
    }

    public void put(String optionName, String optionValue, String optionLabel, String optionReadonlyCondition, String optionDisplayCondition) {
        LinkedHashMap<String, OptionValue> map = this.mapOptions.get(optionName) == null ? new LinkedHashMap<>() : this.mapOptions.get(optionName) ;

        map.put(optionValue, new OptionValue(optionLabel, optionReadonlyCondition, optionDisplayCondition));

        this.mapOptions.put(optionName, map);
    }

    public Map<String, OptionValue> getOptions(String name) {
        return this.mapOptions.get(name);
    }

    public static class OptionValue {
        public String label;

        public String readonlyCondition;
        public boolean readonly = false;
        public String displayCondition;
        public boolean displayable = true; //default

        public OptionValue(String label, String readonlyCondition, String displayCondition) {
            this.label = label;
            this.readonlyCondition = readonlyCondition;
            this.displayCondition = displayCondition;
        }
    }

}
