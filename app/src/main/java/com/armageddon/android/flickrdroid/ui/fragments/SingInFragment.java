package com.armageddon.android.flickrdroid.ui.fragments;

import android.content.Intent;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.armageddon.android.flickrdroid.R;
import com.armageddon.android.flickrdroid.api.OauthActivity;
import com.armageddon.android.flickrdroid.common.QueryPreferences;
import com.armageddon.android.flickrdroid.ui.activities.SearchActivity;
import com.armageddon.android.flickrdroid.ui.activities.UserPersonalPageActivity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

/**
 * Asks user what to do after intro was shown
 * - log in
 * - SearchActivity.class
 */

public class SingInFragment extends Fragment {
    Button mSingIn, mSingInLater;
    TextView mLogoText;

    public static SingInFragment newInstance() {

        Bundle args = new Bundle();

        SingInFragment fragment = new SingInFragment();
        fragment.setArguments(args);
        return fragment;

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_singin, container, false);
        mSingIn = v.findViewById(R.id.sing_in_button);
        mSingInLater = v.findViewById(R.id.sing_in_later_button);
        mLogoText = v.findViewById(R.id.header_text_view);
        Shader textShader = new LinearGradient(0, 0, 0,
                mLogoText.getPaint().getTextSize(),
                new int[]{getResources().getColor(R.color.colorPinkFlickr, null),
                        getResources().getColor(R.color.colorBlueFlickr, null)},
                new float[]{0, 1}, Shader.TileMode.CLAMP);
        mLogoText.getPaint().setShader(textShader);
        mLogoText.setText(getText(R.string.app_name));

        if (QueryPreferences.getUserId(getActivity()) == null) {
            mSingIn.setOnClickListener(
                    v1 -> startActivity(OauthActivity.newIntent(getActivity())));
        } else {
            mSingIn.setText(getString(R.string.personal_page));
            mSingInLater.setText(getString(R.string.search_photos));

            mSingIn.setOnClickListener(v12 -> {
                Intent intent = UserPersonalPageActivity.newIntent(getActivity(),
                        QueryPreferences.getUserId(getActivity()),
                        QueryPreferences.getUserName(getActivity()),
                        QueryPreferences.getUserIconUrl(getActivity()));
                startActivity(intent);
                getActivity().finish();
            });
        }

        mSingInLater.setOnClickListener(
                l -> startActivity(SearchActivity.newIntent(getActivity(),
                        null, SearchActivity.SHOW_MODE, false)));
        return v;
    }
}
