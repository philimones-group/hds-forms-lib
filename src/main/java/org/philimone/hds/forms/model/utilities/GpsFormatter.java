package org.philimone.hds.forms.model.utilities;

import java.util.Locale;
import java.util.Map;
import mz.betainteractive.utilities.StringUtil;

public class GpsFormatter {

    private Double latitude, longitude, altitude, accuracy;

    public GpsFormatter(Double latitude, Double longitude, Double altitude, Double accuracy) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
        this.accuracy = accuracy;
    }

    public GpsFormatter(String columnName, Map<String,Double> gpsValues) {
        this.latitude = gpsValues.get(columnName+"Lat");
        this.longitude = gpsValues.get(columnName+"Lon");
        this.altitude = gpsValues.get(columnName+"Alt");
        this.accuracy = gpsValues.get(columnName+"Acc");
    }

    public static String format(Double latitude, Double longitude, Double altitude, Double accuracy){
        return new GpsFormatter(latitude, longitude, altitude, accuracy).format();
    }

    public static String formatDMS(String columnName, Map<String, Double> gpsValues) {
        return new GpsFormatter(columnName, gpsValues).formatDMS();
    }

    public String format(){
        //String lat = "" + latitude;
        //String lon = "" + longitude;
        return latitude + ", " + longitude + ", alt: "+altitude+", acc: "+accuracy;
    }

    public String formatDMS(){
        String lat = getLatitudeAsDMS(latitude,2);
        String lon = getLongitudeAsDMS(longitude, 2);

        return lat + ", " + lon + ", alt: "+altitude+", acc: "+accuracy;
    }

    private String getLatitudeAsDMS(Double latitude, int decimalPlace){
        return convertComponent(latitude, true);
    }

    private String getLongitudeAsDMS(Double longitude, int decimalPlace){
        return convertComponent(longitude, false);
    }

    private String convertComponent(double coordinate, boolean isLat) {
        String direction = isLat ? (coordinate >= 0 ? "N" : "S") : (coordinate >= 0 ? "E" : "W");
        double abs = Math.abs(coordinate);

        int degrees = (int) abs;
        double minutesRemainder = (abs - degrees) * 60;
        int minutes = (int) minutesRemainder;
        double seconds = (minutesRemainder - minutes) * 60;

        return String.format(Locale.US, "%d°%d'%.1f\"%s", degrees, minutes, seconds, direction);
    }

    public static Double[] getValuesFrom(String formattedGps) {

        if (StringUtil.isBlank(formattedGps)) return null;

        Double[] values = new Double[4];
        formattedGps = formattedGps.replaceAll("alt: ", "");
        formattedGps = formattedGps.replaceAll("acc: ", "");

        String[] splitted = formattedGps.split(", ");

        try {
            values[0] = Double.parseDouble(splitted[0]);
            values[1] = Double.parseDouble(splitted[1]);
            values[2] = Double.parseDouble(splitted[2]);
            values[3] = Double.parseDouble(splitted[3]);
        }catch (Exception exception) {
            exception.printStackTrace();

            return null;
        }

        return values;
    }

    private String replaceDelimiters(String str, int decimalPlace) {
        str = str.replaceFirst(":", "°");
        str = str.replaceFirst(":", "'");
        int pointIndex = str.indexOf(".");
        int endIndex = pointIndex + 1 + decimalPlace;
        if(endIndex < str.length()) {
            str = str.substring(0, endIndex);
        }
        str = str + "\"";
        return str;
    }
}