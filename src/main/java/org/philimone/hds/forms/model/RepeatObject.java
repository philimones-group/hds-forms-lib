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
        Map<String,String> map = new LinkedHashMap<>();
        this.objectsList.add(map);
        return map;
    }

    public void addObject(Map<String, String> objectAsMap){
        this.objectsList = new ArrayList<>();
    }

    public List<Map<String, String>> getList() {
        return this.objectsList;
    }

    public String get(int index, String keyColumn){

        try {
            return objectsList.get(index).get(keyColumn);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public int count(){
        return this.objectsList.size();
    }
}
