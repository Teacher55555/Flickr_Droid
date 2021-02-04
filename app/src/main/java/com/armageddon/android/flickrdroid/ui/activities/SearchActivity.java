package com.armageddon.android.flickrdroid.ui.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.armageddon.android.flickrdroid.R;
import com.armageddon.android.flickrdroid.common.ActivityUtils;
import com.armageddon.android.flickrdroid.common.Converter;
import com.armageddon.android.flickrdroid.common.EnumCategory;
import com.armageddon.android.flickrdroid.ui.adapters.CategoryItemAdapter;
import com.armageddon.android.flickrdroid.ui.adapters.SearchGalleryTabsAdapter;
import com.armageddon.android.flickrdroid.ui.adapters.SearchTabsAdapter;
import com.armageddon.android.flickrdroid.ui.fragments.DaterPickerFragment;
import com.armageddon.android.flickrdroid.ui.fragments.GalleryShowFragment;
import com.armageddon.android.flickrdroid.ui.fragments.UserGroupFragment;
import com.armageddon.android.flickrdroid.ui.fragments.MapFragment;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.tabs.TabLayout;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

/**
 * This class combines all the search capabilities and switches between two adapters:
 * 1. SearchGalleryTabsAdapter
 *    - interesting photo for an any date
 *    - recently uploaded photo
 *    - photos on the map
 *
 * 2. GalleryItemAdapter
 *    - search photo by name, tag or description
 *    - search groups
 *    - search user by name or e-mail
 *
 * Each adapter manages icons in tabs and corresponding fragments. Also there is a status bar
 * with search icons and category filters in horizontal recyclerview.
 * By default SearchGalleryTabsAdapter is active.
 *
 */


