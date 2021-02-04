package com.armageddon.android.flickrdroid.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.armageddon.android.flickrdroid.R;
import com.armageddon.android.flickrdroid.common.ActivityUtils;
import com.armageddon.android.flickrdroid.ui.fragments.SettingsFragment;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

/**
 * Settings class. Contains SettingsFragment.
 * Launch SearchActivity if user switches APP theme.
 */

public class SettingsActivity extends AppCompatActivity implements SettingsFragment.CallBacks {

    private static final String GALLERY_IN_ROW = "gallery_in_row";
    private static final String THEME_FLAG = "theme_flag";
    private boolean isThemeChanged;

    public static Intent newIntent(Context context, boolean isThemeChanged) {
        Intent intent = new Intent(context, SettingsActivity.class);
        intent.putExtra(THEME_FLAG, isThemeChanged);
        return intent;
    }

    public static Intent newIntent(Context context) {
        return new Intent(context, SettingsActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ActivityUtils.onActivityCreateSetTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        isThemeChanged = getIntent().getBooleanExtra(THEME_FLAG, false);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.fragment_container);

        if (fragment == null) {
            fragment = SettingsFragment.newInstance();
            fm.beginTransaction()
                    .add(R.id.fragment_container, fragment)
                    .commit();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        if (isThemeChanged) {
           Intent intent = SearchActivity.newIntent(this,
                   null, SearchActivity.SHOW_MODE, false);
           intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
           startActivity(intent);
        } else {
            super.onBackPressed();
        }
    }


    /**
     * Sends signal to previous activities if user changed the number of rows
     */
    @Override
    public void onGalleryInRowChanged() {
        Intent data = new Intent();
        data.putExtra(GALLERY_IN_ROW,true);
        setResult(RESULT_OK,data);
    }
}
