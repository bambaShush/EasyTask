package com.example.dan_k.easytask;


import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class Task{
    private static final String TITLE_KEY="title";
    private static final String DESCRIPTION_KEY="description";
    private static final String TIMEINMILLIS_KEY="timeInMillis";
    private static final String LOCATIONLAT_KEY="locationLat";
    private static final String LOCATIONLNG_KEY="locationLng";
    private static final String LOCATIONNAME_KEY="locationName";
    private static final String NOTIFIED_KEY="notified";

//    @Exclude
//    public String id;

    private String title;
    private String description;
    private long timeInMillis;
    private double locationLat;
    private double locationLng;
    private String locationName;
    private boolean notified;
    private boolean completed;

    public Task() {
        // Default constructor required for calls to DataSnapshot.getValue(Task.class)
    }

    public Task(String title, String description,long timeInMillis, double locationLat,double locationLng,String locationName
            ,boolean notified,boolean completed) {

        this.title=title;
        this.description=description;
        this.timeInMillis = timeInMillis;
        this.locationLat=locationLat;
        this.locationLng=locationLng;
        this.locationName=locationName;
        this.notified=notified;
        this.completed=completed;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public long getTimeInMillis() {
        return timeInMillis;
    }

    public double getLocationLat() {
        return locationLat;
    }

    public double getLocationLng() {
        return locationLng;
    }

    public String getLocationName() {
        return locationName;
    }

    public boolean isNotified() {
        return notified;
    }

    public boolean isCompleted() {
        return completed;
    }

    public static String getNotifiedKey() {
        return NOTIFIED_KEY;
    }

    public void setNotified(boolean notified) {
        this.notified = notified;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put(TITLE_KEY, title);
        result.put(DESCRIPTION_KEY, description);
        result.put(TIMEINMILLIS_KEY, timeInMillis);
        result.put(LOCATIONLAT_KEY,locationLat);
        result.put(LOCATIONLNG_KEY,locationLng);
        result.put(LOCATIONNAME_KEY,locationName);
        result.put(NOTIFIED_KEY,notified);
        return result;
    }

}
