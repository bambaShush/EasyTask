package com.example.dan_k.easytask;

import android.Manifest;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;

import com.google.firebase.auth.FirebaseUser;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements LoginFragment.OnLoginListener,
        EditTaskFragment.OnSuccessAddingTaskListener,
        MainFragment.OnTaskClickedListener{
    private Fragment signInFragment;
    private FirebaseUser currentUser;
    private final static int RC=1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    RC);
        }



        getSupportFragmentManager().beginTransaction().replace(R.id.showFrag,new LoginFragment()).addToBackStack(null).commit();




    }
    @Override
    public void onStart() {
        super.onStart();
        getSupportActionBar().setTitle("Easy Task");
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onResume(){
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        Context ctx = getApplicationContext();
        stopService(new Intent(ctx, CheckTasksService.class));
        Intent intent = new Intent(ctx, CheckTasksService.class);
        PendingIntent pintent = PendingIntent.getService(ctx, CheckTasksService.SERVICE_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarm = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        alarm.setRepeating(AlarmManager.RTC_WAKEUP, Calendar.getInstance().getTimeInMillis(), 10*1000, pintent);

        super.onDestroy();
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the main_menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }


    @Override
    public void onLogin(FirebaseUser currentUser) {
        this.currentUser=currentUser;
        getSupportFragmentManager().popBackStack();
        boolean showNotifiedOnly=false;
        String taskId=null;

        if(getIntent().getExtras()!=null){
             showNotifiedOnly=getIntent().getExtras().getBoolean(CheckTasksService.NOTIFIED_ONLY_KEY,false);
             taskId=getIntent().getExtras().getString(CheckTasksService.TASK_ID_KEY,null);
        }

        if(showNotifiedOnly)
            getIntent().getExtras().remove(CheckTasksService.NOTIFIED_ONLY_KEY);
        gotoMainFrag(showNotifiedOnly);

        if (taskId != null) {
                getIntent().getExtras().remove(CheckTasksService.TASK_ID_KEY);
                gotoEditTaskFrag(taskId);
        }
    }

    @Override
    public void onSuccessAddingTask() {
        getSupportFragmentManager().popBackStack();
    }

    @Override
    public void onBackPressed() {
        if(getSupportFragmentManager().getBackStackEntryCount()==0)
            moveTaskToBack(true);
        else
            super.onBackPressed();
    }



    @Override
    public void onTaskClicked(String id) {
        gotoEditTaskFrag(id);
    }

    private void gotoEditTaskFrag(String id){
        EditTaskFragment editTaskFragment=new EditTaskFragment();
        Bundle args=new Bundle();
        args.putString(CheckTasksService.TASK_ID_KEY,id);
        editTaskFragment.setArguments(args);
        getSupportFragmentManager().beginTransaction().replace(R.id.showFrag,editTaskFragment).addToBackStack(null).commit();

    }
    private void gotoMainFrag(boolean showNotifiedOnly){
        MainFragment mainFragment=new MainFragment();
        Bundle args=new Bundle();
        args.putBoolean(CheckTasksService.NOTIFIED_ONLY_KEY,showNotifiedOnly);
        mainFragment.setArguments(args);
        getSupportFragmentManager().beginTransaction().replace(R.id.showFrag,mainFragment).commit();
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

}

