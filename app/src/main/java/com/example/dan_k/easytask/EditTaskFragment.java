package com.example.dan_k.easytask;

import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import static android.app.Activity.RESULT_OK;


public class EditTaskFragment extends Fragment implements View.OnClickListener,
        PickerDialogs.DatePickerFragment.OnDateChangedListener,
        PickerDialogs.TimePickerFragment.OnTimeChangedListener {
    private View mFragmentView;
    private OnSuccessAddingTaskListener mListener;
    private EditText mEditTextDate;
    private EditText mEditTextTime;
    private EditText mEditTextLocation;
    private EditText mEditTextTitle;
    private EditText mEditTextDescription;
    private ImageButton mBtnDeleteDate;
    private ImageButton mBtnDeleteTime;
    private ImageButton mBtnDeleteLocation;
    private ScrollView mScrollView;
    private Calendar mCalendar;
    private DatabaseReference mUsersTasksRef;
    private double mLatitude=-1;
    private double mLongitude=-1;
    private final static int PLACE_PICKER_REQUEST = 1;
    private Button mBtnAddCurrentTask;
    @Override
    public void onDateChanged(Calendar calendar) {
        SimpleDateFormat dateFormat=new SimpleDateFormat("EEEE, MMMM d, yyyy");
        mEditTextDate.setText(dateFormat.format(calendar.getTime()).toString());
        mCalendar=calendar;
    }

    @Override
    public void onTimeChanged(Calendar calendar) {
        SimpleDateFormat dateFormat=new SimpleDateFormat("HH:mm");
        mEditTextTime.setText(dateFormat.format(calendar.getTime()).toString());
        mCalendar.set(Calendar.HOUR_OF_DAY,calendar.get(Calendar.HOUR_OF_DAY));
        mCalendar.set(Calendar.MINUTE,calendar.get(Calendar.MINUTE));
    }

    public interface OnSuccessAddingTaskListener {
        void onSuccessAddingTask();
    }

    public EditTaskFragment() {
        // Required empty public constructor
    }



    // TODO: Rename and change types and number of parameters
    public static EditTaskFragment newInstance(String param1, String param2) {
        EditTaskFragment fragment = new EditTaskFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        this.mFragmentView= inflater.inflate(R.layout.fragment_edit_task, container, false);
        mEditTextTitle= this.mFragmentView.findViewById(R.id.EditTextTitle);
        mEditTextDescription= this.mFragmentView.findViewById(R.id.EditTextDescription);
        mEditTextDate = this.mFragmentView.findViewById(R.id.EditTextChooseDate);
        mEditTextTime = this.mFragmentView.findViewById(R.id.EditTextChooseTime);
        mEditTextLocation =this.mFragmentView.findViewById(R.id.EditTextChooseLocation);
        mScrollView=this.mFragmentView.findViewById(R.id.scrollView);
        mBtnDeleteDate=this.mFragmentView.findViewById(R.id.btnDeleteDate);
        mBtnDeleteTime=this.mFragmentView.findViewById(R.id.btnDeleteTime);
        mBtnDeleteLocation=this.mFragmentView.findViewById(R.id.btnDeleteDate);
        mBtnAddCurrentTask=this.mFragmentView.findViewById(R.id.btnAddCurrentTask);
        mEditTextDate.setOnClickListener(this);
        mEditTextDate.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus) {
                    if(mEditTextTime.getText()==null)
                        mEditTextTime.setEnabled(false);
                    else
                        mEditTextTime.setEnabled(true);
                }
            }
        });
        mEditTextDate.setCursorVisible(false);
        mEditTextTime.setCursorVisible(false);
        mEditTextTime.setOnClickListener(this);
        mEditTextLocation.setOnClickListener(this);
        mBtnDeleteDate.setOnClickListener(this);
        mBtnDeleteTime.setOnClickListener(this);
        mBtnDeleteLocation.setOnClickListener(this);
        mBtnAddCurrentTask.setOnClickListener(this);
        return this.mFragmentView;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed() {
        if (mListener != null) {
            mListener.onSuccessAddingTask();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnSuccessAddingTaskListener) {
            mListener = (OnSuccessAddingTaskListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnSuccessAddingTaskListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.findItem(R.id.action_signIn).setVisible(false);
        menu.findItem(R.id.action_add).setVisible(false);
        menu.findItem(R.id.action_favorite).setVisible(false);
        super.onCreateOptionsMenu(menu, inflater);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_CancelTask:
                getActivity().getSupportFragmentManager().popBackStack();
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }


    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.EditTextChooseDate){
            PickerDialogs.SetParentFrag(this);
            showDatePickerDialog(v);
        }
        else if(v.getId()==R.id.EditTextChooseTime){
            if(!mEditTextDate.getText().equals("")) {
                PickerDialogs.SetParentFrag(this);
                showTimePickerDialog(v);
            }
        }
        else if(v.getId()==R.id.EditTextChooseLocation){
            activatePlacePicker();
        }
        else if(v.getId()==R.id.btnAddCurrentTask){
            addTask();
        }
        else if(v.getId()==R.id.btnDeleteDate){
           mEditTextDate.setText(null);
        }
        else if(v.getId()==R.id.btnDeleteTime){
            mEditTextTime.setText(null);
        }
        else if(v.getId()==R.id.btnDeleteLocation){
            mEditTextLocation.setText(null);
        }
    }

    private void addTask(){
        if(!mEditTextTitle.getText().toString().equals("") || !mEditTextDescription.getText().toString().equals("")) {
            long timeInMillis;
            timeInMillis=mEditTextDate.getText().toString().equals("")? -1: mCalendar.getTimeInMillis();
            mUsersTasksRef = FirebaseUtils.getDatabase().getReference(String.format("/tasks/%s/", FirebaseAuth.getInstance().getCurrentUser().getUid()));
            Task currentTask = new Task(
                    mEditTextTitle.getText().toString(),
                    mEditTextDescription.getText().toString(),
                    timeInMillis,
                    mLatitude,
                    mLongitude,
                    mEditTextLocation.getText().toString(),
                    false
                    );
            Map<String, Object> itemValues = currentTask.toMap();
            Map<String, Object> childUpdates = new HashMap<>();

            String key = mUsersTasksRef.child("tasks").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).push().getKey();
            childUpdates.put(String.format("/%s",key), itemValues);

            mUsersTasksRef.updateChildren(childUpdates, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                    if(databaseError==null){
                        getActivity().getSupportFragmentManager().popBackStack();
                    }
                }
            });

        }
    }

    public void showDatePickerDialog(View v) {
        DialogFragment newFragment = new PickerDialogs.DatePickerFragment();
        newFragment.show(getActivity().getFragmentManager(), "datePicker");
    }

    public void showTimePickerDialog(View v) {
        DialogFragment newFragment = new PickerDialogs.TimePickerFragment();
        newFragment.show(getActivity().getFragmentManager(), "timePicker");
    }

    private void activatePlacePicker(){
        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
//        builder.setLatLngBounds(new LatLngBounds(...)); when editing location...
        try {
            mEditTextLocation.setEnabled(false);
            mScrollView.setEnabled(false);
            startActivityForResult(builder.build(getActivity()), PLACE_PICKER_REQUEST);
        } catch (GooglePlayServicesRepairableException e) {
            e.printStackTrace();
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        mScrollView.setEnabled(true);
        mEditTextLocation.setEnabled(true);
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(getContext(), data);
                String toastMsg = String.format("Place: %s", place.getAddress());
                LatLng latLng=place.getLatLng();
                mLatitude=latLng.latitude;
                mLongitude=latLng.longitude;
                Toast.makeText(getContext(), toastMsg, Toast.LENGTH_LONG).show();
                mEditTextLocation.setText(toastMsg);
            }

        }
    }


}
