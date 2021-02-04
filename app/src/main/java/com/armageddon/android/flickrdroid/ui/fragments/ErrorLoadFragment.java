package com.armageddon.android.flickrdroid.ui.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.armageddon.android.flickrdroid.R;

import java.io.Serializable;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

/**
 * Custom dialog fragment to show error if pictures loading on the map was not completed successfully.
 */

public class ErrorLoadFragment extends DialogFragment implements View.OnClickListener {
    private static final String ERROR_TYPE = "error_type";
    private static final String CALL_BACKS = "call_backs";
    private String mErrorText;
    private CallBacks mCallBacks;

    @Override
    public void onClick(View v) {
        mCallBacks.onShowCancel();
        ErrorLoadFragment.this.dismiss();
    }

    ImageView mCloseButton;
    TextView mErrorMessage;

    interface CallBacks extends Serializable {
        void onShowCancel ();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallBacks = null;
    }

    public static ErrorLoadFragment newInstance(String error, CallBacks callBacks) {

        Bundle args = new Bundle();
        args.putString(ERROR_TYPE, error);
        args.putSerializable(CALL_BACKS,callBacks);
        ErrorLoadFragment fragment = new ErrorLoadFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mErrorText = getArguments().getString(ERROR_TYPE);
        mCallBacks = (CallBacks) getArguments().getSerializable(CALL_BACKS);

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

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        View v = LayoutInflater.from(getActivity()).inflate(R.layout.error_load_dialog, null);
        mErrorMessage = v.findViewById(R.id.error_message);
        mCloseButton = v.findViewById(R.id.icon_close);
        mErrorMessage.setText(mErrorText);
        mCloseButton.setOnClickListener(this);


        return new AlertDialog.Builder(getActivity(),R.style.LoadingDialogAnimationStyle)
                .setView(v)
                .show();
    }

}
