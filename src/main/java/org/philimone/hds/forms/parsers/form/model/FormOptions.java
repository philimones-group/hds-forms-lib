package org.philimone.hds.forms.parsers.form.model;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class FormOptions {
    private Map<String, LinkedHashMap<String, OptionValue>> mapOptions;

    public FormOptions() {
        this.mapOptions = new HashMap<>();
    }

    public void put(String optionName, String optionValue, String optionLabel, boolean optionReadonly) {
        LinkedHashMap<String, OptionValue> map = this.mapOptions.get(optionName) == null ? new LinkedHashMap<>() : this.mapOptions.get(optionName) ;

        map.put(optionValue, new OptionValue(optionLabel, optionReadonly));

        this.mapOptions.put(optionName, map);
    }

    public Map<String, OptionValue> getOptions(String name) {
        return this.mapOptions.get(name);
    }

    public static class OptionValue {
        public String label;
        public boolean readonly;

        public OptionValue(String label, boolean readonly) {
            this.label = label;
            this.readonly = readonly;
        }
    }

}
