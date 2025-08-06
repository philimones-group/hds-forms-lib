package mz.betainteractive.utilities;

import com.ibm.icu.util.EthiopicCalendar;

import java.text.ParseException;
import java.util.Date;

/**
 * Date Utilities Method, made to support different date conversions and support agnostic date format
 * The methods formatYMD, formatYMDHMS, formatPrecise - are used when the class is instanciated so that can leverage from type of calendar
 */
public class DateUtil {
    public enum SupportedCalendar {GREGORIAN, ETHIOPIAN}

    private SupportedCalendar calendarType;

    public DateUtil() {
        this.calendarType = SupportedCalendar.GREGORIAN;
    }

    public DateUtil(SupportedCalendar calendarType) {
        this.calendarType = calendarType;
    }

    public String formatYMD(Date date) {
        switch (calendarType) {
            case GREGORIAN: return formatGregorianYMD(date);
            case ETHIOPIAN: return formatEthiopianYMD(date);
            default: return null;
        }
    }

    public String formatYMDHMS(Date date) {
        switch (calendarType) {
            case GREGORIAN: return formatGregorianYMDHMS(date);
            case ETHIOPIAN: return formatEthiopianYMDHMS(date);
            default: return null;
        }
    }

    public String formatYMDHMS(Date date, boolean underscored) {
        switch (calendarType) {
            case GREGORIAN: return formatGregorianYMDHMS(date, underscored);
            case ETHIOPIAN: return formatEthiopianYMDHMS(date, underscored);
            default: return null;
        }
    }

    public String formatPrecise(Date date) {
        switch (calendarType) {
            case GREGORIAN: return formatGregorianPrecise(date);
            case ETHIOPIAN: return formatEthiopianPrecise(date);
            default: return null;
        }
    }

    /**
     * Prints a formatted date as yyyyMMdd-HHmmss.SSS - special code to be used as a timestamp code
     * @param date
     * @return
     */
    public String formatSpecialCode(Date date) {
        switch (calendarType) {
            case GREGORIAN: return formatGregorianSpecialCode(date);
            case ETHIOPIAN: return formatEthiopianSpecialCode(date);
            default: return null;
        }
    }


    /* Gregorian to Date - these methods should be static */
    public static Date toDate(String date, String format){
        if (date==null) return null;

        java.text.DateFormat formatter = new java.text.SimpleDateFormat(format);
        try {
            return formatter.parse(date);
        } catch (ParseException e) {
            return null;
        }
    }

    public static Date toDateYMD(String date){
        return toDate(date, "yyyy-MM-dd");
    }

    public static Date toDatePrecise(String date){
        if (date==null) return null;

        java.text.DateFormat formatter = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        try {
            return formatter.parse(date);
        } catch (ParseException e) {
            return null;
        }
    }

