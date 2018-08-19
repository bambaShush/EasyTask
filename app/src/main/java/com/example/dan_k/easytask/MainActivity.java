package com.example.dan_k.easytask;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;

import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity implements LoginFragment.OnLoginListener,
        EditTaskFragment.OnSuccessAddingTaskListener,
        MainFragment.OnTaskClickedListener{
    private Fragment signInFragment;
    private FirebaseUser currentUser;
    private TasksService mService;
    private boolean mBound = false;
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
        //When you enable disk persistence, your app writes the data locally to the device so your app can maintain state
        // while offline, even if the user or operating system restarts the app.


        //startActivity(new Intent(this,LocationActivity.class));
        getSupportFragmentManager().beginTransaction().replace(R.id.showFrag,new LoginFragment()).addToBackStack(null).commit();




//        getSupportFragmentManager().beginTransaction().replace(R.id.showFrag,new MainFragment()).commit();

    }
    @Override
    public void onStart() {
        super.onStart();

//        if(!mBound) {
//            Intent intent = new Intent(this, TasksService.class);
//            bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
//        }
//            if(!FusedLocationService.isRunning())
//                startService(new Intent(this, FusedLocationService.class));



            getSupportActionBar().setTitle("Easy Task");

        String taskId= getIntent().getExtras()!=null? getIntent().getExtras().getString(MyService.TASK_ID_KEY):null;
        if(taskId!=null && !taskId.equals(EditTaskFragment.EMPTY_STR)){
            getIntent().getExtras().remove(MyService.TASK_ID_KEY);
            GotoEditTaskFrag(taskId);
        }

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
        super.onDestroy();

        if(mService!=null)
            unbindService(mConnection);
        mBound = false;
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the main_menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }
//
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case R.id.action_add:
//                getSupportFragmentManager().beginTransaction().replace(R.id.showFrag,new EditTaskFragment()).addToBackStack(null).commit();
//                return true;
//
//            case R.id.action_favorite:
//                // User chose the "Favorite" action, mark the current item
//                // as a favorite...
//                return true;
//
//            default:
//                // If we got here, the user's action was not recognized.
//                // Invoke the superclass to handle it.
//                return super.onOptionsItemSelected(item);
//
//        }
//    }

    @Override
    public void onLogin(FirebaseUser currentUser) {
        this.currentUser=currentUser;
        getSupportFragmentManager().popBackStack();
        getSupportFragmentManager().beginTransaction().replace(R.id.showFrag,new MainFragment()).commit();

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

    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            TasksService.LocalBinder binder = (TasksService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    @Override
    public void onTaskClicked(String id) {
        GotoEditTaskFrag(id);
    }

    public void GotoEditTaskFrag(String id){
        EditTaskFragment editTaskFragment=new EditTaskFragment();
        Bundle args=new Bundle();
        args.putString(MyService.TASK_ID_KEY,id);
        editTaskFragment.setArguments(args);
        getSupportFragmentManager().beginTransaction().replace(R.id.showFrag,editTaskFragment).addToBackStack(null).commit();
    }
}

