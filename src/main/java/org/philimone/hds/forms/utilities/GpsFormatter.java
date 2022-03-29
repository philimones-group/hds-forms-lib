package org.philimone.hds.forms.utilities;

import android.location.Location;

import androidx.annotation.NonNull;

public class GpsFormatter {

    private Double latitude, longitude, altitude, accuracy;

    public GpsFormatter(Double latitude, Double longitude, Double altitude, Double accuracy) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
        this.accuracy = accuracy;
    }

    public static String format(Double latitude, Double longitude, Double altitude, Double accuracy){
        return new GpsFormatter(latitude, longitude, altitude, accuracy).format();
    }

    public String format(){
        String lat = getLatitudeAsDMS(latitude,2);
        String lon = getLongitudeAsDMS(longitude, 2);

        return lat + ", " + lon + ", alt: "+altitude+", acc: "+accuracy;
    }

    public static Double[] getValuesFrom(String formattedGps) {

        if (StringTools.isBlank(formattedGps)) return null;

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

    private String getLatitudeAsDMS(Double latitude, int decimalPlace){
        String strLatitude = Location.convert(latitude, Location.FORMAT_SECONDS);
        strLatitude = replaceDelimiters(strLatitude, decimalPlace);
        strLatitude = strLatitude + " N";
        return strLatitude;
    }

    private String getLongitudeAsDMS(Double longitude, int decimalPlace){
        String strLongitude = Location.convert(longitude, Location.FORMAT_SECONDS);
        strLongitude = replaceDelimiters(strLongitude, decimalPlace);
        strLongitude = strLongitude + " W";
        return strLongitude;
    }

    @NonNull
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