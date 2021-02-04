package com.armageddon.android.flickrdroid.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.armageddon.android.flickrdroid.R;
import com.armageddon.android.flickrdroid.common.ActivityUtils;
import com.armageddon.android.flickrdroid.common.Converter;
import com.armageddon.android.flickrdroid.common.QueryPreferences;
import com.armageddon.android.flickrdroid.ui.adapters.UserPrivatePageAdapter;
import com.armageddon.android.flickrdroid.ui.adapters.UserPublicPageAdapter;
import com.armageddon.android.flickrdroid.ui.fragments.GalleryShowFragment;
import com.armageddon.android.flickrdroid.ui.fragments.UserGroupFragment;
import com.armageddon.android.flickrdroid.ui.fragments.UserAlbumFragment;
import com.armageddon.android.flickrdroid.ui.fragments.UserGalleryFragment;
import com.bumptech.glide.Glide;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.tabs.TabLayout;


import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

/**
 * Users personal page. 2 modes:
 * 1. If user logged in - adds UserPrivatePagerAdapter to ViewPager
 * 2. If user not logged in - adds UserPublicPagerAdapter to ViewPager
 * Listens current opened tab in ViewPager and provides corresponding link to Flickr.com
 * if user click on users logo.
 */

public class UserPersonalPageActivity extends SlideMenuActivity
        implements GalleryShowFragment.CallBacks, UserGalleryFragment.CallBacks, UserGroupFragment.CallBacks,
        UserAlbumFragment.CallBacks, Converter {

    private static final String ITEM_ID = "item";
    private static final String ITEM_NAME = "item_name";
    private static final String ITEM_ICON = "item_icon";
    private static final String FLICKR_PEOPLE_LINK = "https://www.flickr.com/people/";
    private static final String FLICKR_PHOTOS_LINK = "https://www.flickr.com/photos/";
    private static final String FLICKR_FRIENDS_LINK = "https://www.flickr.com/photos/friends";
    private static final String FLICKR_CAMERA_ROLL_LINK = "https://www.flickr.com/cameraroll";


    protected AppBarLayout mAppBarLayout;
    protected ImageView mItemIcon;
    protected Toolbar mFakeToolbar;
    protected Toolbar mRealToolbar;
    protected ViewPager mViewPager;
    protected TabLayout mTabLayout;
    protected TextView mItemName;
    protected ProgressBar mProgressBar;
    protected LinearLayout mDescriptionLayout;
    protected ImageView mBackButton;
    protected DrawerLayout mDrawerLayout;
    protected String mId;
    protected String mName;

    public static Intent newIntent(Context context,
                                   String userId,
                                   String itemName,
                                   String itemIconUrl) {
        Intent intent = new Intent(context, UserPersonalPageActivity.class);
        intent.putExtra(ITEM_ID, userId);
        intent.putExtra(ITEM_NAME, itemName);
        intent.putExtra(ITEM_ICON, itemIconUrl);
        return intent;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ActivityUtils.onActivityCreateSetTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_personal_page);

        mId = getIntent().getStringExtra(ITEM_ID);
        mName = getIntent().getStringExtra(ITEM_NAME);
        String iconUrl = getIntent().getStringExtra(ITEM_ICON);

        mAppBarLayout = findViewById(R.id.appBar_layout);
        mItemIcon = findViewById(R.id.item_icon);
        mFakeToolbar = findViewById(R.id.fake_toolbar);
        mRealToolbar = findViewById(R.id.toolbar);
        mViewPager = findViewById(R.id.view_pager);
        mTabLayout = findViewById(R.id.tab_layout);
        mItemName = findViewById(R.id.item_title);
        mProgressBar = findViewById(R.id.progress_bar);
        mDescriptionLayout = findViewById(R.id.item_description_layout);
        mBackButton = findViewById(R.id.icon_back);
        mDrawerLayout = findViewById(R.id.drawer_layout);
        initMenuItems(mDrawerLayout, mRealToolbar);

        mItemIcon.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.parse(FLICKR_PEOPLE_LINK + mId),"text/html");
            startActivity(intent);
        });

        String userOwnerId = QueryPreferences.getUserId(this);

        // if user NOT logged in
        if (userOwnerId == null || !userOwnerId.equals(mId)) {
            mViewPager.setAdapter(
                    new UserPublicPageAdapter(this,
                            getSupportFragmentManager(),
                            PagerAdapter.POSITION_UNCHANGED, mId, mName));


            mItemIcon.setOnClickListener(v -> {
                String url = "";
                switch (mViewPager.getCurrentItem()) {
                    case 0: url = FLICKR_PHOTOS_LINK + mId; break;
                    case 1: url = FLICKR_PHOTOS_LINK + mId + "/albums"; break;
                    case 2: url = FLICKR_PHOTOS_LINK + mId + "/galleries"; break;
                    case 3: url = FLICKR_PEOPLE_LINK + mId + "/groups"; break;
                    case 4: url = FLICKR_PEOPLE_LINK + mId; break;
                }
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse(url),"text/html");
                startActivity(intent);
            });

        // if user logged in
        } else {
            mViewPager.setAdapter(
                    new UserPrivatePageAdapter(this,
                            getSupportFragmentManager(),
                            PagerAdapter.POSITION_UNCHANGED, mId, mName));
            mItemIcon.setOnClickListener(v -> {
                String url = "";
                switch (mViewPager.getCurrentItem()) {
                    case 0: url = FLICKR_FRIENDS_LINK; break;
                    case 1: url = FLICKR_CAMERA_ROLL_LINK; break;
                    case 2: url = FLICKR_PHOTOS_LINK + mId; break;
                    case 3: url = FLICKR_PHOTOS_LINK + mId + "/albums"; break;
                    case 4: url = FLICKR_PHOTOS_LINK + mId + "/galleries"; break;
                    case 5: url = FLICKR_PEOPLE_LINK + mId + "/groups"; break;
                    case 6: url = FLICKR_PEOPLE_LINK + mId; break;
                }
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse(url),"text/html");
                startActivity(intent);
            });
        }


        mTabLayout.setupWithViewPager(mViewPager, true);
        mItemName.setText(mName);
        Glide.with(this)
                .load(iconUrl)
                .error(ContextCompat.getDrawable(this,R.drawable.icon_person_filled))
                .centerCrop()
                .into(mItemIcon);

    }

    @Override
    public void setProgressBar(boolean on) {
        if (on) {
            mProgressBar.setVisibility(View.VISIBLE);
        } else {
            mProgressBar.setVisibility(View.GONE);
        }
    }

}

