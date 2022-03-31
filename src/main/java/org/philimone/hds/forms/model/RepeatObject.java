package org.philimone.hds.forms.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RepeatObject implements Serializable {
    private List<Map<String, String>> objectsList;

    public RepeatObject(){
        this.objectsList = new ArrayList<>();
    }

    public Map<String, String> createNewObject(){
        return new LinkedHashMap<>();
    }

    public void addObject(Map<String, String> objectAsMap){
        this.objectsList = new ArrayList<>();
    }

    public List<Map<String, String>> getList() {
        return this.objectsList;
    }

    public int count(){
        return this.objectsList.size();
    }
}
