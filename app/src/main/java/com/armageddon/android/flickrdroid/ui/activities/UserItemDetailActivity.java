package com.armageddon.android.flickrdroid.ui.activities;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.armageddon.android.flickrdroid.R;
import com.armageddon.android.flickrdroid.common.ActivityUtils;
import com.armageddon.android.flickrdroid.common.Converter;
import com.armageddon.android.flickrdroid.ui.adapters.UserAlbumAdapter;
import com.armageddon.android.flickrdroid.ui.adapters.UserFavesAdapter;
import com.armageddon.android.flickrdroid.ui.adapters.UserGalleryAdapter;
import com.armageddon.android.flickrdroid.ui.adapters.UserGroupAdapter;
import com.armageddon.android.flickrdroid.ui.fragments.GalleryShowFragment;
import com.armageddon.android.flickrdroid.ui.fragments.UserGroupFragment;
import com.armageddon.android.flickrdroid.ui.fragments.UserAlbumFragment;
import com.armageddon.android.flickrdroid.ui.fragments.UserGalleryFragment;
import com.bumptech.glide.Glide;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.tabs.TabLayout;

import java.util.Objects;

import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;


/**
 * Common class for user's elements: albums, favorites, galleries, groups.
 * Checks type of element and adds matching adapter to ViewPager.
 * Controls UI in ToolBar (hide, show, animation).
 * Provides links to Flickr.com when user click on element's logo.
 */

