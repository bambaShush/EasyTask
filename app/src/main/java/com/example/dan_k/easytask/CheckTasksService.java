package com.example.dan_k.easytask;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class CheckTasksService extends Service {
    private static String TAG = CheckTasksService.class.getName();
    private Handler myHandler;
    private Map<String, Task> taskMap;
    private FirebaseDatabase mDatabaseInstance;
    private DatabaseReference mUsersTasksRef;
    private LocationManager lm;
    private FusedLocationProviderClient mFusedLocationClient;
    private static final String LOGOUT = "logout";
    public static final String TASK_ID_KEY="taskId";
    public static final String NOTIFIED_ONLY_KEY="notifiedOnly";
    public static final int NO_VALUE=-1;
    public static final String NO_VALUE_STR="-1";
    private static final int DELAY_MILLIS=6000;
    private static final long METERS_FROM_DESTINATION=500;
    private static final long MAX_PASSED_MS_LCCATION_CHECK =1000*60*5;
    private static final long MAX_PASSED_MS =1000*60;
    public static String CHANNEL_ID = "Tasks channel";
    private boolean valueEventCancelled = false;
    private boolean justLoggedOut = false;
    private boolean userIsLoggedIn = false;
    private boolean mNeedLocationChecks=false;
    private Notification mNotifyNoLocation;
    private static final int NoLocationNotfyId = 68;
    private static final int SummaryNotfyId = 168;
    private static final String GROUP_KEY_TASKS="MY_TASKS";
    public static boolean fireNotif=false;
    private HandlerThread thread;
    private int mPassedMsLocationCheck =0;
    private int mPassedMs;
    private static final Uri NOTIFY_SOUND = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
    public CheckTasksService() {
    }

    private Handler jobHandler;
    private Runnable runnableCode = new Runnable() {

        @Override
        public void run() {
            try {
            if (!userIsLoggedIn) {
                userIsLoggedIn = initiateDataBase();
                if (userIsLoggedIn) {
                    getTasksData();
                }
            } else {
                if (valueEventCancelled)
                    getTasksData();
            }
            if (mNeedLocationChecks) {
                if (mPassedMsLocationCheck == 0) {
                    if (lm != null && !lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        fireNotification(NoLocationNotfyId, mNotifyNoLocation);
                        mPassedMsLocationCheck += DELAY_MILLIS;
                    }
                } else if (mPassedMsLocationCheck < MAX_PASSED_MS_LCCATION_CHECK) {
                    mPassedMsLocationCheck += DELAY_MILLIS;
                } else
                    mPassedMsLocationCheck = 0;
            }
//            if(mPassedMs<MAX_PASSED_MS)
//                mPassedMs+=DELAY_MILLIS;
//            else
//                stopSelf();
            fireNotificationsIfNeeded();
            jobHandler.postDelayed(runnableCode, DELAY_MILLIS);
            }
            catch (Exception ex){
                ex.printStackTrace();
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        try {


             thread = new HandlerThread("Thread name", android.os.Process.THREAD_PRIORITY_BACKGROUND);
            thread.start();
            Looper looper = thread.getLooper();
            myHandler = new OurHandler(looper);

            lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            this.mNotifyNoLocation = createNotification("location provider disabled", "please enable it", false, "");
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            jobHandler = new OurHandler(looper);
            jobHandler.post(runnableCode);
            this.taskMap = new HashMap<>();
            createNotificationChannel();

        }
        catch (Exception ex){
            Toast.makeText(getApplication(),ex.getMessage(),Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
        if(intent!=null) // May not have an Intent is the service was killed and restarted (See STICKY_SERVICE).
            justLoggedOut=intent.getBooleanExtra(CheckTasksService.getLogoutKey(),false);
        if(justLoggedOut){
            taskMap.clear();
            NotificationManagerCompat managerCompat = NotificationManagerCompat.from(this);
            managerCompat.cancelAll();
            mNeedLocationChecks=false;
        } else {
            initiateDataBase();
            getTasksData();
        }
        }
        catch (Exception ex){

        }
        return START_NOT_STICKY;
    }

    public class OurHandler extends Handler {
        public OurHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int startId = msg.arg1;
                //boolean stopped = stopSelfResult(startId);
                // stopped is true if the service is stopped

        }


    }



    @Override
    public void onDestroy() {
        thread.interrupt();
        thread.quitSafely();
        super.onDestroy();
    }

    private void getTasksData() {
        if(mUsersTasksRef==null)
            return;
        if(taskMap==null)
            taskMap=new HashMap<>();
        mUsersTasksRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount() > 0) {
                    for(DataSnapshot taskSnapShot:dataSnapshot.getChildren()) {
                        taskMap.put(taskSnapShot.getKey(),taskSnapShot.getValue(Task.class));
                    }
                }
                valueEventCancelled = false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                valueEventCancelled = true;
            }
        });
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Easy Task channel";
            String description = "notifications by task's time or location";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private Notification createNotification(String contentTitle, String contentText,boolean isGroup,String taskId) {
        int requestID = (int) System.currentTimeMillis();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.easy_task_notify)
                .setContentTitle(contentTitle)
                .setContentText(contentText)
                .setAutoCancel(false);
        if(isGroup){
            builder.setGroup(GROUP_KEY_TASKS);
            builder.setSound(NOTIFY_SOUND);

            Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            notificationIntent.putExtra(TASK_ID_KEY,taskId);
            PendingIntent contentIntent = PendingIntent.getActivity(this, requestID,notificationIntent, PendingIntent.FLAG_ONE_SHOT/*PendingIntent.FLAG_UPDATE_CURRENT*/);
            builder.setContentIntent(contentIntent);
        }

        return builder.build();
    }

    private void fireSummaryNotification(String contentTitle, String contentText,ArrayList<String>textLines){
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            int requestID = (int) System.currentTimeMillis();
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID);
                            builder.setContentTitle(contentTitle)
                            //set content text to support devices running API level < 24
                            .setContentText(contentText)
                            .setSmallIcon(R.drawable.easy_task_notify)
                            //specify which group this notification belongs to
                            .setGroup(GROUP_KEY_TASKS)
                            .setAutoCancel(false)
                            //set this notification as the summary for the group
                            .setGroupSummary(true).build();
            //build summary info into InboxStyle template
            NotificationCompat.InboxStyle style = new NotificationCompat.InboxStyle();
            for(String taskTitle : textLines){
                style.addLine(taskTitle);
            }
            builder.setStyle(style);
            Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            notificationIntent.putExtra(CheckTasksService.NOTIFIED_ONLY_KEY,true);
            PendingIntent contentIntent = PendingIntent.getActivity(this, requestID,notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentIntent(contentIntent);
            fireNotification(SummaryNotfyId,builder.build());
        }

    }



    private void fireNotification(int id, Notification notification) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        // notificationId is a unique int for each notification that you must define

        notificationManager.notify(id, notification);

    }


    private boolean initiateDataBase() {
        if(mUsersTasksRef!=null)
            return true;
        FirebaseUser user;
        if ((user = FirebaseAuth.getInstance().getCurrentUser()) == null)
            return false;
        if ((mDatabaseInstance = FirebaseUtils.getDatabase()) == null)
            return false;
        mUsersTasksRef = FirebaseUtils.getUserTasksRef(true);
        return true;
    }

    private void fireNotificationsIfNeeded() {
        ArrayList<String>notifiedList=new ArrayList<>();
        boolean hasLocation;
        boolean hasTime;
        boolean timePassed;
        boolean locationPassed;
        int countCurrentNotifications=0;
        Calendar calendar=Calendar.getInstance();
        if (taskMap == null)
            taskMap=new HashMap<>();
        Iterator<Map.Entry<String, Task>> it = taskMap.entrySet().iterator();
        mNeedLocationChecks=false;
        while (it.hasNext()) {
            Map.Entry<String, Task> taskEntry = it.next();
            Task task = taskEntry.getValue();
            hasLocation=task.getLocationLat()!=NO_VALUE;
            if(!mNeedLocationChecks)
                mNeedLocationChecks=  hasLocation && (!task.isNotified() && !task.isCompleted());
            hasTime=task.getTimeInMillis()!=NO_VALUE;
            if(task.isCompleted()){
                continue;
            }
            else if(task.isNotified()){
                   notifiedList.add(task.getTitle());
            }
            else {
                calendar.setTimeInMillis(task.getTimeInMillis());
                timePassed =hasTime? calendar.compareTo(Calendar.getInstance()) <= 0 : false;
                locationPassed=hasLocation ? isNearLocation(getCurrentLocation(),task.getLocationLat(),task.getLocationLng()):false;
                if ((locationPassed && timePassed) ||
                        (locationPassed && !hasTime) || (timePassed && !hasLocation)) {
                    fireNotification(getTaskIntegerId(taskEntry.getKey())
                            , createNotification(task.getTitle(), task.getDescription(), true,taskEntry.getKey()));
                    if (mUsersTasksRef != null) {
                        mUsersTasksRef.child(taskEntry.getKey()).child(Task.getNotifiedKey()).setValue(true);
                        task.setNotified(true);
                    }
                    countCurrentNotifications++;
                    notifiedList.add(task.getTitle());

                }
            }

        }
        if(countCurrentNotifications==notifiedList.size() && notifiedList.size()==1){
            NotificationManagerCompat managerCompat = NotificationManagerCompat.from(this);
            managerCompat.cancel(SummaryNotfyId);
        }else if(countCurrentNotifications>0){
                fireSummaryNotification(String.format("Easy Task: %s tasks waiting",notifiedList.size()),"click to watch",notifiedList);
        }

    }

    private Location getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return null;
        }
        Location dummy=new Location("dummy");
        dummy.setLatitude(0);
        dummy.setLongitude(0);
        final Location[] currentLocation = new Location[1];
        currentLocation[0]=dummy;
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                            currentLocation[0] =location;

                    }
                });
        while (dummy.equals(currentLocation[0])){

        }
        return currentLocation[0];
    }
    private boolean isNearLocation(Location currentLocation,double distanceLat,double distanceLng){
        Location newLocation=new Location("destinationLocation");
        newLocation.setLatitude(distanceLat);
        newLocation.setLongitude(distanceLng);
        if(currentLocation!=null) {
            float distance = currentLocation.distanceTo(newLocation);  //in meters
            if(distance<=METERS_FROM_DESTINATION)
                return true;
        }
        return false;
    }

    private int getTaskIntegerId(String taskId){
        StringBuilder sb = new StringBuilder();
        for (char c : taskId.toCharArray())
            sb.append((int)c);

        BigInteger taskIdInt = new BigInteger(sb.toString());
        return taskIdInt.intValue();
    }


    public static String getLogoutKey(){
        return CheckTasksService.LOGOUT;
    }

    public void appendLog(String text)
    {
        File logFile = new File("sdcard/log.file");
        if (!logFile.exists())
        {
            try
            {
                logFile.createNewFile();
            }
            catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        try
        {
            //BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            buf.append(text);
            buf.newLine();
            buf.close();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
