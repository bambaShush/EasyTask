package com.example.dan_k.easytask;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class FireBaseActivity extends AppCompatActivity {
    private static String TAG=FireBaseActivity.class.getName();
    final String mockUser1="user1";
    final String mockUser2="user2";
    private FirebaseDatabase mDatabaseInstance;
    private DatabaseReference mUsersTasksRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fire_base);


        mDatabaseInstance = FirebaseDatabase.getInstance();



        mUsersTasksRef = mDatabaseInstance.getReference(String.format("/tasks/%s/",FirebaseAuth.getInstance().getCurrentUser().getUid()));
        mUsersTasksRef.keepSynced(true);

        Calendar calendar=Calendar.getInstance();
        calendar.set(2018,8,5,10,5);

        ArrayList<Task>arrayList=new ArrayList<>();
/*
        rowsArrayList.add(new Task(
                "long Title... need to do this and that",
                "description, long description need this and that long long",
                calendar.getTimeInMillis(),0,0,"הבאר של סבא", false));

        rowsArrayList.add(new Task(
                "short Title",
                "description, long description need this and that long long very very longi",
                calendar.getTimeInMillis(),0,0,"some loaction long shit",true));

        rowsArrayList.add(new Task(
                "partial long Title... need to do",
                "description, long description need this and that long longi",
                calendar.getTimeInMillis(),0,0,"another location blal lll",false));


        rowsArrayList.add(new Task(
                "short Title number 4",
                "description, long description need this and that long long very very longi",
                calendar.getTimeInMillis(),0,0,"location with bloo",false));

        rowsArrayList.add(new Task(
                "כותרת משימה כלשהי משימה 5",
                "description, short description",
                calendar.getTimeInMillis(),0,0,"location loccdasdaosdjdiasduada this is location",true));
*/

        for(Task task : arrayList){
            addNewTask(task);
        }


        //mDatabaseRef.child("tasks").child("z").setValue(task);



        mUsersTasksRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
//        mUsersTasksRef.orderByChild("addedDate")
        mUsersTasksRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                Log.d(TAG, "Value is: " + dataSnapshot.getValue());
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });



    }

    private void addNewTask(Task task){
        Map<String, Object> itemValues = task.toMap();
        Map<String, Object> childUpdates = new HashMap<>();

        String key = mUsersTasksRef.child("tasks").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).push().getKey();
        childUpdates.put(String.format("/%s",key), itemValues);

        mUsersTasksRef.updateChildren(childUpdates);
    }

    @IgnoreExtraProperties
    public class User {

        public String username;
        public String email;

        public User() {
            // Default constructor required for calls to DataSnapshot.getValue(User.class)
        }

        public User(String username, String email) {
            this.username = username;
            this.email = email;
        }

    }



}
