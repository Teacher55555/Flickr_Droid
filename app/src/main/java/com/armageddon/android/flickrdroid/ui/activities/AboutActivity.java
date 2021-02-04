package com.armageddon.android.flickrdroid.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.armageddon.android.flickrdroid.R;
import com.armageddon.android.flickrdroid.common.ActivityUtils;
import com.armageddon.android.flickrdroid.ui.fragments.IntroFragment;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

/**
 * Shows Apps logo and version
 */

public class AboutActivity extends SingleFragmentActivity {
    private final static int ABOUT_PAGE = IntroFragment.SLIDE_COUNT;
    public static Intent newIntent (Context context) {
        return new Intent(context, AboutActivity.class);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        ActivityUtils.onActivityCreateSetTheme(this);
        super.onCreate(savedInstanceState);
        findViewById(R.id.divider).setVisibility(View.VISIBLE);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setVisibility(View.VISIBLE);
        toolbar.setTitle(R.string.about);
        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public Fragment getFragment() {
        return IntroFragment.newInstance(ABOUT_PAGE);
    }
}