    public static Date toDateYMDHMS(String date){
        java.text.DateFormat formatter = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            return formatter.parse(date);
        } catch (ParseException e) {
            return null;
        }
    }

    public static String formatGregorian(Date date, String format) {
        if (date==null) return null;
        java.text.DateFormat formatter = new java.text.SimpleDateFormat(format);
        return formatter.format(date);
    }

    public static String formatGregorianYMD(Date date){
        if (date==null) return null;
        java.text.DateFormat formatter = new java.text.SimpleDateFormat("yyyy-MM-dd");
        return formatter.format(date);
    }

    public static String formatGregorianYMDHMS(Date date){
        if (date==null) return null;
        java.text.DateFormat formatter = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return formatter.format(date);
    }

    public static String formatGregorianYMDHMS(Date date, boolean underscored){
        if (date==null) return null;
        java.text.DateFormat formatter = new java.text.SimpleDateFormat(underscored ? "yyyy-MM-dd_HH_mm_ss" : "yyyy-MM-dd HH:mm:ss");
        return formatter.format(date);
    }

    public static String formatGregorianPrecise(Date date){
        if (date==null) return null;
        java.text.DateFormat formatter = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        return formatter.format(date);
    }

    public static String formatGregorianSpecialCode(Date date){
        if (date==null) return null;
        java.text.DateFormat formatter = new java.text.SimpleDateFormat("yyyyMMdd-HHmmss.SSS");
        return formatter.format(date);
    }

    public static String formatEthiopianYMD(java.util.Date date) {

        EthiopicCalendar ethiopic = toEthiopianCalendar(date);

        Integer year = ethiopic.get(EthiopicCalendar.YEAR);
        Integer month = ethiopic.get(EthiopicCalendar.MONTH) + 1; // 0-indexed
        Integer day = ethiopic.get(EthiopicCalendar.DATE);

        return String.format("%04d-%02d-%02d EC", year, month, day);
    }

    public static String formatEthiopianYMDHMS(java.util.Date date) {

        EthiopicCalendar ethiopic = toEthiopianCalendar(date);

        Integer year = ethiopic.get(EthiopicCalendar.YEAR);
        Integer month = ethiopic.get(EthiopicCalendar.MONTH) + 1; // 0-indexed
        Integer day = ethiopic.get(EthiopicCalendar.DATE);
        Integer hour = ethiopic.get(EthiopicCalendar.HOUR_OF_DAY);
        Integer minute = ethiopic.get(EthiopicCalendar.MINUTE);
        Integer second = ethiopic.get(EthiopicCalendar.SECOND);

        return String.format("%04d-%02d-%02d %02d:%02d:%02d EC", year, month, day, hour, minute, second);
    }

    public static String formatEthiopianYMDHMS(java.util.Date date, boolean underscored) {

        EthiopicCalendar ethiopic = toEthiopianCalendar(date);

        Integer year = ethiopic.get(EthiopicCalendar.YEAR);
        Integer month = ethiopic.get(EthiopicCalendar.MONTH) + 1; // 0-indexed
        Integer day = ethiopic.get(EthiopicCalendar.DATE);
        Integer hour = ethiopic.get(EthiopicCalendar.HOUR_OF_DAY);
        Integer minute = ethiopic.get(EthiopicCalendar.MINUTE);
        Integer second = ethiopic.get(EthiopicCalendar.SECOND);

        if (underscored) {
            return String.format("%04d-%02d-%02d_%02d_%02d_%02d_EC", year, month, day, hour, minute, second);
        } else {
            return String.format("%04d-%02d-%02d %02d:%02d:%02d EC", year, month, day, hour, minute, second);
        }
    }

    public static String formatEthiopianPrecise(java.util.Date date) {

        EthiopicCalendar ethiopic = toEthiopianCalendar(date);

        Integer year = ethiopic.get(EthiopicCalendar.YEAR);
        Integer month = ethiopic.get(EthiopicCalendar.MONTH) + 1; // 0-indexed
        Integer day = ethiopic.get(EthiopicCalendar.DATE);
        Integer hour = ethiopic.get(EthiopicCalendar.HOUR_OF_DAY);
        Integer minute = ethiopic.get(EthiopicCalendar.MINUTE);
        Integer second = ethiopic.get(EthiopicCalendar.SECOND);
        Integer millisec = ethiopic.get(EthiopicCalendar.MILLISECONDS_IN_DAY);

        return String.format("%04d-%02d-%02d %02d:%02d:%02d.%04d EC", year, month, day, hour, minute, second, millisec);
    }

    public static String formatEthiopianSpecialCode(java.util.Date date) {

        EthiopicCalendar ethiopic = toEthiopianCalendar(date);

        Integer year = ethiopic.get(EthiopicCalendar.YEAR);
        Integer month = ethiopic.get(EthiopicCalendar.MONTH) + 1; // 0-indexed
        Integer day = ethiopic.get(EthiopicCalendar.DATE);
        Integer hour = ethiopic.get(EthiopicCalendar.HOUR_OF_DAY);
        Integer minute = ethiopic.get(EthiopicCalendar.MINUTE);
        Integer second = ethiopic.get(EthiopicCalendar.SECOND);
        Integer millisec = ethiopic.get(EthiopicCalendar.MILLISECONDS_IN_DAY);

        //yyyyMMdd-HHmmss.SSS
        return String.format("%04d%02d%02d-%02d%02d%02d.%04d_EC", year, month, day, hour, minute, second, millisec);
    }

    public static EthiopicCalendar toEthiopianCalendar(java.util.Date date) {
        EthiopicCalendar ethiopic = new EthiopicCalendar();
        ethiopic.setTime(date);
        return ethiopic;
    }

    /**
     *
     * @param year
     * @param month the month is zero-based
     * @param day
     * @param hh
     * @param mm
     * @param ss
     * @return
     */
    public static EthiopicCalendar toEthiopianCalendar(int year, int month, int day, int hh, int mm, int ss) {
        EthiopicCalendar ethiopic = new EthiopicCalendar();
        ethiopic.set(year, month, day, hh, mm, ss);
        return ethiopic;
    }

}
