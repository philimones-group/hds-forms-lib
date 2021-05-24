package org.philimone.hds.forms.model.enums;

import java.util.HashMap;
import java.util.Map;

public enum ColumnType {

    INTEGER ("integer"),
    DECIMAL ("decimal"),
    STRING ("string"),
    DATE ("date"),
    DATETIME ("datetime"),
    GPS ("gps"),
    SELECT ("select"),
    MULTI_SELECT ("multi_select"),
    TIMESTAMP ("timestamp"), /* Not visible*/
    START_TIMESTAMP ("start"),
    END_TIMESTAMP ("end"),
    DEVICE_ID ("device_id"),
    COLLECTED_BY ("username");
    //collectedBy
    //collectedDate

    private String code;

    ColumnType(String code){
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    /* Finding entity by code */
    private static final Map<String, ColumnType> TYPES = new HashMap<>();

    static {
        for (ColumnType e: values()) {
            TYPES.put(e.code, e);
        }
    }

    public static ColumnType getFrom(String code) {
        return TYPES.get(code);
    }


}
