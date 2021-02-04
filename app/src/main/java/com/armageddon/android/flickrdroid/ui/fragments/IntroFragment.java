package com.armageddon.android.flickrdroid.ui.fragments;

import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.armageddon.android.flickrdroid.BuildConfig;
import com.armageddon.android.flickrdroid.R;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

/**
 * Fragment with slides for intro.
 * Receives slide number from activity and via "case" sets the relevant information.
 */

public class IntroFragment extends Fragment {
    private static final String INTRO_PAGE = "intro_page";
    public static final int SLIDE_COUNT = 10;
    private int mIntroPageNumber;

    public static IntroFragment newInstance(int introPage) {

        Bundle args = new Bundle();
        args.putInt(INTRO_PAGE, ++introPage);
        IntroFragment fragment = new IntroFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        assert getArguments() != null;
        mIntroPageNumber = getArguments().getInt(INTRO_PAGE);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_intro, container, false);
        ImageView logoImage = v.findViewById(R.id.logo_image);
        TextView headerText = v.findViewById(R.id.header_text_view);
        TextView logoText = v.findViewById(R.id.logo_text);
        TextView descriptionText = v.findViewById(R.id.description_text_view);

        ConstraintLayout.LayoutParams lp = (ConstraintLayout.LayoutParams)
                descriptionText.getLayoutParams();
        lp.height = ConstraintLayout.LayoutParams.WRAP_CONTENT;
        lp.width = ConstraintLayout.LayoutParams.WRAP_CONTENT;

        switch (mIntroPageNumber) {
            case 1:
                Shader textShader = new LinearGradient(0, 0, 0,
                        logoText.getPaint().getTextSize(),
                        new int[]{getResources().getColor(R.color.colorPinkFlickr, null),
                                getResources().getColor(R.color.colorBlueFlickr, null)},
                        new float[]{0, 1}, Shader.TileMode.CLAMP);
                logoText.getPaint().setShader(textShader);
                logoText.setText(getText(R.string.app_name));
                headerText.setText(getString(R.string.intro_header1));
                descriptionText.setText(getString(R.string.intro_description1));
                break;
            case 2:
                headerText.setText(getText(R.string.intro_header2));
                descriptionText.setLayoutParams(lp);
                descriptionText.setText(getText(R.string.intro_description2));
                descriptionText.setTextAlignment(View.TEXT_ALIGNMENT_INHERIT);
                logoImage.setImageDrawable(
                        ContextCompat.getDrawable(getActivity(),R.drawable.intro_photo_2));
                break;

            case 3:
                headerText.setText(getText(R.string.intro_header3));
                descriptionText.setText(getText(R.string.intro_description3));
                logoImage.setImageDrawable(
                        ContextCompat.getDrawable(getActivity(),R.drawable.intro_photo_3));
                break;

            case 4:
                descriptionText.setText(getText(R.string.intro_description4));
                descriptionText.setTextAlignment(View.TEXT_ALIGNMENT_INHERIT);
                headerText.setText(getText(R.string.intro_header4));
                logoImage.setImageDrawable(
                        ContextCompat.getDrawable(getActivity(),R.drawable.intro_photo_4));
                break;

            case 5:
                descriptionText.setText(getText(R.string.intro_description5));
                headerText.setText(getText(R.string.intro_header5));
                logoImage.setImageDrawable(
                        ContextCompat.getDrawable(getActivity(),R.drawable.intro_photo_5));
                break;

            case 6:
                descriptionText.setText(getText(R.string.intro_description6));
                headerText.setText(getText(R.string.intro_header6));
                logoImage.setImageDrawable(
                        ContextCompat.getDrawable(getActivity(),R.drawable.intro_photo_6));
                break;

            case 7:
                descriptionText.setText(getText(R.string.intro_description7));
                headerText.setText(getText(R.string.intro_header7));
                logoImage.setImageDrawable(
                        ContextCompat.getDrawable(getActivity(),R.drawable.intro_photo_7));
                break;

            case 8:
                descriptionText.setText(getText(R.string.intro_description8));
                headerText.setText(getText(R.string.intro_header8));
                logoImage.setImageDrawable(
                        ContextCompat.getDrawable(getActivity(),R.drawable.intro_photo_8));
                break;

            case 9:
                descriptionText.setText(getText(R.string.intro_description9));
                headerText.setText(getText(R.string.intro_header9));
                logoImage.setImageDrawable(
                        ContextCompat.getDrawable(getActivity(),R.drawable.intro_photo_9));
                break;

            case 10:
                descriptionText.setText(getText(R.string.intro_description10));
                headerText.setText(getText(R.string.intro_header10));
                logoImage.setImageDrawable(
                        ContextCompat.getDrawable(getActivity(),R.drawable.intro_photo_10));
                break;

            case 11:
                Shader textShader2 = new LinearGradient(0, 0, 0,
                        logoText.getPaint().getTextSize(),
                        new int[]{getResources().getColor(R.color.colorPinkFlickr, null),
                                getResources().getColor(R.color.colorBlueFlickr, null)},
                        new float[]{0, 1}, Shader.TileMode.CLAMP);
                logoText.getPaint().setShader(textShader2);
                logoText.setText(getText(R.string.app_name));
                descriptionText.setText(getString(R.string.app_version,BuildConfig.VERSION_NAME));
                break;
        }
        return v;
    }
}
