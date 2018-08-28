package com.example.dan_k.easytask;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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
    private ProgressBar mProgressBarLocation;
    private CheckBox mChceckBoxCompleted;
    private ScrollView mScrollView;
    private Calendar mCalendar;
    private DatabaseReference mUsersTasksRef;
    private DatabaseReference mEditTaskRef;
    private double mLatitude=-1;
    private double mLongitude=-1;
    private final static int PLACE_PICKER_REQUEST = 1;
    private final static int DEFUALT_HOUR=8;
    private final static int DEFUALT_MINUTE=0;
    public final static String EMPTY_STR="";
    private boolean isEditMode=false;
    private String editTaskId;
    private Task currentTask;
    private android.support.v7.app.ActionBar mActionBar;

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
        Bundle args=getArguments();
        mCalendar=Calendar.getInstance();
        mCalendar.set(Calendar.HOUR_OF_DAY,DEFUALT_HOUR);
        mCalendar.set(Calendar.MINUTE,DEFUALT_MINUTE);
        if(args!=null)
            editTaskId=args.getString(CheckTasksService.TASK_ID_KEY);
        mActionBar=((AppCompatActivity)getActivity()).getSupportActionBar();
        if(editTaskId!=null && !editTaskId.equals(EMPTY_STR)) {
            isEditMode = true;
            mActionBar.setTitle("Edit task");
        }else {
            mActionBar.setTitle("Create task");
        }
        mUsersTasksRef=FirebaseUtils.getUserTasksRef(false);
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
        mBtnDeleteLocation=this.mFragmentView.findViewById(R.id.btnDeleteLocation);
        mProgressBarLocation=this.mFragmentView.findViewById(R.id.progressBarLocation);
        mChceckBoxCompleted=this.mFragmentView.findViewById(R.id.CheckBoxCompleted);
        mEditTextDate.setOnClickListener(this);
        mEditTextDate.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                //disable choosing time when date is empty
                if(!hasFocus) {
                    if(isEditTextEqualsStr(mEditTextDate,EMPTY_STR))
                        mEditTextTime.setEnabled(false);
                    else
                        mEditTextTime.setEnabled(true);
                }
            }
        });
        mEditTextDescription.setMovementMethod(new ScrollingMovementMethod());
        mEditTextDate.setCursorVisible(false);
        mEditTextTime.setCursorVisible(false);
        mEditTextTime.setOnClickListener(this);
        mEditTextLocation.setOnClickListener(this);
        mBtnDeleteDate.setOnClickListener(this);
        mBtnDeleteTime.setOnClickListener(this);
        mBtnDeleteLocation.setOnClickListener(this);
        mProgressBarLocation.setVisibility(View.GONE);

        mScrollView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                mEditTextDescription.getParent().requestDisallowInterceptTouchEvent(false);

                return false;
            }
        });

        mEditTextDescription.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                mEditTextDescription.getParent().requestDisallowInterceptTouchEvent(true);

                return false;
            }
        });

        if(!isEditMode)
            mChceckBoxCompleted.setVisibility(View.GONE);
        loadTask();
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
        menu.findItem(R.id.action_add).setVisible(false);
        menu.findItem(R.id.action_logout).setVisible(false);
        menu.findItem(R.id.action_CancelTask).setVisible(true);
        menu.findItem(R.id.action_SaveTask).setVisible(true);
        menu.findItem(R.id.action_DeleteTask).setVisible(false);
        if(isEditMode)
            menu.findItem(R.id.action_DeleteTask).setVisible(true);
        super.onCreateOptionsMenu(menu, inflater);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_CancelTask:
                getActivity().getSupportFragmentManager().popBackStack();
                return true;
            case  R.id.action_SaveTask:
                saveTask();
                return true;
            case R.id.action_DeleteTask:
                AlertDialog diaBox = askDelete();
                diaBox.show();
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
            showDatePickerDialog();
        }
        else if(v.getId()==R.id.EditTextChooseTime){
            if(!isEditTextEqualsStr(mEditTextDate,EMPTY_STR)) {
                PickerDialogs.SetParentFrag(this);
                showTimePickerDialog();
            }
        }
        else if(v.getId()==R.id.EditTextChooseLocation){
            activatePlacePicker();
        }
        else if(v.getId()==R.id.btnDeleteDate){
           mEditTextDate.setText(null);
        }
        else if(v.getId()==R.id.btnDeleteTime){
            mEditTextTime.setText(null);
        }
        else if(v.getId()==R.id.btnDeleteLocation){
            mEditTextLocation.setText(null);
            mLatitude= CheckTasksService.NO_VALUE;
            mLongitude= CheckTasksService.NO_VALUE;
        }
    }

    private void addTask(){
            Map<String, Object> itemValues = currentTask.toMap();
            Map<String, Object> childUpdates = new HashMap<>();

            String key = mUsersTasksRef.child("tasks").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).push().getKey();
            childUpdates.put(String.format("/%s",key), itemValues);

            mUsersTasksRef.updateChildren(childUpdates, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                    if(databaseError==null){

                    }
                }
            });
        getActivity().getSupportFragmentManager().popBackStack();
    }
    private void loadTask(){
        if(isEditMode) {
            mEditTaskRef = FirebaseDatabase.getInstance()
                    .getReference(String.format("/tasks/%s/%s/", FirebaseAuth.getInstance().getCurrentUser().getUid(), editTaskId));
            getTasksData();
        }
    }

    private void saveTask(){
        if(!isEditTextEqualsStr(mEditTextTitle,EMPTY_STR) || !isEditTextEqualsStr(mEditTextDescription,EMPTY_STR)) {
            long timeInMillis;
            timeInMillis = isEditTextEqualsStr(mEditTextDate,EMPTY_STR) ? CheckTasksService.NO_VALUE : mCalendar.getTimeInMillis();
            currentTask = new Task(
                    mEditTextTitle.getText().toString(),
                    mEditTextDescription.getText().toString(),
                    timeInMillis,
                    mLatitude,
                    mLongitude,
                    mEditTextLocation.getText().toString(),
                    false,
                    mChceckBoxCompleted.isChecked()
            );
            if(isEditMode){
                mEditTaskRef.setValue(currentTask, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                        if(databaseError==null){

                        }
                    }
                });

            }else {
                addTask();
            }
            getActivity().getSupportFragmentManager().popBackStack();
        }
        else
            mEditTextTitle.requestFocus();
    }

    private void getTasksData() {
        if(mEditTaskRef!=null) {
            mEditTaskRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getChildrenCount() > 0) {
                        currentTask = dataSnapshot.getValue(Task.class);
                        mEditTextTitle.setText(currentTask.getTitle());
                        mEditTextDescription.setText(currentTask.getDescription());

                        if (currentTask.getTimeInMillis() == CheckTasksService.NO_VALUE) {
                            mEditTextDate.setText(EMPTY_STR);
                            mEditTextTime.setText(EMPTY_STR);
                        } else {
                            mCalendar.setTimeInMillis(currentTask.getTimeInMillis());
                            setDate(mCalendar);
                        }

                        mLatitude = currentTask.getLocationLat();
                        mLongitude = currentTask.getLocationLng();
                        mEditTextLocation.setText(mLatitude == CheckTasksService.NO_VALUE || mLongitude == CheckTasksService.NO_VALUE ? EMPTY_STR : currentTask.getLocationName());
                        mChceckBoxCompleted.setChecked(currentTask.isCompleted());
                        isEditMode = true;
                    } else
                        isEditMode = false;

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(getContext(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    isEditMode = false;
                }
            });
        }
    }

    private void deleteTask(){
        if(isEditMode){
        mEditTaskRef.removeValue();
        getActivity().getSupportFragmentManager().popBackStack();
        }
    }

    private AlertDialog askDelete()
    {
        AlertDialog myQuittingDialogBox =new AlertDialog.Builder(getContext())
                //set message, title, and icon
                .setTitle("Delete")
                .setMessage("Delete this task?")
                .setIcon(R.drawable.outline_delete_forever_24)

                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        deleteTask();
                        dialog.dismiss();
                    }

                })



                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();

                    }
                })
                .create();
        return myQuittingDialogBox;

    }


    @Override
    public void onDateChanged(Calendar calendar) {
        setDate(calendar);
    }

    private void setDate(Calendar calendar){
        SimpleDateFormat dateFormat=new SimpleDateFormat("EEEE, MMMM d, yyyy");
        mEditTextDate.setText(dateFormat.format(calendar.getTime()).toString());
        mCalendar.set(calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),calendar.get(Calendar.DAY_OF_MONTH));
        if(isEditTextEqualsStr(mEditTextTime,EMPTY_STR)){ //to not override the current value
            setTime(calendar);
        }
    }

    @Override
    public void onTimeChanged(Calendar calendar) {
        setTime(calendar);
    }

    private void setTime(Calendar calendar){
        SimpleDateFormat dateFormat=new SimpleDateFormat("HH:mm");
        mEditTextTime.setText(dateFormat.format(calendar.getTime()).toString());
        mCalendar.set(Calendar.HOUR_OF_DAY,calendar.get(Calendar.HOUR_OF_DAY));
        mCalendar.set(Calendar.MINUTE,calendar.get(Calendar.MINUTE));
    }


    public void showDatePickerDialog() {
        DialogFragment newFragment = new PickerDialogs.DatePickerFragment();
        if(!isEditTextEqualsStr(mEditTextDate,EMPTY_STR)) {
            Bundle args = new Bundle();
            args.putLong(PickerDialogs.TIME_IN_MILLIS, mCalendar.getTimeInMillis());
            newFragment.setArguments(args);
        }
        newFragment.show(getActivity().getFragmentManager(), "datePicker");

    }

    public void showTimePickerDialog() {
        DialogFragment newFragment = new PickerDialogs.TimePickerFragment();
        Bundle args = new Bundle();
        args.putLong(PickerDialogs.TIME_IN_MILLIS, mCalendar.getTimeInMillis());
        newFragment.setArguments(args);
        newFragment.show(getActivity().getFragmentManager(), "timePicker");
    }

    private void activatePlacePicker(){
        mProgressBarLocation.setVisibility(View.VISIBLE);
        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
        if(mLongitude!= CheckTasksService.NO_VALUE && mLatitude!= CheckTasksService.NO_VALUE)
            builder.setLatLngBounds(new LatLngBounds
                    (new LatLng(mLatitude,mLongitude)
                            ,new LatLng(mLatitude,mLongitude)));
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
        mProgressBarLocation.setVisibility(View.GONE);
        mScrollView.setEnabled(true);
        mEditTextLocation.setEnabled(true);
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(getContext(), data);
                String placeName=place.getName().toString();
                placeName=placeName.contains("\"E") || placeName.contains("\"N")? EMPTY_STR: placeName;
                String locationName = String.format("Place: %s\n%s",placeName,place.getAddress());
                LatLng latLng=place.getLatLng();
                mLatitude=latLng.latitude;
                mLongitude=latLng.longitude;
                mEditTextLocation.setText(locationName);
            }

        }
    }

    private boolean isEditTextEqualsStr(EditText editText,String str){
        return editText.getText().toString().equals(str);
    }

    @Override
    public void onDestroy() {
        mActionBar.setTitle("Easy Task");
        super.onDestroy();
    }
}
