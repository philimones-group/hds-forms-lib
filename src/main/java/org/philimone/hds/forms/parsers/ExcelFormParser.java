package org.philimone.hds.forms.parsers;

import android.util.Log;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.philimone.hds.forms.model.Column;
import org.philimone.hds.forms.model.ColumnGroup;
import org.philimone.hds.forms.model.HForm;
import org.philimone.hds.forms.model.enums.ColumnType;
import org.philimone.hds.forms.utilities.StringTools;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class ExcelFormParser implements FormParser {

    private File excelFile;
    private HForm form;
    private String language;

    public ExcelFormParser(File excelFile) {
        this.excelFile = excelFile;
        this.language = Locale.getDefault().getLanguage();

        try {
            FileInputStream fileInputStream = new FileInputStream(excelFile);
            this.form = parseFromFile(fileInputStream);
        } catch (Exception ex){

        }
    }

    public ExcelFormParser(InputStream inputStream) {
        this.language = Locale.getDefault().getLanguage();

        try {
            this.form = parseFromFile(inputStream);
        } catch (Exception ex){

        }
    }

    private HForm parseFromFile(InputStream inputStream) {

        //get to rows and columns

        try {

            XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
            XSSFSheet sheet_columns = workbook.getSheetAt(0);

            FormSettings settings = getFormSettings(workbook);
            FormOptions options = getFormOptions(workbook);
            Map<String, Integer> mapHeaderIndex = getRowHeaders(sheet_columns);
            Map<String, Integer> mapLocalizedCellIndex = getLocalizedCellsIndex(sheet_columns);


            int group_index = mapHeaderIndex.get("group");
            int group_label_index = mapLocalizedCellIndex.get("group_label")==null ? mapHeaderIndex.get("group_label") : mapLocalizedCellIndex.get("group_label");
            int name_index = mapHeaderIndex.get("name");
            int type_index = mapHeaderIndex.get("type");
            int options_index = mapHeaderIndex.get("options");
            int label_index = mapLocalizedCellIndex.get("label")==null ? mapHeaderIndex.get("label") : mapLocalizedCellIndex.get("label");
            int default_value_index = mapHeaderIndex.get("default_value");
            int required_index = mapHeaderIndex.get("required");
            int readonly_index = mapHeaderIndex.get("readonly");
            int display_index = mapHeaderIndex.get("display_condition");


            HForm form = new HForm(settings.formId, settings.formName);

            Map<String, ColumnGroup> mapGroup = new LinkedHashMap<>();

            for (Row row : sheet_columns) {
                if (row.getRowNum() != 0) {
                    String cellGroup = getCellValue(row.getCell(group_index));
                    String cellGroupLabel = getCellValue(row.getCell(group_label_index));
                    String cellName = getCellValue(row.getCell(name_index));
                    String cellType = getCellValue(row.getCell(type_index));
                    String cellOptions = getCellValue(row.getCell(options_index));
                    String cellLabel = getCellValue(row.getCell(label_index));
                    String defaultValue = getCellValue(row.getCell(default_value_index));
                    String cellRequired = getCellValue(row.getCell(required_index));
                    String cellReadonly = getCellValue(row.getCell(readonly_index));
                    String cellDisplay = getCellValue(row.getCell(display_index));

                    if (cellName == null || cellName.isEmpty()) continue;

                    ColumnGroup group = mapGroup.get(cellGroup);

                    if (group == null) {
                        group = new ColumnGroup();
                        group.setHeader(cellGroup.equalsIgnoreCase("header"));
                        group.setName(cellGroup);
                        group.setLabel(cellGroupLabel);

                        if (!StringTools.isBlank(group.getName())) {
                            mapGroup.put(group.getName(), group);
                        }
                    }

                    Column column = new Column(cellName, ColumnType.getFrom(cellType), options.getOptions(cellOptions), cellLabel, defaultValue, getBooleanValue(cellRequired), getBooleanValue(cellReadonly), cellDisplay);
                    group.addColumn(column);

                    form.addColumn(group);

                }
            }

            //close file
            if (inputStream != null){
                inputStream.close();
            }

            return form;

        }catch (Exception ex){
            ex.printStackTrace();
        }

        return null;
    }

    private boolean getBooleanValue(String booleanValue) {
        return booleanValue.equalsIgnoreCase("true") || booleanValue.equalsIgnoreCase("yes");
    }

    public ColumnGroup getColumnGroup(Column column){
        ColumnGroup group = new ColumnGroup();

        group.addColumn(column);

        return group;
    }

    private String getCellValue(Cell cell) {
        DataFormatter formatter = new DataFormatter();
        return formatter.formatCellValue(cell);
    }

    private FormOptions getFormOptions(XSSFWorkbook workbook) {

        FormOptions formOptions = new FormOptions();

        XSSFSheet sheet1 = workbook.getSheetAt(1); //options

        Map<String, Integer> mapHeaderIndex = getRowHeaders(sheet1);
        Map<String, Integer> mapLocaleCellIndex = getLocalizedCellsIndex(sheet1);
        Integer defaultLabelIndex = mapHeaderIndex.get("label");
        Integer localizLabelIndex = mapLocaleCellIndex.get("label");
        int labelIndex = (localizLabelIndex==null) ? defaultLabelIndex : localizLabelIndex;

        for (Row row : sheet1) {

            if (row.getRowNum() == 0) continue;

            Cell cellName = row.getCell(0);
            Cell cellValue = row.getCell(1);
            Cell cellLabel = row.getCell(labelIndex);

            String name = getCellValue(cellName);
            String value = getCellValue(cellValue);
            String label = getCellValue(cellLabel);

            formOptions.put(name, value, label);

        }

        return formOptions;
    }

    private FormSettings getFormSettings(XSSFWorkbook workbook) throws Exception {

        XSSFSheet sheet2 = workbook.getSheetAt(2); //settings

        //read settings
        Map<String, Integer> mapHeaderIndex = getRowHeaders(sheet2);
        Map<String, Integer> mapLocaleCellIndex = getLocalizedCellsIndex(sheet2);
        Integer localizableFormNameIndex = mapLocaleCellIndex.get("form_name");
        Integer defaultFormNameIndex = mapHeaderIndex.get("form_name");
        Integer formNameIndex = (localizableFormNameIndex==null) ? defaultFormNameIndex : localizableFormNameIndex;

        if (formNameIndex == null) {
            throw new Exception("There is no default 'form_name'");
        }

        XSSFRow rowValues = sheet2.getRow(1);

        String formId = rowValues.getCell(0).getStringCellValue();
        String formName = getCellValue(rowValues.getCell(formNameIndex));

        return new FormSettings(formId, formName);
    }

    /*
     * Get map with, columns header name and cell index
     */
    private Map<String, Integer> getRowHeaders(XSSFSheet sheet){

        Map<String, Integer> map = new LinkedHashMap<>();

        XSSFRow headerRow = sheet.getRow(0);

        headerRow.forEach( cell -> {
            String cellValue = cell.getStringCellValue();

            if (cellValue != null) {
                map.put(cellValue, cell.getColumnIndex());
            }
        });

        return map;
    }

    /*
     * Get map with, columns header name/identifier with their respective index in the row
     */
    private Map<String, Integer> getLocalizedCellsIndex(XSSFSheet sheet){

        Map<String, Integer> map = new LinkedHashMap<>();

        XSSFRow headerRow = sheet.getRow(0);

        headerRow.forEach( cell -> {

            String cellValue = cell.getStringCellValue();
            Log.d("tag", ""+cellValue);

            if (cellValue != null && cellValue.endsWith("::"+language)) {
                String[] values = cellValue.split("::");

                map.put(values[0], cell.getColumnIndex());
            }
        });

        return map;
    }

    @Override
    public HForm getForm() {
        return this.form;
    }

    class FormOptions {
        private Map<String, LinkedHashMap<String, String>> mapOptions;

        public FormOptions() {
            this.mapOptions = new HashMap<>();
        }

        public void put(String optionName, String optionValue, String optionLabel) {
            LinkedHashMap<String, String> map = this.mapOptions.get(optionName) == null ? new LinkedHashMap<>() : this.mapOptions.get(optionName) ;

            map.put(optionValue, optionLabel);

            this.mapOptions.put(optionName, map);
        }

        public Map<String, String> getOptions(String name) {
            return this.mapOptions.get(name);
        }

    }

    class FormSettings {
        public String formId;
        public String formName;

        public FormSettings(String formId, String formName) {
            this.formId = formId;
            this.formName = formName;
        }
    }


}
