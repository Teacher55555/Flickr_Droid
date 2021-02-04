package com.armageddon.android.flickrdroid.ui.activities;

import androidx.fragment.app.Fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.armageddon.android.flickrdroid.common.ActivityUtils;
import com.armageddon.android.flickrdroid.ui.fragments.LogoFragment;


/**
 * Intro Animation Activity
 */

public class LogoActivity extends SingleFragmentActivity {
    private static final String SHOW_INTRO = "show_intro";
    private boolean showIntro;

    public static Intent newIntent (Context context, boolean showIntro) {
        Intent intent = new Intent(context, LogoActivity.class);
        intent.putExtra(SHOW_INTRO, showIntro);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ActivityUtils.onActivityCreateSetTheme(this);
        showIntro = getIntent().getBooleanExtra(SHOW_INTRO, false);
        super.onCreate(savedInstanceState);
    }

    @Override
    public Fragment getFragment() {
        return LogoFragment.newInstance(showIntro);
    }
}