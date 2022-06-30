package com.example.locationlistner;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationProvider;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

public class LocationBgTracker extends Service {

    private FusedLocationProviderClient fusedLocationProviderClient;
    private MutableLiveData<String> latlongMutableData;
    public static LiveData<String> liveData;
    private NotificationManager notificationManager;
    private NotificationCompat.Builder notificationBuilder;

    @Override
    public void onCreate() {
        super.onCreate();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationBuilder=notificationBuilder();
        if (latlongMutableData == null) {
            latlongMutableData = new MutableLiveData<String>();
            latlongMutableData.setValue("Default Value");
            liveData = latlongMutableData;
        }
    }

    private LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationAvailability(@NonNull LocationAvailability locationAvailability) {
            super.onLocationAvailability(locationAvailability);
        }

        @Override
        public void onLocationResult(@NonNull LocationResult locationResult) {
            super.onLocationResult(locationResult);
            if (locationResult != null && locationResult.getLastLocation() != null) {
                double lat = locationResult.getLastLocation().getLatitude();
                double longi = locationResult.getLastLocation().getLongitude();
                Log.d("Location_Result", lat + "  " + longi + " ");
                double dist = Constants.distance(17.4327999, lat, 78.3868092, longi, 0.00, 0.00);
                updateNotificationPeriodically(checkOut(dist));
                latlongMutableData.setValue(checkOut(dist));
            }
        }
    };

    private String checkOut(double dist){
        String msg="Location Tracking";
        if (Math.round(dist)>10){
            msg="Checked Out";
            stopLocationUpdate();
            stopSelf();
        }else {
            msg=Math.round(dist)+" Meters";
        }
        return msg;
    }

    public void startLocationService() {

        createNotificationChannel();

        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(4000);
        locationRequest.setFastestInterval(2000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());

    }

    private void stopLocationUpdate() {
        removeLocationUpdates();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if (action != null) {
                if (action.equals(Constants.START_LOCATION_SERVICE)) {
                    startLocationService();
                } else if (action.equals(Constants.STOP_LOCATION_SERVICE)) {
                    stopLocationUpdate();
                }
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (notificationManager != null && notificationManager.getNotificationChannel(Constants.LOCATION_NOTIFICATION_CHANNEL_ID) == null) {
                NotificationChannel notificationChannel = new NotificationChannel(Constants.LOCATION_NOTIFICATION_CHANNEL_ID, Constants.LOCATION_NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW);
                notificationChannel.setDescription("This channel is used by notification service");
                notificationManager.createNotificationChannel(notificationChannel);
            }
        }
        startForeground(Constants.LOCATION_SERVICE_ID, notificationBuilder.build());
    }

    public NotificationCompat.Builder notificationBuilder() {
        Intent intent = new Intent();
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), Constants.LOCATION_NOTIFICATION_CHANNEL_ID);
        builder.setSmallIcon(R.drawable.location_icon);
        builder.setContentTitle("Location Service");
        builder.setDefaults(NotificationCompat.DEFAULT_ALL);
        builder.setContentText("Running");
        builder.setContentIntent(pendingIntent);
        builder.setAutoCancel(false);
        builder.setPriority(NotificationCompat.PRIORITY_MAX);
        return builder;
    }

    private void updateNotificationPeriodically(String res) {
        notificationBuilder.setContentTitle("Location in Distance");
        notificationBuilder.setContentText(res);
        notificationManager.notify(Constants.LOCATION_SERVICE_ID, notificationBuilder.build());
    }

    private void removeLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        ((NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE)).cancel(Constants.LOCATION_SERVICE_ID);
        stopForeground(true);
        stopSelf();
    }



}
