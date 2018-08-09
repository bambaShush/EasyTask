package com.example.dan_k.easytask;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

public class LocationService extends Service
{
    private static final String TAG = LocationService.class.getName();
    private LocationManager mLocationManager = null;
    private static final int LOCATION_INTERVAL = 2000;
    private static final float LOCATION_DISTANCE = 0;
    private final IBinder mBinder = new LocalBinder(); // Binder given to clients
    private static final int NOTIFICATION_ID=1;
    private static final String CHANNEL_ID="1";
    private boolean notifiedAlready=false;
    NotificationCompat.Builder mBuilder;
    public class LocalBinder extends Binder {
        LocationService getService() {
            // Return this instance of LocalService so clients can call public methods
            return LocationService.this;
        }
    }

    private class LocationListener implements android.location.LocationListener {
        Location mLastLocation;

        /*private void showForegroundNotification(String contentText) {
            // Create intent that will bring our app to the front, as if it was tapped in the app
            // launcher
            Intent showTaskIntent = new Intent(getApplicationContext(), MainActivity.class);
            showTaskIntent.setAction(Intent.ACTION_MAIN);
            showTaskIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            showTaskIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            PendingIntent contentIntent = PendingIntent.getActivity(
                    getApplicationContext(),
                    0,
                    showTaskIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);

            Notification notification = new Notification.Builder(getApplicationContext())
                    .setContentTitle(getString(R.string.app_name))
                    .setContentText(contentText)
                    .setSmallIcon(R.drawable.common_google_signin_btn_icon_dark)
                    .setWhen(System.currentTimeMillis())
                    .setContentIntent(contentIntent)
                    .build();

            startForeground(NOTIFICATION_ID, notification);
        }
*/

        public LocationListener(String provider) {
            Log.e(TAG, "LocationListener " + provider);
            mLastLocation = new Location(provider);
        }

        @Override
        public void onLocationChanged(Location location){
            Log.e(TAG, "onLocationChanged: " + location);
            if(!notifiedAlready){
                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());

                // notificationId is a unique int for each notification that you must define
                notificationManager.notify(NOTIFICATION_ID, mBuilder.build());
            }
                //showForegroundNotification("onLocationChanged: " + location);
            mLastLocation.set(location);
        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.e(TAG, "onProviderDisabled: " + provider); //probably when location services are off
        }
        @Override
        public void onProviderEnabled(String provider) {
            Log.e(TAG, "onProviderEnabled: " + provider); //probably when location services are off
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras){
            Log.e(TAG, "onStatusChanged: " + provider);
        }
    }

    LocationListener[] mLocationListeners = new LocationListener[] {
            new LocationListener(LocationManager.GPS_PROVIDER),
            new LocationListener(LocationManager.NETWORK_PROVIDER)
    };

    @Override
    public IBinder onBind(Intent arg0) {
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand");
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onCreate() {
         mBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.common_google_signin_btn_icon_dark)
                .setContentTitle("textTitle")
                .setContentText("textContent")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);


        Log.e(TAG, "onCreate");
        initializeLocationManager();
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[1]);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "network provider does not exist, " + ex.getMessage());
        }
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[0]);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "gps provider does not exist " + ex.getMessage());
        }
    }

    @Override
    public void onDestroy() {
        Log.e(TAG, "onDestroy");
        super.onDestroy();
        if (mLocationManager != null) {
            for (int i = 0; i < mLocationListeners.length; i++) {
                try {
                    mLocationManager.removeUpdates(mLocationListeners[i]);
                } catch (Exception ex) {
                    Log.i(TAG, "fail to remove location listners, ignore", ex);
                }
            }
        }
    }

    private void initializeLocationManager() {
        Log.e(TAG, "initializeLocationManager");
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }

    /*private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }*/

}
