package org.philimone.hds.forms.model;

import org.philimone.hds.forms.R;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class CollectedDataMap {
    private Map<String, ColumnValue> map = new LinkedHashMap<String, ColumnValue>();
    //private Map<String, RepeatColumnValue> mapRepeats = new LinkedHashMap<>();

    public void put(ColumnValue columnValue) {
        map.put(columnValue.getColumnName(), columnValue);
    }

    public void put(String columnName, ColumnValue columnValue) {
        map.put(columnName, columnValue);
    }

    public void put(String columnName, RepeatColumnValue repeatColumnValue) {
        //mapRepeats.put(columnName, repeatColumnValue);
        map.put(columnName, repeatColumnValue);
    }

    public void put(RepeatColumnValue repeatColumnValue) {
        //mapRepeats.put(repeatColumnValue.getGroupName(), repeatColumnValue);
        map.put(repeatColumnValue.getGroupName(), repeatColumnValue);
    }

    public Collection<ColumnValue> values() {
        return map.values();
    }

    /*
    public Collection<RepeatColumnValue> repeatValues(){
        return mapRepeats.values();
    }
    */

    public ColumnValue get(String columnName) {
        return this.map.get(columnName);
    }

    public RepeatColumnValue getRepeatColumn(String repeatGroupName) {
        //return this.mapRepeats.get(repeatGroupName);
        ColumnValue cv = this.map.get(repeatGroupName);
        if (cv instanceof RepeatColumnValue) {
            return (RepeatColumnValue) cv;
        }
        return null;
    }
}
