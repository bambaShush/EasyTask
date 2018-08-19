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
    private boolean completed;

    public TaskListRowItem(String id, String title, String description, String dueDate, String location, boolean notified,boolean completed) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.location = location;
        this.notified = notified;
        this.completed=completed;
    }


    public TaskListRowItem(String taskId,Task task){
        this.id = taskId;
        this.title = task.getTitle();
        this.description = task.getDescription();

        if(task.getTimeInMillis()!=MyService.NO_VALUE) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(task.getTimeInMillis());
            SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMMM d, yyyy  HH:mm");
            this.dueDate = dateFormat.format(calendar.getTime()).toString();
        }
        else
            this.dueDate = MyService.NO_VALUE_STR;
        this.location = task.getLocationName();
        this.notified = task.isNotified();
        this.completed=task.isCompleted();
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

    public boolean isCompleted() {
        return completed;
    }
}
