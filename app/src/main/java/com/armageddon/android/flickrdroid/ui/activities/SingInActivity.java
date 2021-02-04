package com.armageddon.android.flickrdroid.ui.activities;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.armageddon.android.flickrdroid.common.ActivityUtils;
import com.armageddon.android.flickrdroid.common.QueryPreferences;
import com.armageddon.android.flickrdroid.ui.fragments.SingInFragment;


/**
 * Appears when slides from IntroActivity have come to an end.
 */

public class SingInActivity extends SingleFragmentActivity {

    public static Intent newIntent (Context context) {
        return new Intent(context, SingInActivity.class);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        ActivityUtils.onActivityCreateSetTheme(this);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (QueryPreferences.getUserId(this) != null) {
            Intent intent = UserPersonalPageActivity.newIntent(this,
                    QueryPreferences.getUserId(this),
                    QueryPreferences.getUserName(this),
                    QueryPreferences.getUserIconUrl(this));
            startActivity(intent);
            finish();
        }
    }

    @Override
    public Fragment getFragment() {
        return SingInFragment.newInstance();
    }
}