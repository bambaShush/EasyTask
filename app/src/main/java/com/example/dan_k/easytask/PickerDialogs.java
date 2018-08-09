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
    final static int PLACE_PICKER_REQUEST = 1;
    private static EditTaskFragment ParentFrag;
    public PickerDialogs(){
    }
    public static void SetParentFrag(EditTaskFragment fragment){
        if(ParentFrag==null)
            ParentFrag=fragment;
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
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            listener=PickerDialogs.ParentFrag;
            Calendar c=Calendar.getInstance();
            c.set(year,month,day);
            listener.onDateChanged(c);
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
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            // Create a new instance of TimePickerDialog and return it
            return new TimePickerDialog(getActivity(), this, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            listener=ParentFrag;
            Calendar c=Calendar.getInstance();
            c.set(0,0,0,hourOfDay,minute);
            listener.onTimeChanged(c);
        }
    }
}
