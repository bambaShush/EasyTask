package com.example.dan_k.easytask;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity implements SignUpFragment.OnLoginListener,
        EditTaskFragment.OnSuccessAddingTaskListener {
    private Fragment signInFragment;
    private FirebaseUser currentUser;
    private FusedLocationService mService;
    private boolean mBound = false;
    private Menu mMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //When you enable disk persistence, your app writes the data locally to the device so your app can maintain state
        // while offline, even if the user or operating system restarts the app.
        FirebaseUtils.getDatabase();

        //startActivity(new Intent(this,LocationActivity.class));
//        getSupportFragmentManager().beginTransaction().replace(R.id.showFrag,new SignUpFragment()).addToBackStack(null).commit();
        getSupportFragmentManager().beginTransaction().replace(R.id.showFrag,new MainFragment()).commit();

    }
    @Override
    public void onStart() {
        super.onStart();
//        if(!mBound) {
//            Intent intent = new Intent(this, FusedLocationService.class);
//            bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
//        }
            startService(new Intent(this, FusedLocationService.class));

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
        super.onDestroy();

        if(mService!=null)
            unbindService(mConnection);
        mBound = false;
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the main_menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        mMenu=menu;
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add:
                getSupportFragmentManager().beginTransaction().replace(R.id.showFrag,new EditTaskFragment()).addToBackStack(null).commit();
                getSupportActionBar().setTitle("Create task");
                return true;

            case R.id.action_favorite:
                // User chose the "Favorite" action, mark the current item
                // as a favorite...
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    public void onLogin(FirebaseUser currentUser) {
        this.currentUser=currentUser;
        startActivity(new Intent(this,FireBaseActivity.class));
    }

    @Override
    public void onSuccessAddingTask() {
        getFragmentManager().popBackStackImmediate();
    }



    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            FusedLocationService.LocalBinder binder = (FusedLocationService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };
}

