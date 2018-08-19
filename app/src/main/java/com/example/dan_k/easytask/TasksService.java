package com.example.dan_k.easytask;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.util.Log;

public class TasksService extends Service {
    protected LocalBinder mBinder = new LocalBinder(); // Binder given to clients
    private Handler jobHandler=new Handler();
    private Runnable runnableCode = new Runnable() {
        @Override
        public void run() {
            // Do something here on the main thread
            Log.d("Handlers", "Called on main thread");
            // Repeat this the same runnable code block again another 2 seconds
            jobHandler.postDelayed(runnableCode, 2000);
        }
    };

    HandlerThread serviceThread;
    private Handler serviceHandler;
     class LocalBinder extends Binder {
        private TasksService tasksService;
        TasksService getService() {
            // Return this instance of LocalService so clients can call public methods
            return tasksService;
        }
    }

    public TasksService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        serviceThread = new HandlerThread(TasksService.class.getSimpleName());
        serviceThread.start();
        serviceHandler = new Handler(serviceThread.getLooper());
    }

    @Override
    public IBinder onBind(Intent intent) {
        mBinder.tasksService =this;
        mBinder.tasksService.jobHandler.post(mBinder.tasksService.runnableCode);
        return mBinder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
