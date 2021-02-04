package com.armageddon.android.flickrdroid.ui.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.armageddon.android.flickrdroid.R;

import java.io.Serializable;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;


/**
 * Custom dialog fragment to show loading process (photos on the map).
 */

public class LoadingFragment extends DialogFragment implements View.OnClickListener {
    private static final String CALL_BACKS = "call_backs";
    private CallBacks mCallBacks;

    @Override
    public void onClick(View v) {
        mCallBacks.onShowCancel();
        LoadingFragment.this.dismiss();
    }

    interface CallBacks extends Serializable {
       void onShowCancel ();
    }

    ProgressBar mProgressBar;
    ImageView mClose;

    public static LoadingFragment newInstance(CallBacks callBacks) {

        Bundle args = new Bundle();
        args.putSerializable(CALL_BACKS,callBacks);
        LoadingFragment fragment = new LoadingFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        assert getArguments() != null;
        mCallBacks = (CallBacks) getArguments().getSerializable(CALL_BACKS);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallBacks = null;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Window window = Objects.requireNonNull(getDialog()).getWindow();

        /* Dialog background Drawble */
        assert window != null;
        float density = getActivity().getResources().getDisplayMetrics().density;

        /* Dialog width and height */
        int width = (int) (250 * density + 0.5f);
        int height = (int) (150 * density + 0.5f);
        window.setLayout(width,height);

        return super.onCreateView(inflater, container, savedInstanceState);
    }


    public void setProgressMax (int value) {
        mProgressBar.setMax(value);
    }


    public void setLoadingProgress (int progress) {
                mProgressBar.setProgress(progress);
                if (progress == mProgressBar.getMax()) {
                    LoadingFragment.this.dismiss();
                }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        View v = LayoutInflater.from(getActivity()).inflate(R.layout.loading_dialog, null);
        mProgressBar = v.findViewById(R.id.progress_bar);
        mClose = v.findViewById(R.id.icon_close);
        mClose.setOnClickListener(this);


        return new AlertDialog.Builder(getActivity(),R.style.LoadingDialogAnimationStyle)
                .setView(v)
                .show();
    }

}
