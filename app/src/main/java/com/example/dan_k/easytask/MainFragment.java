package com.example.dan_k.easytask;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class MainFragment extends Fragment {
    private static String TAG=MainFragment.class.getName();
    protected View fragmentView;
    private ListView listView;
    private Map<String, Task> taskMap;
    CustomAdapter adapter;
    ArrayList<TaskListRowItem> rowsArrayList;
    private DatabaseReference mUsersTasksRef;
    private OnTaskClickedListener mListener;
    private ActionBar mActionBar;
    private RelativeLayout mLoadingPanel;
    private TextView mTextAddFirstTask;
    private boolean mShowLoadingPanel =true;
    private ValueEventListener mDataChangedListener;
    private CheckBox mChkShowNotified;
    private boolean mInitChkShowNotified=false;
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
        mActionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        mActionBar.setTitle("Easy Task");
        mActionBar.show();
        setHasOptionsMenu(true);
        Bundle args=getArguments();
        if(args!=null)
            mInitChkShowNotified=args.getBoolean(CheckTasksService.NOTIFIED_ONLY_KEY);
        mUsersTasksRef = FirebaseUtils.getUserTasksRef(true);
        taskMap=new HashMap<>();
        rowsArrayList =new ArrayList<>();

//        mUsersTasksRef.orderByChild("addedDate")

        mDataChangedListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                if (dataSnapshot.getChildrenCount() > 0) {
                    mTextAddFirstTask.setVisibility(View.GONE);
                    updateAdapter(dataSnapshot);
                }
                else
                    mTextAddFirstTask.setVisibility(View.VISIBLE);
                mLoadingPanel.setVisibility(View.GONE);
                mShowLoadingPanel =false;
                getActivity().startService(new Intent(getContext(),CheckTasksService.class));
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        };

        mUsersTasksRef.addValueEventListener(mDataChangedListener);



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
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) { mListener.onTaskClicked(((TaskListRowItem) parent.getAdapter().getItem(position)).getId());
            }
        });
        listView.setAdapter(adapter);
        mLoadingPanel =this.fragmentView.findViewById(R.id.loadingPanel);
        if(!mShowLoadingPanel)
            mLoadingPanel.setVisibility(View.GONE);

        mTextAddFirstTask =this.fragmentView.findViewById(R.id.textAddFirstTask);
        mTextAddFirstTask.setVisibility(View.GONE);
        mTextAddFirstTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.showFrag,new EditTaskFragment()).addToBackStack(null).commit();
            }
        });

        mChkShowNotified=this.fragmentView.findViewById(R.id.chkShowNotifiedOnly);
        mChkShowNotified.setChecked(mInitChkShowNotified);
        mInitChkShowNotified=false;
        mChkShowNotified.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateAdapter();
            }
        });
        return this.fragmentView;
    }



    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.findItem(R.id.action_add).setVisible(true);
        menu.findItem(R.id.action_logout).setVisible(true);
        menu.findItem(R.id.action_CancelTask).setVisible(false);
        menu.findItem(R.id.action_SaveTask).setVisible(false);
        menu.findItem(R.id.action_DeleteTask).setVisible(false);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add:
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.showFrag,new EditTaskFragment()).addToBackStack(null).commit();
                return true;
            case R.id.action_logout:
                Bundle args=new Bundle();
                args.putBoolean(CheckTasksService.getLogoutKey(),true);
                LoginFragment fragment=new LoginFragment();
                fragment.setArguments(args);
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.showFrag,fragment).addToBackStack(null).commit();
                return true;
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }


    private void updateAdapter(DataSnapshot dataSnapshot) {
        if(!rowsArrayList.isEmpty())
            rowsArrayList.clear();
        if(!taskMap.isEmpty())
            taskMap.clear();
        for(DataSnapshot taskSnapShot:dataSnapshot.getChildren()) {
            taskMap.put(taskSnapShot.getKey(),taskSnapShot.getValue(Task.class));
            addRowsArrListItem(taskSnapShot.getValue(Task.class),taskSnapShot.getKey());
        }
        adapter.notifyDataSetChanged();
    }

    private void updateAdapter() {
        if(!rowsArrayList.isEmpty())
            rowsArrayList.clear();
        Iterator<Map.Entry<String, Task>> it = taskMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Task> taskEntry = it.next();
            addRowsArrListItem(taskEntry.getValue(),taskEntry.getKey());
        }
        adapter.notifyDataSetChanged();
    }

    private void addRowsArrListItem(Task task,String taskId){
        if(!mChkShowNotified.isChecked() || (task.isNotified() && !task.isCompleted())) {
            rowsArrayList.add(new TaskListRowItem(taskId,task));
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof OnTaskClickedListener) {
            mListener = (OnTaskClickedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnTaskClickedListener");
        }
    }


    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onDestroy() {
        if(mUsersTasksRef!=null && mDataChangedListener!=null)
            mUsersTasksRef.removeEventListener(mDataChangedListener);

        super.onDestroy();
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
    public interface OnTaskClickedListener {
        // TODO: Update argument type and name
        void onTaskClicked(String id);
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
            TextView dueDateText;
            ImageView notifiedIcon;
            ImageView locationIcon;
            TextView location;
            CheckBox completed;

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
                holder.dueDateText=convertView.findViewById(R.id.dueDateText);
                holder.notifiedIcon =convertView.findViewById(R.id.notifiedImg);
                holder.locationIcon=convertView.findViewById(R.id.locationImg);
                holder.location=convertView.findViewById(R.id.locationValue);
                holder.completed=convertView.findViewById(R.id.chkCompleted);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }



            final TaskListRowItem row_item = rowItems.get(position);
                holder.title.setText(row_item.getTitle());
            if(row_item.getDescription()!=null && !row_item.getDescription().equals(EditTaskFragment.EMPTY_STR)) {
                holder.description.setText(row_item.getDescription());
                holder.description.setVisibility(View.VISIBLE);
            }
            else
                holder.description.setVisibility(View.GONE);
            if(!row_item.getDueDate().equals(CheckTasksService.NO_VALUE_STR)) {
                holder.dueDate.setText(row_item.getDueDate());
                holder.dueDate.setVisibility(View.VISIBLE);
                holder.dueDateText.setVisibility(View.VISIBLE);
            }
            else {
                holder.dueDate.setVisibility(View.GONE);
                holder.dueDateText.setVisibility(View.GONE);
            }
            if(!row_item.isNotified())
                holder.notifiedIcon.setVisibility(View.GONE);
            else
                holder.notifiedIcon.setVisibility(View.VISIBLE);
            if(!row_item.getLocation().equals(EditTaskFragment.EMPTY_STR)) {
                holder.locationIcon.setVisibility(View.VISIBLE);
                holder.location.setText(row_item.getLocation());
                holder.location.setVisibility(View.VISIBLE);
            }
            else {
                holder.locationIcon.setVisibility(View.GONE);
                holder.location.setVisibility(View.GONE);
            }
            if(row_item.isCompleted()){
                holder.title.setPaintFlags(holder.title.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                holder.description.setPaintFlags(holder.description.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            }else {
                holder.title.setPaintFlags(holder.title.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                holder.description.setPaintFlags(holder.description.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            }
                holder.completed.setChecked(row_item.isCompleted());

            holder.completed.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mUsersTasksRef!=null){
                        mUsersTasksRef.child(row_item.getId()).child(Task.getCompletedKey()).setValue(((CheckBox)v).isChecked());
                    }
                }
            });

            return convertView;
        }



    }
}
