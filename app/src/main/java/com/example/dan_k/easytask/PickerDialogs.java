package com.example.dan_k.easytask;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.DatePicker;
import android.widget.TimePicker;

import java.util.Calendar;


public class PickerDialogs {
    public static final String TIME_IN_MILLIS="timeinmillis";
    private static EditTaskFragment s_parentFrag;
    public PickerDialogs(){
    }
    public static void SetParentFrag(EditTaskFragment fragment){
        s_parentFrag =fragment;
    }

    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {
        private static OnDateChangedListener listener;
        public interface OnDateChangedListener {
            // TODO: Update argument type and name
            void onDateChanged(Calendar calendar);
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            Calendar c = Calendar.getInstance();
            Bundle args=getArguments();
            if(args!=null){
                c.setTimeInMillis(args.getLong(TIME_IN_MILLIS));
            }
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            listener=PickerDialogs.s_parentFrag;
            Calendar c=Calendar.getInstance();
            c.set(year,month,day);
            listener.onDateChanged(c);
            s_parentFrag=null;
        }
    }

    public static class TimePickerFragment extends DialogFragment
            implements TimePickerDialog.OnTimeSetListener {

        private static OnTimeChangedListener listener;
        public interface OnTimeChangedListener {
            void onTimeChanged(Calendar calendar);
        }


        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current time as the default values for the picker
            Calendar c = Calendar.getInstance();
            Bundle args=getArguments();
            if(args!=null){
                c.setTimeInMillis(args.getLong(TIME_IN_MILLIS));
            }
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);
            // Create a new instance of TimePickerDialog and return it
            return new TimePickerDialog(getActivity(), this, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            listener= s_parentFrag;
            Calendar c=Calendar.getInstance();
            c.set(0,0,0,hourOfDay,minute);
            listener.onTimeChanged(c);
            s_parentFrag=null;
        }
    }
}