public class SearchActivity extends SlideMenuActivity implements DaterPickerFragment.CallBacks,
        CategoryItemAdapter.CallBacks, GalleryShowFragment.CallBacks, UserGroupFragment.CallBacks,
        Converter {
    public static final int SHOW_MODE = 0;
    public static final int SEARCH_MODE = 1;
    private static final int OFFSCREEN_PAGE_LIMIT = 2;
    private static final String SEARCH_TRIGGER = "search_trigger";
    private static final String TITLE = "title";
    private static final String ACTIVITY_MODE = "activity_mode";
    private static final String OPEN_MENU_FLAG = "menu_panel";
    private static final String CATEGORY_FLAG = "category_flag";

    private ViewPager mViewPager;
    private TabLayout mTabLayout;
    private Toolbar mToolbar;
    private DrawerLayout mDrawerLayout;
    private SearchView mBarSearchView;
    private int mViewPagerBeforeSearchPosition;
    private int mViewPagerSearchPosition;
    private  MenuItem mSearchItem;
    private MenuItem mBurgerMenuItem;
    private MenuItem mCloseButton;
    private MenuItem mDatePickerButton;
    private Calendar mCalendar;
    private String mToolBarTitle;
    private boolean mSetCategoryFlag;
    private RecyclerView mCategoryRecyclerView;
    private ProgressBar mProgressBar;
    private AppBarLayout mAppBarLayout;
    private String mQuery;
    private int mActivityMode;

    public static Intent newIntent(Context context, String query, int showMode, boolean openMenuPanel) {
        Intent intent = new Intent(context, SearchActivity.class);
        intent.putExtra(SEARCH_TRIGGER, query);
        intent.putExtra(OPEN_MENU_FLAG, openMenuPanel);
        intent.putExtra(ACTIVITY_MODE, showMode);
        return intent;
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        ActivityUtils.onActivityCreateSetTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        mCategoryRecyclerView = findViewById(R.id.category_recycle_view);
        mAppBarLayout = findViewById(R.id.appBar_layout);
        mProgressBar = findViewById(R.id.progress_bar);
        mDrawerLayout = findViewById(R.id.drawer_layout);
        mViewPager = findViewById(R.id.view_pager);
        mTabLayout = findViewById(R.id.tab_layout);
        mToolbar = findViewById(R.id.toolbar);
        initMenuItems(mDrawerLayout, mToolbar);

        // checks: need to open slide menu or not
        if (getIntent().getBooleanExtra(OPEN_MENU_FLAG, false)) {
            mDrawerLayout.openDrawer(GravityCompat.END, true);
        }

        mQuery = getIntent().getStringExtra(SEARCH_TRIGGER);
        mActivityMode = getIntent().getIntExtra(ACTIVITY_MODE,0);

        if (savedInstanceState != null) {
            mQuery = savedInstanceState.getString(SEARCH_TRIGGER);
            mToolBarTitle = savedInstanceState.getString(TITLE);
            mSetCategoryFlag = savedInstanceState.getBoolean(CATEGORY_FLAG);
            mActivityMode = savedInstanceState.getInt(ACTIVITY_MODE);

        }

        // checks: witch adapter needs to use
        if (mActivityMode == SHOW_MODE)  {
            setUpGalleryShowAdapter(null, null);
        } else {
            setUpSearchAdapter(mQuery);
            mToolbar.setTitle(mQuery);
            Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
            AppBarLayout.LayoutParams params =
                    (AppBarLayout.LayoutParams) mTabLayout.getLayoutParams();
            params.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL
                    | AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS);
        }

        mViewPager.setOffscreenPageLimit(OFFSCREEN_PAGE_LIMIT);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_activity_menu, menu);

        mCloseButton = menu.findItem(R.id.app_bar_close);
        mDatePickerButton = menu.findItem(R.id.app_bar_calendar);

        if (mSetCategoryFlag) {
            mCloseButton.setVisible(true);
            mDatePickerButton.setVisible(false);
        }
        mSearchItem = menu.findItem(R.id.app_bar_search_button);
        mBurgerMenuItem = menu.findItem(R.id.app_bar_menu_button);
        mBarSearchView = ((SearchView) mSearchItem.getActionView());

        if (mQuery != null) {
            mBurgerMenuItem.setVisible(false);
            mDatePickerButton.setVisible(false);
            mTabLayout.removeOnTabSelectedListener(itemsSearchListener);
            mTabLayout.addOnTabSelectedListener(itemsSearchListener);
            mToolbar.setOnClickListener(v -> {
                mBarSearchView.setIconified(false);
                mBarSearchView.setQuery(mQuery,false);
            });
        }

        // controls the size and color of the hint text when clicking on the search button
        mBarSearchView.setBackgroundColor(Color.TRANSPARENT);
        mBarSearchView.setMaxWidth(Integer.MAX_VALUE);
        mBarSearchView.setQueryHint(getString(R.string.search_photo_hint));
        LinearLayout linearLayout1 = (LinearLayout) mBarSearchView.getChildAt(0);
        LinearLayout linearLayout2 = (LinearLayout) linearLayout1.getChildAt(2);
        LinearLayout linearLayout3 = (LinearLayout) linearLayout2.getChildAt(1);
        AutoCompleteTextView autoComplete = (AutoCompleteTextView) linearLayout3.getChildAt(0);
        autoComplete.setTextSize(16);
        autoComplete.setHintTextColor(ContextCompat.getColor(this,R.color.colorGreyLight));

        // changes adapter in activity to SearchAdapter
        mBarSearchView.setOnSearchClickListener(v -> {
            if (mViewPager.getCurrentItem() != 2) {
                mViewPagerBeforeSearchPosition = mViewPager.getCurrentItem();
                Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
                mCloseButton.setVisible(false);
                mDatePickerButton.setVisible(false);
                setUpSearchAdapter(null);
                mBarSearchView.requestFocus();

            }

        });
        mBarSearchView.setOnCloseListener(() -> {
            closeSearchOptionsMenu();
            return false;
        });


        mBarSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mQuery = query;
                Objects.requireNonNull(getSupportActionBar()).setTitle(query);
                mBarSearchView.onActionViewCollapsed();
                mBurgerMenuItem.setVisible(false);
                mViewPagerSearchPosition = mViewPager.getCurrentItem();
                setUpSearchAdapter(query);
                mToolbar.setOnClickListener(v -> {
                    mBarSearchView.setIconified(false);
                    mBarSearchView.setQuery(query,false);
                });

                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
            case R.id.app_bar_close:
                    closeSearchOptionsMenu();
                break;
            case R.id.app_bar_menu_button:
                mDrawerLayout.openDrawer(GravityCompat.END);
                break;
            case R.id.app_bar_calendar:
                DaterPickerFragment dialog = DaterPickerFragment.newInstance(mCalendar);
                dialog.show(getSupportFragmentManager(),null);
                mSetCategoryFlag = false;
                mCloseButton.setVisible(false);
                break;
        }
        return true;
    }

    /**
     * changes buttons in status bar and setups GalleryShowAdapter and shows it buttons.
     */

    private void closeSearchOptionsMenu () {
        mViewPagerSearchPosition = 0;
        mToolbar.setOnClickListener(null);
        mSetCategoryFlag = false;
        mCloseButton.setVisible(false);
        mBarSearchView.clearFocus();
        mBarSearchView.onActionViewCollapsed();
        mBurgerMenuItem.setVisible(true);
        mDatePickerButton.setVisible(true);
        mSearchItem.setVisible(true);
        mToolBarTitle = null;
        setUpGalleryShowAdapter(null, null);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(false);
    }

    /**
     * Setups SearchGalleryTabsAdapter with ViewPager.
     * Setups tab buttons icons in active and inactive mode.
     * Setups tabs listener.
     */

    private void setUpGalleryShowAdapter (String category, String query) {
        mActivityMode = SHOW_MODE;

        mViewPager.setAdapter(new SearchGalleryTabsAdapter(
                getSupportFragmentManager(),PagerAdapter.POSITION_UNCHANGED, category, query));
        mTabLayout.setupWithViewPager(mViewPager, true);
        if (category == null) {
            mCategoryRecyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
            mCategoryRecyclerView.setAdapter(new CategoryItemAdapter(this, mToolBarTitle));
            mCategoryRecyclerView.setHasFixedSize(true);
            mCategoryRecyclerView.setVisibility(View.VISIBLE);
        }

        Objects.requireNonNull(mTabLayout.getTabAt(0))
                .setIcon(R.drawable.icon_top_stroke);
        Objects.requireNonNull(mTabLayout.getTabAt(1))
                .setIcon(R.drawable.icon_search_recent_stroke);
        Objects.requireNonNull(mTabLayout.getTabAt(2))
                .setIcon(R.drawable.icon_search_map_stroke);

        switch (mViewPager.getCurrentItem()) {
            case 0:
                Objects.requireNonNull(mTabLayout.getTabAt(0))
                        .setIcon(R.drawable.icon_top_filled);
                if (mToolBarTitle == null) {
                    mToolbar.setTitle(getString(R.string.search_top));
                } else {
                    mToolbar.setTitle(mToolBarTitle);
                }
                break;
            case 1:
                Objects.requireNonNull(mTabLayout.getTabAt(1))
                        .setIcon(R.drawable.icon_search_recent_filled);
                if (mToolBarTitle == null) {
                    mToolbar.setTitle(getString(R.string.search_recent));
                } else {
                    mToolbar.setTitle(mToolBarTitle);
                }
                break;
            case 2:
                Objects.requireNonNull(mTabLayout.getTabAt(2))
                        .setIcon(R.drawable.icon_map_search_filled);
                mToolbar.setTitle(getString(R.string.search_on_map));
                break;

        }
        mTabLayout.removeOnTabSelectedListener(itemsSearchListener);
        mTabLayout.addOnTabSelectedListener(galleryShowListener);
        mViewPager.setCurrentItem(mViewPagerBeforeSearchPosition);
    }

    /**
     * Setups SearchGalleryAdapter with ViewPager.
     * Setups tab buttons icons in active and inactive mode.
     * Setups tabs listener.
     */

    private void setUpSearchAdapter (String query) {
        mActivityMode = SEARCH_MODE;

        mCategoryRecyclerView.setVisibility(View.GONE);
            mViewPager.setAdapter(new SearchTabsAdapter(
                    getSupportFragmentManager(), PagerAdapter.POSITION_UNCHANGED, query));
            mTabLayout.setupWithViewPager(mViewPager, true);
        Objects.requireNonNull(mTabLayout.getTabAt(0))
                .setText(getString(R.string.photo))
                .setIcon(R.drawable.icon_photo_search_stroke);
        Objects.requireNonNull(mTabLayout.getTabAt(1))
                .setText(getString(R.string.person))
                .setIcon(R.drawable.icon_person_stroke);
        Objects.requireNonNull(mTabLayout.getTabAt(2))
                .setText(getString(R.string.groups))
                .setIcon(R.drawable.icon_group_search_stroke);
        switch (mViewPager.getCurrentItem()) {

            case 0:
                Objects.requireNonNull(mTabLayout.getTabAt(0))
                        .setIcon(R.drawable.icon_photo_search_filled); break;
            case 1:
                Objects.requireNonNull(mTabLayout.getTabAt(1))
                        .setIcon(R.drawable.icon_person_filled); break;
            case 2:
                Objects.requireNonNull(mTabLayout.getTabAt(2))
                        .setIcon(R.drawable.icon_group_search_filled); break;
        }
//
        mTabLayout.removeOnTabSelectedListener(galleryShowListener);
        mTabLayout.addOnTabSelectedListener(itemsSearchListener);
        mViewPager.setCurrentItem(mViewPagerSearchPosition);
    }


    /** Listener for search TABS between PHOTO, PEOPLE, GROUPS */

    private final TabLayout.OnTabSelectedListener itemsSearchListener =
            new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    switch (tab.getPosition()) {
                        case 0:
                            tab.setIcon(R.drawable.icon_photo_search_filled);
                            if (mBarSearchView != null) {
                                mBarSearchView.setQueryHint(getString(R.string.search_photo_hint));
                            }
                            break;
                        case 1:
                            tab.setIcon(R.drawable.icon_person_filled);
                            if (mBarSearchView != null) {
                                mBarSearchView.setQueryHint(getString(R.string.search_user_hint));
                            }
                            break;
                        case 2:
                            tab.setIcon(R.drawable.icon_group_search_filled);
                            if (mBarSearchView != null) {
                                mBarSearchView.setQueryHint(getString(R.string.search_group_hint));
                            }
                            break;
                    }
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {
                    switch (tab.getPosition()) {
                        case 0:
                            tab.setIcon(R.drawable.icon_photo_search_stroke);
                            break;
                        case 1:
                            tab.setIcon(R.drawable.icon_person_stroke);
                            break;
                        case 2:
                            tab.setIcon(R.drawable.icon_group_search_stroke);
                            break;
                    }
                }

                @Override
                public void onTabReselected(TabLayout.Tab tab) {}
            };

    /** Listener for Tabs between TOP, RECENT, MAP */

    private final TabLayout.OnTabSelectedListener galleryShowListener =
            new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    switch (tab.getPosition()) {
                        case 0:
                            tab.setIcon(R.drawable.icon_top_filled);
                            mToolbar.setTitle(R.string.search_top);
                              if (!mBarSearchView.isIconified()) {
                                  mDatePickerButton.setVisible(false);
                              } else if (mSetCategoryFlag) {
                                  mDatePickerButton.setVisible(false);
                                  mCloseButton.setVisible(true);
                                  mToolbar.setTitle(mToolBarTitle);
                              } else if (mCalendar != null) {
                                  mToolbar.setTitle(mToolBarTitle);
                                  mDatePickerButton.setVisible(true);
                              } else {
                                  mDatePickerButton.setVisible(true);
                              }
                            break;
                        case 1:

                            tab.setIcon(R.drawable.icon_search_recent_filled);
                            if (mSetCategoryFlag) {
                                mToolbar.setTitle(mToolBarTitle);
                                mCloseButton.setVisible(true);
                            } else {
                                mToolbar.setTitle(R.string.search_recent);
                            }
                            if (mDatePickerButton != null && mBarSearchView != null) {
                                mDatePickerButton.setVisible(false);
                                mBarSearchView.setVisibility(View.VISIBLE);
                            }
                            break;

                        case 2:
                            MapFragment fragment = (MapFragment)
                                    Objects.requireNonNull(mViewPager.getAdapter())
                                            .instantiateItem(mViewPager,2);
                            fragment.setSlidingUpPanelState(true);
                            mAppBarLayout.setExpanded(false,true);
                            setAppBarDragBehavior(false);
                            tab.setIcon(R.drawable.icon_map_search_filled);
                            break;
                    }
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {
                    switch (tab.getPosition()) {
                        case 0:
                            tab.setIcon(R.drawable.icon_top_stroke);
                            break;
                        case 1:
                            tab.setIcon(R.drawable.icon_search_recent_stroke);
                            break;
                        case 2:
                            tab.setIcon(R.drawable.icon_search_map_stroke);
                            mAppBarLayout.setExpanded(true,true);
                            MapFragment fragment = (MapFragment)
                                    Objects.requireNonNull(mViewPager.getAdapter())
                                            .instantiateItem(mViewPager,2);
                            fragment.setSlidingUpPanelState(false);
                            setAppBarDragBehavior(true);
                            break;
                    }
                }

                @Override
                public void onTabReselected(TabLayout.Tab tab) {
                }
            };

    /**
     * Receives data from calender when user choose date and click "OK".
     * Setups GalleryShowAdapter with received date.
     */

    @Override
    public void getCalendar(Calendar calendar, boolean reset) {
        SimpleDateFormat queryFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        SimpleDateFormat titleFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.US);

        if (reset) {
            mCalendar = null;
            mToolBarTitle = null;
        } else {
            mCalendar = calendar;
            String formattedDate = titleFormat.format(calendar.getTime());
            mToolBarTitle = getString(R.string.search_date_title_result) + formattedDate;
            mToolbar.setTitle(mToolBarTitle);
        }
        String formattedDate = queryFormat.format(calendar.getTime());
        mViewPagerBeforeSearchPosition = 0;
        setUpGalleryShowAdapter(null, formattedDate);
    }

    private void setAppBarDragBehavior (boolean isCanDrag) {
        CoordinatorLayout.LayoutParams params =
                (CoordinatorLayout.LayoutParams) mAppBarLayout.getLayoutParams();
        AppBarLayout.Behavior behavior = (AppBarLayout.Behavior) params.getBehavior();
        if (behavior != null) {
            behavior.setDragCallback(new AppBarLayout.Behavior.DragCallback() {
                @Override
                public boolean canDrag(@NonNull AppBarLayout appBarLayout) {
                    return isCanDrag;
                }
            });
        }
    }

    /**
     * Receives category filter from CategoryItemAdapter when user choose photo category.
     * Setups GalleryShowAdapter with received category name.
     */

    @Override
    public void callCategoryFilter(EnumCategory category) {
        mViewPagerBeforeSearchPosition = mViewPager.getCurrentItem();
        mSetCategoryFlag = true;
        mCloseButton.setVisible(true);
        mDatePickerButton.setVisible(false);
        mCalendar = null;
        mToolBarTitle = category.name();
        setUpGalleryShowAdapter(category.name(), null);
        mToolbar.setTitle(mToolBarTitle);
    }


    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putBoolean(CATEGORY_FLAG, mSetCategoryFlag);
        outState.putString(TITLE, mToolBarTitle);
        outState.putString(SEARCH_TRIGGER, mQuery);
        outState.putInt(ACTIVITY_MODE,mActivityMode);
        super.onSaveInstanceState(outState);
    }

    /**
     * Receives signal from fragment when fragment is fetching more elements to list progressBar
     * animation is visible and invisible when fetching is stopped.
     */

    @Override
    public void setProgressBar(boolean on) {
        if (on) {
            mProgressBar.setVisibility(View.VISIBLE);
        } else {
            mProgressBar.setVisibility(View.GONE);
        }
    }
}