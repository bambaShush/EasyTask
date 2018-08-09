package com.example.dan_k.easytask;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MainFragment extends Fragment {
    private static String TAG=MainFragment.class.getName();
    protected View fragmentView;
    private ListView listView;
    private Map<String,Task>taskMap;
    CustomAdapter adapter;
    ArrayList<TaskListRowItem> rowsArrayList;
    private FirebaseDatabase mDatabaseInstance;
    private DatabaseReference mUsersTasksRef;
    private OnFragmentInteractionListener mListener;

    public MainFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static MainFragment newInstance(String param1, String param2) {
        MainFragment fragment = new MainFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDatabaseInstance = FirebaseDatabase.getInstance();
        mUsersTasksRef = mDatabaseInstance.getReference(String.format("/tasks/%s/", FirebaseAuth.getInstance().getCurrentUser().getUid()));
        mUsersTasksRef.keepSynced(true);
        taskMap=new HashMap<>();
        rowsArrayList =new ArrayList<>();
        mUsersTasksRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
//                    taskMap.put(dataSnapshot.getKey(),dataSnapshot.getValue(Task.class));
                rowsArrayList.add(new TaskListRowItem(dataSnapshot.getKey(),dataSnapshot.getValue(Task.class)));
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
                adapter.notifyDataSetChanged();
                Log.d(TAG, "Value is: " + dataSnapshot.getValue());
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });





/*
        rowsArrayList.add(new TaskListRowItem("1",
                "long Title... need to do this and that",
                "description, long description need this and that long long",
                "23.2.18 22:45 something like that","0",false));

        rowsArrayList.add(new TaskListRowItem("2",
                "short Title",
                "description, long description need this and that long long very very longi",
                "23.2.18 22:45 something like that","0",false));

        rowsArrayList.add(new TaskListRowItem("3",
                "partial long Title... need to do",
                "description, long description need this and that long longi",
                "23.2.18 22:45 something like that","0",true));


        rowsArrayList.add(new TaskListRowItem("4",
                "short Title number 4",
                "description, long description need this and that long long very very longi",
                "23.2.18 22:45 something like that","0",false));

        rowsArrayList.add(new TaskListRowItem("5",
                "כותרת משימה כלשהי משימה 5",
                "description, short description",
                "23.2.18 22:45 something like that","0",true));
     */
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        adapter = new CustomAdapter(getContext(), rowsArrayList);
        // Inflate the layout for this fragment
        this.fragmentView= inflater.inflate(R.layout.fragment_main, container, false);
        listView = (ListView)this.fragmentView.findViewById(R.id.list);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });
        listView.setAdapter(adapter);
        return this.fragmentView;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
//
//        if (context instanceof OnFragmentInteractionListener) {
//            mListener = (OnFragmentInteractionListener) context;
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    private class CustomAdapter extends BaseAdapter {

        Context context;
        List<TaskListRowItem> rowItems;

        CustomAdapter(Context context, List<TaskListRowItem> rowItems) {
            this.context = context;
            this.rowItems = rowItems;
        }

        @Override
        public int getCount() {
            return rowItems.size();
        }

        @Override
        public Object getItem(int position) {
            return rowItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return rowItems.indexOf(getItem(position));
        }

        /* private view holder class */
        private class ViewHolder {
            TextView title;
            TextView description;
            TextView dueDate;
            ImageView notifiedImg;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            ViewHolder holder = null;

            LayoutInflater mInflater = (LayoutInflater) context
                    .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.task_list_item, null);
                holder = new ViewHolder();

                holder.title = convertView.findViewById(R.id.titleValue);
                holder.description =  convertView.findViewById(R.id.descriptionValue);
                holder.dueDate =  convertView.findViewById(R.id.dueDateValue);
                holder.notifiedImg =convertView.findViewById(R.id.notifiedImg);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }


            TaskListRowItem row_item = rowItems.get(position);
            if(!row_item.isNotified())
                holder.notifiedImg.setVisibility(View.INVISIBLE);
            holder.title.setText(row_item.getTitle());
            holder.description.setText(row_item.getDescription());
            holder.dueDate.setText(row_item.getDueDate());


            return convertView;
        }



    }
}
