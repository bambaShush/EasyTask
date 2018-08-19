package com.example.dan_k.easytask;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FirebaseUtils {
    private static FirebaseDatabase mDatabase;
    //is needed: private static Auth variable
    public static FirebaseDatabase getDatabase() {
        if (mDatabase == null) {
            mDatabase = FirebaseDatabase.getInstance();
            mDatabase.setPersistenceEnabled(true);
        }
        return mDatabase;
    }

    public static DatabaseReference getUserTasksRef(boolean keepSynced) {
        DatabaseReference usersTasksRef=null;
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            usersTasksRef = getDatabase().getReference(String.format("/tasks/%s/", FirebaseAuth.getInstance().getCurrentUser().getUid()));
            if (keepSynced && usersTasksRef != null)
                usersTasksRef.keepSynced(true);
        }
        return usersTasksRef;
    }


}
