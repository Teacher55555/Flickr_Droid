package com.armageddon.android.flickrdroid.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.armageddon.android.flickrdroid.R;
import com.armageddon.android.flickrdroid.api.OauthActivity;
import com.armageddon.android.flickrdroid.common.QueryPreferences;
import com.bumptech.glide.Glide;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

/**
 * Slide menu uses DrawerLayout. Appears from right to left.
 * Any activity except PhotoFullActivity and SettingsActivity extends this class.
 */

public abstract class SlideMenuActivity extends AppCompatActivity {
    public static final int REQUEST_CODE = 1;
    private final static String GOOGLE_PLAY_PAGE =
            "https://play.google.com/store/apps/details?id=com.armageddon.android.flickrdroid";
    private DrawerLayout mDrawerLayout;
    private ImageView mUserIcon;
    private TextView mUserName;
    private ImageView mIconSingOut;
    private boolean isSingIn;

    void initMenuItems (DrawerLayout panel, Toolbar toolbar) {
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }
        TextView searchButton = findViewById(R.id.menu_search_button);
        TextView infoButton = findViewById(R.id.menu_info_button);
        TextView rateButton = findViewById(R.id.menu_rate_button);
        TextView settingButton = findViewById(R.id.menu_settings_button);
        mUserIcon = findViewById(R.id.person_icon);
        mUserName = findViewById(R.id.person_real_name);
        mIconSingOut = findViewById(R.id.icon_close);
        mDrawerLayout = panel;


        // if user already logged then shows it logo and name otherwise launch OauthActivity
        if (QueryPreferences.getUserId(this) != null) {
            initUserData();
        } else {
            View.OnClickListener singInListener = v -> {
                isSingIn = true;
                startActivity(OauthActivity.newIntent(SlideMenuActivity.this));
            };
            mUserIcon.setOnClickListener(singInListener);
            mUserName.setOnClickListener(singInListener);
        }

        // logs out and launch SearchActivity in default mode.
        mIconSingOut.setOnClickListener(v -> {
            Context context = SlideMenuActivity.this;
            QueryPreferences.setUserIconUrl(context, null);
            QueryPreferences.setUserName(context,null);
            QueryPreferences.setUserId(context, null);
            QueryPreferences.setOauthToken(context, null);
            QueryPreferences.setOauthTokenSecret(context, null);
            QueryPreferences.setOauthVerifier(context, null);
            mUserName.setText(getString(R.string.sing_in));
            mUserIcon.setImageDrawable(ContextCompat.getDrawable(context
                    ,R.drawable.icon_person_filled));
            mIconSingOut.setVisibility(View.INVISIBLE);
            Intent intent = SearchActivity.newIntent(context, null, SearchActivity.SHOW_MODE,
                    false);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
            finish();
        });


        rateButton.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse(GOOGLE_PLAY_PAGE));
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
        });

        infoButton.setOnClickListener(view -> {
            Intent intent = LogoActivity.newIntent(SlideMenuActivity.this, true);
            startActivity(intent);
        });

        searchButton.setOnClickListener(v -> {
            startActivity(SearchActivity.newIntent(SlideMenuActivity.this,null,
                    SearchActivity.SHOW_MODE, false));
        });

        settingButton.setOnClickListener(v -> {
            startActivityForResult(SettingsActivity.newIntent(SlideMenuActivity.this),
                    REQUEST_CODE );
        });


    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (isSingIn && QueryPreferences.getUserId(this) != null) {
            initUserData();
            Intent intent = SearchActivity.newIntent(this, null,
                    SearchActivity.SHOW_MODE, true);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
        } else {
            mDrawerLayout.closeDrawer(GravityCompat.END, false);
        }

    }

    private void initUserData () {
        mIconSingOut.setVisibility(View.VISIBLE);
        mUserName.setText(QueryPreferences.getUserName(this));
        Glide.with(this)
                .load(QueryPreferences.getUserIconUrl(this))
                .error(R.drawable.logo)
                .into(mUserIcon);

        View.OnClickListener userPageListener = v -> {
            Intent intent = UserPersonalPageActivity.newIntent(SlideMenuActivity.this,
                    QueryPreferences.getUserId(SlideMenuActivity.this),
                    QueryPreferences.getUserName(SlideMenuActivity.this),
                    QueryPreferences.getUserIconUrl(SlideMenuActivity.this));
            startActivity(intent);
        };

        mUserIcon.setOnClickListener(userPageListener);
        mUserName.setOnClickListener(userPageListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.common_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.app_bar_menu_button:
                mDrawerLayout.openDrawer(GravityCompat.END, true);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

}
