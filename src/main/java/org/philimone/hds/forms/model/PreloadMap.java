package org.philimone.hds.forms.model;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

public class PreloadMap implements Serializable {
    private Map<String, Object> map;

    public PreloadMap(){
        this.map = new LinkedHashMap<>();
    }

    public PreloadMap(Map<String, String> map){
        this();
        putAll(map);
    }

    public void put(String keyColumn, String value){
        this.map.put(keyColumn, value);
    }

    public void put(String keyColumn, RepeatObject repeatObject){
        this.map.put(keyColumn, repeatObject);
    }

    public void putAll(PreloadMap preloadMap){
        this.map.putAll(preloadMap.map);
    }

    public void putAll(Map<String, String> mapValues){
        this.map.putAll(mapValues);
    }

    public boolean containsKey(String keyColumn){
        return this.map.containsKey(keyColumn);
    }

    public boolean isValueRepeatObject(String keyColumn){
        Object obj = this.map.get(keyColumn);

        return (obj != null) && (obj instanceof RepeatObject);
    }

    public String gets(String keyColumn) {
        return (String) this.map.get(keyColumn);
    }

    public String getStringValue(String keyColumn){
        Object obj = this.map.get(keyColumn);
        return obj == null ? null : obj.toString();
    }

    public RepeatObject getRepeatObject(String keyColumn){
        Object obj = this.map.get(keyColumn);
        return obj == null ? null : (RepeatObject) obj;
    }

    //List<Map<String,String>> --> RepeatObject

}