public class UserItemDetailActivity extends SlideMenuActivity
        implements GalleryShowFragment.CallBacks, UserGalleryFragment.CallBacks, UserGroupFragment.CallBacks,
        UserAlbumFragment.CallBacks, Converter {

    private static final String OWNER_ID = "owner_id";
    private static final String ITEM_ID = "item";
    private static final String ITEM_NAME = "item_name";
    private static final String ITEM_ICON = "item_icon";
    private static final String ITEM_DESCRIPTION = "item_description";
    private static final String ITEM_MEMBERS = "item_members";
    private static final String ITEM_COUNT = "items_count";
    private static final String VIEWS_COUNT = "views_count";
    private static final String COMMENTS_COUNT = "comments_count";
    private static final String ITEM_ADAPTER = "item_adapter";
    private static final String FLICKR_PHOTOS_LINK = "https://www.flickr.com/photos/";
    private static final String FLICKR_GROUPS_LINK = "https://www.flickr.com/groups/";
    private static final String FLICKR_ALBUMS_LINK = "/albums/";
    private static final String FLICKR_GALLERIES_LINK = "/galleries/";
    private static final String FLICKR_FAVORITES_LINK = "/favorites";


    private static final float OFFSET_OLD_MIN = 0;
    private static final float OFFSET_OLD_MAX = -150;
    private static final float OFFSET_NEW_MIN = 1;
    private static final float OFFSET_NEW_MAX = 0;
    private static final float OFFSET_OLD_RANGE = (OFFSET_OLD_MAX - OFFSET_OLD_MIN);
    private static final float OFFSET_NEW_RANGE = (OFFSET_NEW_MAX - OFFSET_NEW_MIN);

    public enum AdapterTypes {
        USER_ALBUM,
        USER_FAVES,
        USER_GALLERY,
        USER_GROUP
    }

    protected AppBarLayout mAppBarLayout;
    protected ImageView mItemIcon;
    protected Toolbar mFakeToolbar;
    protected Toolbar mRealToolbar;
    protected ViewPager mViewPager;
    protected TabLayout mTabLayout;
    protected TextView mItemName;
    protected ProgressBar mProgressBar;
    protected LinearLayout mDescriptionLayout;
    protected TextView mDescription;
    protected TextView mDesc1;
    protected TextView mDesc2;
    protected TextView mDesc3;
    protected View mToolbarBack;
    protected ColorStateList mColorStateList;
    protected DrawerLayout mDrawerLayout;

    public static Intent newIntent(Context context,
                                   String ownerId,
                                   String itemId,
                                   String itemName,
                                   String itemIconUrl,
                                   String itemDescription,
                                   String itemMembers,
                                   String itemsCount,
                                   String viewsCount,
                                   String commentsCount,
                                   AdapterTypes adapterType) {
        Intent intent = new Intent(context, UserItemDetailActivity.class);
        intent.putExtra(OWNER_ID, ownerId);
        intent.putExtra(ITEM_ID, itemId);
        intent.putExtra(ITEM_NAME, itemName);
        intent.putExtra(ITEM_ICON, itemIconUrl);
        intent.putExtra(ITEM_DESCRIPTION, itemDescription);
        intent.putExtra(ITEM_MEMBERS, itemMembers);
        intent.putExtra(ITEM_COUNT, itemsCount);
        intent.putExtra(VIEWS_COUNT, viewsCount);
        intent.putExtra(COMMENTS_COUNT, commentsCount);
        intent.putExtra(ITEM_ADAPTER, adapterType);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ActivityUtils.onActivityCreateSetTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_item_detail);
        String ownerId = getIntent().getStringExtra(OWNER_ID);
        String id = getIntent().getStringExtra(ITEM_ID);
        String name = getIntent().getStringExtra(ITEM_NAME);
        String iconUrl = getIntent().getStringExtra(ITEM_ICON);
        String description = getIntent().getStringExtra(ITEM_DESCRIPTION);
        String members = getIntent().getStringExtra(ITEM_MEMBERS);
        String itemsCount = getIntent().getStringExtra(ITEM_COUNT);
        String viewsCount = getIntent().getStringExtra(VIEWS_COUNT);

        mDrawerLayout = findViewById(R.id.drawer_layout);
        mAppBarLayout = findViewById(R.id.appBar_layout);
        mItemIcon = findViewById(R.id.item_icon);
        mFakeToolbar = findViewById(R.id.fake_toolbar);
        mViewPager = findViewById(R.id.view_pager);
        mTabLayout = findViewById(R.id.tab_layout);
        mProgressBar = findViewById(R.id.progress_bar);
        mDescriptionLayout = findViewById(R.id.item_description_layout);
        mDescription = findViewById(R.id.item_description);
        mDesc1 = findViewById(R.id.item_desc_1);
        mDesc2 = findViewById(R.id.item_desc_2);
        mDesc3 = findViewById(R.id.item_desc_3);
        mToolbarBack = findViewById(R.id.toolbar_background);
        mRealToolbar = findViewById(R.id.toolbar);

        initMenuItems(mDrawerLayout, mRealToolbar);

        Drawable errorDrawable = ContextCompat.getDrawable(this,R.drawable.logo);

        Glide.with(this)
                .load(iconUrl)
                .error(errorDrawable)
                .centerCrop()
                .into(mItemIcon);
        mDescription.setText(name);
        mDesc1.setText(itemsCount);

        AdapterTypes adapterType = (AdapterTypes) getIntent().getSerializableExtra(ITEM_ADAPTER);
        assert adapterType != null;
        switch (adapterType) {
            case USER_ALBUM:
                mItemIcon.setOnClickListener(v -> {
                    String [] idArray = id.split(" ");
                    String url = FLICKR_PHOTOS_LINK + idArray[0] + FLICKR_ALBUMS_LINK + idArray[1];
                    openLinkInWebBrowser(url);
                });
                mDesc2.setVisibility(View.VISIBLE);
                mDesc2.setText(viewsCount);
                mViewPager.setAdapter(
                        new UserAlbumAdapter(this,
                                getSupportFragmentManager(),
                                PagerAdapter.POSITION_UNCHANGED, id, description));
                if (description == null) {
                    mTabLayout.setVisibility(View.GONE);
                    mFakeToolbar.setVisibility(View.GONE);
                } else {
                    mTabLayout.setupWithViewPager(mViewPager, true);
                }
                setTitleTextColors();
                break;
            case USER_GALLERY:
                mItemIcon.setOnClickListener(v -> {
                    String url = FLICKR_PHOTOS_LINK + ownerId + FLICKR_GALLERIES_LINK + id;
                    openLinkInWebBrowser(url);
                });
                mDesc2.setVisibility(View.VISIBLE);
                mDesc2.setText(viewsCount);
                mViewPager.setAdapter(
                        new UserGalleryAdapter(this,
                                getSupportFragmentManager(),
                                PagerAdapter.POSITION_UNCHANGED, id ,description));
                if (description == null) {
                    mTabLayout.setVisibility(View.GONE);
                    mFakeToolbar.setVisibility(View.GONE);
                } else {
                    mTabLayout.setupWithViewPager(mViewPager, true);
                }
                setTitleTextColors();
                break;

            case USER_GROUP:
                mItemIcon.setOnClickListener(v -> {
                    String url = FLICKR_GROUPS_LINK + id;
                    openLinkInWebBrowser(url);
                });
                mDesc2.setVisibility(View.VISIBLE);
                mDesc2.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_people_search_stroke
                        , 0, 0, 0);
                mDesc2.setText(members);
                mViewPager.setAdapter(
                        new UserGroupAdapter(this,
                                getSupportFragmentManager(),
                                PagerAdapter.POSITION_UNCHANGED, id));
                mTabLayout.setupWithViewPager(mViewPager, true);
                setTitleTextColors();
                break;

            case USER_FAVES:
                mItemIcon.setOnClickListener(v -> {
                    String url = FLICKR_PHOTOS_LINK + ownerId + FLICKR_FAVORITES_LINK;
                    openLinkInWebBrowser(url);
                });
                mTabLayout.setVisibility(View.GONE);
                mFakeToolbar.setVisibility(View.GONE);
                mViewPager.setAdapter(
                        new UserFavesAdapter(
                                getSupportFragmentManager(),
                                PagerAdapter.POSITION_UNCHANGED, id));
                break;
        }

        mViewPager.setOffscreenPageLimit(Objects.requireNonNull(mViewPager.getAdapter()).getCount());
    }

    private void setTitleTextColors () {
        mColorStateList = mTabLayout.getTabTextColors();
        mAppBarLayout.addOnOffsetChangedListener((appBarLayout, verticalOffset) -> {
            float offsetNewValue = (((verticalOffset - OFFSET_OLD_MIN) * OFFSET_NEW_RANGE)
                    / OFFSET_OLD_RANGE) + OFFSET_NEW_MIN;
            mDescription.setAlpha(offsetNewValue);
            mDesc1.setAlpha(offsetNewValue);
            mDesc2.setAlpha(offsetNewValue);
        });
        mAppBarLayout.addOnOffsetChangedListener(new AppBarStateChangeListener() {
            @Override
            public void onStateChanged(AppBarLayout appBarLayout, State state) {
                if (state == State.EXPANDED) {
                    mTabLayout.setTabTextColors(
                            ContextCompat.getColorStateList(
                                    UserItemDetailActivity.this,
                                    R.color.tab_user_item_expandet_selector));
                } else if (state == State.COLLAPSED) {
                    mTabLayout.setTabTextColors(mColorStateList);
                }
            }
        });
    }

    private void openLinkInWebBrowser(String url) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.parse(url),"text/html");
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(this, R.string.internet_connection_error, Toast.LENGTH_SHORT)
                    .show();
        }

    }


    @Override
    public void setProgressBar(boolean on) {
        if (on) {
            mProgressBar.setVisibility(View.VISIBLE);
        } else {
            mProgressBar.setVisibility(View.GONE);
        }
    }


     abstract static class AppBarStateChangeListener implements AppBarLayout.OnOffsetChangedListener {
         enum State {
            EXPANDED,
            COLLAPSED,
            IDLE
        }

        private State mCurrentState = State.IDLE;

        @Override
        public final void onOffsetChanged(AppBarLayout appBarLayout, int i) {
            if (i == 0) {
                if (mCurrentState != State.EXPANDED) {
                    onStateChanged(appBarLayout, State.EXPANDED);
                }
                mCurrentState = State.EXPANDED;
            } else if (Math.abs(i) >= appBarLayout.getTotalScrollRange()) {
                if (mCurrentState != State.COLLAPSED) {
                    onStateChanged(appBarLayout, State.COLLAPSED);
                }
                mCurrentState = State.COLLAPSED;
            } else {
                if (mCurrentState != State.IDLE) {
                    onStateChanged(appBarLayout, State.IDLE);
                }
                mCurrentState = State.IDLE;
            }
        }
        public abstract void onStateChanged(AppBarLayout appBarLayout, State state);
    }
}

