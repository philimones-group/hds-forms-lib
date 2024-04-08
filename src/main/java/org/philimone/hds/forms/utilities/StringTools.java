package org.philimone.hds.forms.utilities;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class StringTools {

    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public static boolean isBlank(String value){
        return value==null || value.trim().isEmpty();
    }

    public static boolean isBlankDate(LocalDate value){
        return value==null;
    }

    public static boolean isBlankBoolean(Boolean value){
        return value==null;
    }

    public static boolean isBlankInteger(Integer value){
        return value==null;
    }

    public static boolean isBlankDouble(Double value){
        return value==null;
    }

    public static String format(Date date, String format){
        java.text.DateFormat formatter = new java.text.SimpleDateFormat(format);
        return formatter.format(date);
    }

    public static Date toDate(String date, String format){
        java.text.DateFormat formatter = new java.text.SimpleDateFormat(format);
        try {
            return formatter.parse(date);
        } catch (ParseException e) {
            return null;
        }
    }

    public static Date toDate(String date){
        java.text.DateFormat formatter = new java.text.SimpleDateFormat("yyyy-MM-dd");
        try {
            return formatter.parse(date);
        } catch (ParseException e) {
            return null;
        }
    }

    public static Date toDateTime(String date){
        java.text.DateFormat formatter = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            return formatter.parse(date);
        } catch (ParseException e) {
            return null;
        }
    }

    public static String format(Date date){
        if (date == null) return "null";
        java.text.DateFormat formatter = new java.text.SimpleDateFormat("yyyy-MM-dd");
        return formatter.format(date);
    }

    public static String formatLocalDate(LocalDate date){
        if (date == null) return "";
        //"yyyy-MM-dd"
        return date.format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    public static String format(LocalDate date, String format) {
        if (date == null) return "";

        DateTimeFormatter formatter = null;

        if (isBlank(format)){
            formatter = DateTimeFormatter.ISO_LOCAL_DATE;
        }else {
            formatter = DateTimeFormatter.ofPattern(format);
        }

        return date.format(formatter);
    }

    public static String format(LocalDateTime dateTime){
        if (dateTime == null) return "";

        //"yyyy-MM-dd"
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return dateTime.format(formatter);
    }

    public static String format(LocalDateTime dateTime, String format){
        if (dateTime == null) return "";

        DateTimeFormatter formatter = null;

        if (isBlank(format)){
            formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        }else {
            formatter = DateTimeFormatter.ofPattern(format);
        }

        return dateTime.format(formatter);
    }

    public static LocalDate toLocalDate(String dateString) {
        try {
            return LocalDate.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (Exception ex){
            //ex.printStackTrace();
            return null;
        }
    }

    public static LocalDateTime toLocalDateTime(String dateString) {
        try {
            return LocalDateTime.parse(dateString, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        } catch (Exception ex) {
            //ex.printStackTrace();
            return null;
        }
    }

    public static String formatUnderscoreDate(String collectedDate) {
        //yyyy-MM-dd_HH_mm_ss
        collectedDate = collectedDate.replaceAll(" ", "_");
        collectedDate = collectedDate.replaceAll(":", "_");

        return collectedDate;
    }

    public static boolean getBooleanValue(String booleanValue) {
        return booleanValue.equalsIgnoreCase("true") || booleanValue.equalsIgnoreCase("yes");
    }
}
