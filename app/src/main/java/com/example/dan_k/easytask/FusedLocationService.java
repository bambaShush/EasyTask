package com.example.dan_k.easytask;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class FusedLocationService extends Service  {
    private static boolean s_isRunning=false;
    private static final String TAG = FusedLocationService.class.getName();
    private static final int UPDATE_INTERVAL=5000;
    private static final int FASTEST_INTERVAL=5000;
    private static int s_counter=0;
    private final IBinder mBinder = new FusedLocationService.LocalBinder(); // Binder given to clients
    private LocationRequest mLocationRequest;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback mLocationCallback;
    private FirebaseDatabase mDatabaseInstance;
    private DatabaseReference mUsersTasksRef;
    private Looper looper;
    private MyServiceHandler myServiceHandler;
    public static String CHANNEL_ID="68";
    public class LocalBinder extends Binder {
        FusedLocationService getService() {
            // Return this instance of LocalService so clients can call public methods
            return FusedLocationService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();


//        mDatabaseInstance=FirebaseUtils.getDatabase();
//        mUsersTasksRef = mDatabaseInstance.getReference(String.format("/tasks/%s/", FirebaseAuth.getInstance().getCurrentUser().getUid()));

//        FusedLocationService.s_isRunning=true;
//        FusedLocationService.s_counter++;
//        createLocationRequest();
//        mLocationCallback = new LocationCallback() {
//            @Override
//            public void onLocationResult(LocationResult locationResult) {
//                if (locationResult == null) {
//                    return;
//                }
//                for (Location location : locationResult.getLocations()) {
//                    // Update UI with location data
//                    // ...
//                    Log.d(TAG, "counter:"+FusedLocationService.s_counter+"  "+location.toString());
//                }
//            }
//        };
//        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(FusedLocationService.this);
//
//        if (ActivityCompat.checkSelfPermission(FusedLocationService.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(FusedLocationService.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//
//        }
//        else {
//
//            mFusedLocationClient.getLastLocation()
//                    .addOnSuccessListener( new OnSuccessListener<Location>() {
//                        @Override
//                        public void onSuccess(Location location) {
//                            // Got last known location. In some rare situations this can be null.
//                            if (location != null) {
//
//                                // Logic to handle location object
//                            }
//                        }
//                    });
//        }
//
//        mFusedLocationClient.requestLocationUpdates(mLocationRequest,
//                mLocationCallback,
//                null /* Looper */);


//        HandlerThread handlerthread = new HandlerThread("MyThread", Process.THREAD_PRIORITY_BACKGROUND);
//        handlerthread.start();
//        looper = handlerthread.getLooper();
//        myServiceHandler = new MyServiceHandler(looper);
//        Message msg = myServiceHandler.obtainMessage();
//        msg.arg1 = FusedLocationService.s_counter;
//        myServiceHandler.sendMessage(msg);

    }

    private final class MyServiceHandler extends Handler {
        public MyServiceHandler(Looper looper) {
            super(looper);

        }
        @Override
        public void handleMessage(Message msg) {
            synchronized (this) {


            }

        }
    }

    private void createLocationRequest() {
        this.mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return  mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand");
        super.onStartCommand(intent, flags, startId);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        super.onDestroy();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Dan channel";
            String description = "no description";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public static  boolean isRunning(){
        return FusedLocationService.s_isRunning;
    }


    private Runnable myTask = new Runnable() {
        public void run() {
            // Do something here

        }
    };
}
