package org.philimone.hds.forms.model;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RepeatColumnValue extends ColumnValue { //find a better way to do this
    private String groupName;
    private String nodeName;
    private Map<Integer, Map<String, ColumnValue>> map = new LinkedHashMap<>();

    public RepeatColumnValue(String groupName, String nodeName) {
        this.groupName = groupName;
        this.nodeName = nodeName;
    }

    public String getGroupName() {
        return groupName;
    }

    public String getNodeName() {
        return nodeName;
    }

    public int getCount(){
        return map.size();
    }

    public ColumnValue get(String columnName, int repeatIndex){
        try {
            return map.get(repeatIndex).get(columnName);
        }catch (Exception ex){
            ex.printStackTrace();
        }

        return null;
    }

    public Collection<ColumnValue> getColumnValues(int repeatIndex){
        return this.map.get(repeatIndex).values();
    }

    public void put(Integer repeatGroupIndex, String columnName, ColumnValue columnValue){
        Map<String, ColumnValue> mapRep = map.get(repeatGroupIndex);
        mapRep = (mapRep!=null) ? mapRep : new LinkedHashMap<>();

        mapRep.put(columnName, columnValue);

        this.map.put(repeatGroupIndex, mapRep);
    }

    public void put(Integer repeatGroupIndex, ColumnValue columnValue){
        put(repeatGroupIndex, columnValue.getColumnName(), columnValue);
    }
}
