package com.armageddon.android.flickrdroid.ui.fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.DatePicker;

import com.armageddon.android.flickrdroid.R;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;


/**
 * Shows dialog view with calender spinner mode.
 * Minimum date permitted from Flickr.com: 01/08/2004
 */


public class DaterPickerFragment extends DialogFragment {
    private final static String DATE_ID = "date_id";
    private static final Calendar CALENDAR_MIN_DATE =
            new GregorianCalendar(2004, 7, 1);
    private Calendar mCurrentDate;
    private CallBacks mCallBacks;

    public interface CallBacks {
        void getCalendar (Calendar calendar, boolean reset);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mCallBacks = (CallBacks) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallBacks = null;
    }

    DatePicker mDatePicker;

    public static DaterPickerFragment newInstance(Calendar calendar) {

        Bundle args = new Bundle();
        args.putSerializable(DATE_ID, calendar);
        DaterPickerFragment fragment = new DaterPickerFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        assert getArguments() != null;
        Calendar calendar = (Calendar) getArguments().get(DATE_ID);

        if (calendar == null) {
            mCurrentDate = Calendar.getInstance();
            mCurrentDate.add(Calendar.DATE, -2);
        } else {
            mCurrentDate = calendar;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Window window = Objects.requireNonNull(getDialog()).getWindow();

        assert window != null;
        float density = requireActivity().getResources().getDisplayMetrics().density;

        /* Dialog Margrin Top */
        window.setGravity(Gravity.TOP);
        WindowManager.LayoutParams params = window.getAttributes();
        params.y = (int) (78 * density + 0.5f);
        window.setAttributes(params);

        /* Dialog width and height */
        int width = (int) (300 * density + 0.5f);
        int height = (int) (250 * density + 0.5f);

        window.setLayout(width,height);
        return super.onCreateView(inflater, container, savedInstanceState);
    }



    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        @SuppressLint("InflateParams")
        View v = LayoutInflater.from(getActivity()).inflate(R.layout.date_dialog, null);
        mDatePicker = (DatePicker) v;
        mDatePicker.setMinDate(CALENDAR_MIN_DATE.getTimeInMillis());

        Calendar yesterday = Calendar.getInstance();
        yesterday.add(Calendar.DATE, - 1);
        mDatePicker.setMaxDate(yesterday.getTimeInMillis());

        Calendar today = new GregorianCalendar();
        today.add(Calendar.DATE, -1);

        mDatePicker.init(
                mCurrentDate.get(Calendar.YEAR),
                mCurrentDate.get(Calendar.MONTH),
                mCurrentDate.get(Calendar.DATE),
                null);

        return new AlertDialog.Builder(getActivity(),R.style.DialogAnimationStyle)
                .setView(mDatePicker)
                .setTitle(getString(R.string.date_dialog_title))
                .setNegativeButton(getString(R.string.reset), (dialog, which) -> {
                    mCurrentDate = null;
                    mCallBacks.getCalendar(today,true);
                })
                .setPositiveButton("Ok", (dialog, which) -> {

                Calendar userCalender = new GregorianCalendar(
                        mDatePicker.getYear(),
                        mDatePicker.getMonth(),
                        mDatePicker.getDayOfMonth());
                    if (dateCompare(userCalender, today)) {
                        mCallBacks.getCalendar(today, true);
                    } else {
                        mCallBacks.getCalendar(userCalender, false);
                    }
                }).show();
    }

    private boolean dateCompare (Calendar calendar1, Calendar calendar2) {
        int date1 = calendar1.get(Calendar.DATE);
        int month1 = calendar1.get(Calendar.MONTH);
        int year1 = calendar1.get(Calendar.YEAR);
        int date2 = calendar2.get(Calendar.DATE);
        int month2 = calendar2.get(Calendar.MONTH);
        int year2 = calendar2.get(Calendar.YEAR);

        return date1 == date2 && month1 == month2 && year1 == year2;
    }
}
