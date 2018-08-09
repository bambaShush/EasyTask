package com.example.dan_k.easytask;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class TaskListRowItem {
    private String id;
    private String title;
    private String description;
    private String dueDate;
    private String location;
    private boolean notified;


    public TaskListRowItem(String id, String title, String description, String dueDate, String location, boolean notified) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.location = location;
        this.notified = notified;
    }


    public TaskListRowItem(String taskId,Task task){
        this.id = taskId;
        this.title = task.title;
        this.description = task.description;

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(task.timeInMillis);
        SimpleDateFormat dateFormat=new SimpleDateFormat("EEEE, MMMM d, yyyy  HH:mm");
        this.dueDate = dateFormat.format(calendar.getTime()).toString();
        this.location = task.locationName;
        this.notified = task.notified;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getDueDate() {
        return dueDate;
    }

    public String getLocation() {
        return location;
    }

    public boolean isNotified() {
        return notified;
    }
}
