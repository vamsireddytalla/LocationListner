package com.example.locationlistner;

import java.util.List;

public class Constants
{
    static final int LOCATION_SERVICE_ID=101;
    static final String START_LOCATION_SERVICE="START_LOCATION_SERVICE";
    static final String STOP_LOCATION_SERVICE="STOP_LOCATION_SERVICE";
    static final String LOCATION_NOTIFICATION_CHANNEL_ID="LOCATION_NOTIFICATION_CHANNEL_ID";
    static final String LOCATION_NOTIFICATION_CHANNEL_NAME="LOCATION_NOTIFICATION_CHANNEL_NAME";


    public static double distance(double lat1, double lat2, double lon1, double lon2, double el1, double el2) {

        final int R = 6371; // Radius of the earth

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // convert to meters

        double height = el1 - el2;

        distance = Math.pow(distance, 2) + Math.pow(height, 2);

        return Math.sqrt(distance);
    }

}
