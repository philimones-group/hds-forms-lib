package org.philimone.hds.forms.main.testing;

import org.philimone.hds.forms.model.Column;
import org.philimone.hds.forms.model.ColumnGroup;
import org.philimone.hds.forms.model.HForm;
import org.philimone.hds.forms.model.enums.ColumnType;

import java.util.LinkedHashMap;
import java.util.Map;

public class FormModelFactory {

    public static HForm getTestForm(){

        HForm form = new HForm("rawMemberEnu", "Enumerate new Member");

        ColumnGroup headerGroup = new ColumnGroup();
        headerGroup.setHeader(true);
        headerGroup.setLabel("Preloaded Info");

        Map<String, String> genderMap = new LinkedHashMap<>();
        Map<String, String> gendersMap = new LinkedHashMap<>();

        genderMap.put("M", "Male");
        genderMap.put("F", "Female");

        gendersMap.put("M", "Male");
        gendersMap.put("F", "Female");
        gendersMap.put("X", "Xovine");
        gendersMap.put("Z", "Zamatra");



        Column cregion_code = new Column("region_code", ColumnType.STRING, null, "Region Code", "TXU", null, true, true);
        Column cregion_name = new Column("region_name", ColumnType.STRING, null, "Region Name", "Txumene", null, true, true);
        Column chouseh_code = new Column("household_code", ColumnType.STRING, null, "Household Code", "TXUPF1001", null, true, true);
        Column chouseh_name = new Column("household_name", ColumnType.STRING, null, "Household Name", "GUSTO JONAZE", null, true, true);
        Column cmember_code = new Column("member_code", ColumnType.STRING, null, "Member Code", "TXUPF1001001", null, true, true);
        Column cmember_name = new Column("member_name", ColumnType.STRING, null, "Member Name", null, null, true, false);
        Column cmember_gndr = new Column("member_gender", ColumnType.SELECT, genderMap, "Member Gender", null, null, true, false);
        Column cmember_gnds = new Column("member_genders", ColumnType.MULTI_SELECT, gendersMap, "Member Multi Gender", null, null, true, false);
        Column cmember_wght = new Column("member_weight", ColumnType.DECIMAL, null, "Member Weight", null, null, true, false);
        Column cmember_dob  = new Column("member_dob", ColumnType.DATE, null, "Member Date of Birth", null, null, true, false);
        Column chouseh_gps  = new Column("household_gps", ColumnType.GPS, null, "Household GPS", null, null, true, false);

        headerGroup.addColumn(cregion_code);
        headerGroup.addColumn(cregion_name);
        headerGroup.addColumn(chouseh_code);
        headerGroup.addColumn(chouseh_name);

        ColumnGroup g2 = getColumnGroup(cmember_code);
        ColumnGroup g3 = getColumnGroup(cmember_name);
        ColumnGroup g4 = getColumnGroup(cmember_gndr);
        ColumnGroup g5 = getColumnGroup(cmember_gnds);
        ColumnGroup g6 = getColumnGroup(cmember_wght);
        ColumnGroup g7 = getColumnGroup(cmember_dob);
        ColumnGroup g8 = getColumnGroup(chouseh_gps);

        form.addColumn(headerGroup);
        form.addColumn(g2);
        form.addColumn(g3);
        form.addColumn(g4);
        form.addColumn(g5);
        form.addColumn(g6);
        form.addColumn(g7);
        form.addColumn(g8);


        return form;
    }

    public static ColumnGroup getColumnGroup(Column column){
        ColumnGroup group = new ColumnGroup();

        group.addColumn(column);

        return group;
    }
}
