package com.example.dan_k.easytask;


import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class Task {
//    @Exclude
//    public String id;

    public String title;
    public String description;
    public long timeInMillis;
    public double locationLat;
    public double locationLng;
    public String locationName;
    public boolean notified;

    public Task() {
        // Default constructor required for calls to DataSnapshot.getValue(Task.class)
    }

    public Task(String title, String description,long timeInMillis, double locationLat,double locationLng,String locationName,boolean notified) {

        this.title=title;
        this.description=description;
        this.timeInMillis = timeInMillis;
        this.locationLat=locationLat;
        this.locationLng=locationLng;
        this.locationName=locationName;
        this.notified=notified;
    }


    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("title", title);
        result.put("description", description);
        result.put("timeInMillis", timeInMillis);
        result.put("locationLat",locationLat);
        result.put("locationLng",locationLng);
        result.put("locationName",locationName);
        result.put("notified",notified);
        return result;
    }

}
