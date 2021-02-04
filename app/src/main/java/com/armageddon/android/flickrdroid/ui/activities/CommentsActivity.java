package com.armageddon.android.flickrdroid.ui.activities;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.armageddon.android.flickrdroid.R;
import com.armageddon.android.flickrdroid.common.ActivityUtils;
import com.armageddon.android.flickrdroid.ui.fragments.CommentsFragment;

import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;



public class CommentsActivity extends SlideMenuActivity {
    private static final String PHOTO_ID = "photo_id";

    public static Intent newIntent(Context context, String photoId) {
        Intent intent = new Intent(context, CommentsActivity.class);
        intent.putExtra(PHOTO_ID, photoId);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ActivityUtils.onActivityCreateSetTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_comments);

        Toolbar toolbar = findViewById(R.id.toolbar);
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        initMenuItems(drawerLayout, toolbar);


        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.fragment_container);

        if (fragment == null) {
            fragment = CommentsFragment
                    .newInstance(getIntent().getStringExtra(PHOTO_ID));
            fm.beginTransaction()
                    .add(R.id.fragment_container, fragment)
                    .commit();
        }
    }
}