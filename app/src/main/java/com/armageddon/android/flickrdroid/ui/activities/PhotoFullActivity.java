package com.armageddon.android.flickrdroid.ui.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.armageddon.android.flickrdroid.R;
import com.armageddon.android.flickrdroid.api.FlickrFetchr;
import com.armageddon.android.flickrdroid.common.ActivityUtils;
import com.armageddon.android.flickrdroid.common.GalleryItemBase;
import com.armageddon.android.flickrdroid.common.QueryPreferences;
import com.armageddon.android.flickrdroid.common.QueryTypes;
import com.armageddon.android.flickrdroid.model.GalleryItem;
import com.armageddon.android.flickrdroid.api.RequestResponse;
import com.armageddon.android.flickrdroid.ui.fragments.PhotoFullFragment;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;


/**
 * Controls full-size photo viewing:
 * - downloads more elements to Gallery list
 * - sliding animation between photo elements via ViewPager
 * - slideShow handler
 */

public class PhotoFullActivity extends AppCompatActivity implements PhotoFullFragment.CallBacks {
    public static final int SLIDESHOW_INTERVAL_3_SEC = 3000;
    public static final int SLIDESHOW_INTERVAL_4_SEC = 4000;
    public static final int SLIDESHOW_INTERVAL_5_SEC = 5000;
    public static final int SLIDESHOW_INTERVAL_10_SEC = 10000;
    public static final int SLIDESHOW_INTERVAL_15_SEC = 15000;
    private static final int OFFSCREEN_PAGES_LIMIT = 3;
    private static final String SLIDE_SHOW_FLAG = "slide_show";
    private static final int ITEMS_LOAD_BORDER = 20;
    private static final String PHOTO_INFO_SHOW = "info_show";
    private static final String GALLERY_ITEM_POSITION = "position";
    private static final String PHOTO_ID = "photo_id";
    private static final String GALLERY_ITEMS = "photo_recent";

    private ViewPagerFixed mViewPager;
    private boolean isPhotoInfoVisible = true;
    private RequestResponse<GalleryItem> mResponse;
    private List<GalleryItem> mItems;
    private String mQueryType;
    private String mQuery;
    private String mCategory;
    private boolean isFetchingOn;
    private boolean isSlideShowRun;
    private final Handler mHandler = new Handler();


    private FetchItemsTask mFetchItemsTask = new FetchItemsTask();

    public static Intent newIntent (Context context, UUID itemId, ArrayList<GalleryItem> items) {
        Intent intent = new Intent(context, PhotoFullActivity.class);
        intent.putExtra(PHOTO_ID, itemId);
        intent.putExtra(GALLERY_ITEMS,items);
        return intent;
    }


    public static UUID getGalleryItemPosition(Intent data) {
        return (UUID) data.getSerializableExtra(GALLERY_ITEM_POSITION);
    }

    /**
     * Sends the current position in the gallery list to the previous activity.
     */

    @Override
    public void onBackPressed() {
        mFetchItemsTask.cancel(true);
        mResponse.setItems(mItems);
        GalleryItemBase.setResponse(this, mResponse);
        Intent data = new Intent();
        UUID mCurrentItemId = mItems.get(mViewPager.getCurrentItem()).getItemId();
        data.putExtra(GALLERY_ITEM_POSITION, mCurrentItemId);
        setResult(RESULT_OK, data);
        super.onBackPressed();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(PHOTO_INFO_SHOW, isPhotoInfoVisible);
        outState.putBoolean(SLIDE_SHOW_FLAG, isSlideShowRun);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mHandler.removeCallbacksAndMessages(null);
    }

    /**
     * Starts slide show.
     */
    private void startSlideShow () {
        int interval = QueryPreferences.getSlideShowInterval(this);

        Toast.makeText(this,
                getString(R.string.slide_show_started,
                        interval/1000),
                Toast.LENGTH_SHORT)
                .show();
        mHandler.postDelayed(new SlideShowJob(), interval);

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isSlideShowRun) {
           startSlideShow();
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        ActivityUtils.onActivityCreateSetTheme(this,ActivityUtils.THEME_DARK);
        ActivityUtils.hideStatusBar(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_full);

//        if (mResponse == null) {
           mResponse = GalleryItemBase.getResponse(this);
           mItems = mResponse.getItems();
           mQueryType = mResponse.getQueryType();
           mQuery = mResponse.getQuery();
           mCategory = mResponse.getCategory();
//        }

        if (savedInstanceState != null) {
            isPhotoInfoVisible = savedInstanceState.getBoolean(PHOTO_INFO_SHOW);
            isSlideShowRun = savedInstanceState.getBoolean(SLIDE_SHOW_FLAG);
        }

        mViewPager = findViewById(R.id.view_pager);
        mViewPager.setOffscreenPageLimit(OFFSCREEN_PAGES_LIMIT);  //instant pages load limit for fast browsing
        mViewPager.setAdapter(new FragmentStatePagerAdapter(getSupportFragmentManager(),
                FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
            @NonNull
            @Override
            public Fragment getItem(int position) {
                return PhotoFullFragment.newInstance(mItems.get(position), isPhotoInfoVisible);
            }

            @Override
            public int getCount() {
                return mItems.size();
            }
        });


        mViewPager.setPageTransformer(false, new DepthPageTransformer());
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (!isFetchingOn && mResponse.getPage() < mResponse.getPages()
                        && mItems.size() - position < ITEMS_LOAD_BORDER
                        && mQueryType != null) {
                    isFetchingOn = true;
                    mFetchItemsTask = new FetchItemsTask();
                    mFetchItemsTask.execute();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        // Get photo ID from previous activity and start viewing from picture with the same ID

        UUID currentPhotoId = (UUID) getIntent().getSerializableExtra(PHOTO_ID);
        for (int i = 0; i < mItems.size(); i++) {
            if (mItems.get(i).getItemId().equals(currentPhotoId)) {
                mViewPager.setCurrentItem(i, false);
                break;
            }
        }

    }

    /**
     * Sends response to Flickr.com and gets data.
     */
    private class FetchItemsTask extends AsyncTask<String, Void, RequestResponse<GalleryItem>> {

        @Override
        protected RequestResponse<GalleryItem> doInBackground(String... strings) {

            String page = String.valueOf(mResponse.getPage() + 1);
            switch (mQueryType) {
                case QueryTypes.INTERESTINGNESS:
                    if (mCategory == null) {
                        return new FlickrFetchr().fetchInterestingPhotos(
                                PhotoFullActivity.this,mQuery, page, null);
                    } else {
                        return new FlickrFetchr().fetchInterestingPhotosByTag(
                                PhotoFullActivity.this,mCategory, page);
                    }
                case QueryTypes.RECENT:
                    if (mCategory == null) {
                        return new FlickrFetchr().fetchRecentPhotos(
                                PhotoFullActivity.this,page);
                    } else {
                        return new FlickrFetchr().fetchRecentPhotosByTag(
                                PhotoFullActivity.this,mCategory, page);
                    }
                case QueryTypes.PHOTO:
                    return new FlickrFetchr()
                            .fetchSearchPhotos(
                                    PhotoFullActivity.this,
                                    mQuery, page, null, null, false);
                case QueryTypes.PUBLIC_PHOTO:
                    return new FlickrFetchr().fetchPublicPhotos(
                            PhotoFullActivity.this,mQuery, page);
                case QueryTypes.USER_FAVES:
                    return new FlickrFetchr().fetchUserFaves(
                            PhotoFullActivity.this, mQuery, page);
                case QueryTypes.USER_GALLERY:
                    return new FlickrFetchr().fetchUserGallery(
                            PhotoFullActivity.this, mQuery, page);
                case QueryTypes.USER_GROUP:
                    return new FlickrFetchr().fetchUserGroupPhotos(
                            PhotoFullActivity.this, mQuery, page);
                case QueryTypes.USER_ALBUM:
                    String [] idArray = mQuery.split(" ");
                    return new FlickrFetchr().fetchUserAlbum(
                            PhotoFullActivity.this,idArray[0], idArray[1], page);
                case QueryTypes.CAMERA_ROLL:
                    return new FlickrFetchr().fetchUserCameraRollPhotos(
                            PhotoFullActivity.this, page);
                case QueryTypes.CONTACTS_PHOTO:
                    return new FlickrFetchr().fetchUserContactsPhotos(
                            PhotoFullActivity.this, page);
            }
            return null;
        }

        @Override
        protected void onPostExecute(RequestResponse<GalleryItem> response) {
            mResponse = response;
            if (mResponse.getConnectionStat() == RequestResponse.CONNECTION_OK
                    && mResponse.getResponseDataStat().equals(RequestResponse.RESPONSE_DATA_OK)) {
                mItems.addAll(response.getItems());
                Objects.requireNonNull(mViewPager.getAdapter()).notifyDataSetChanged();
                isFetchingOn = false;
            }

            }
        }

    /**
     * Controls the visibility of buttons. If the user hides the buttons in current picture,
     * then they should also be hidden in the previous photos and in the photos in front
     * of current picture.
     * Based on parameter OFF_SCREEN_PAGES_LIMIT.
     */


    @Override
    public void setInfoVisibilityState(boolean isVisible) {

        if (isSlideShowRun) {
            onSlideShowStopped();
        }

        isPhotoInfoVisible = isVisible;
        mViewPager.getAdapter().notifyDataSetChanged();
        for (int i = 1; i < OFFSCREEN_PAGES_LIMIT - 1; i++) {  // current limit is 3
            int positionForward = mViewPager.getCurrentItem() + i;
            int positionBack = mViewPager.getCurrentItem() - i;
            if (positionForward < mViewPager.getAdapter().getCount()) {
                PhotoFullFragment fragment = (PhotoFullFragment) mViewPager
                        .getAdapter()
                        .instantiateItem(mViewPager, positionForward);
                fragment.setPhotoInfoVisible(isVisible);
            }

            if (positionBack >= 0) {
                PhotoFullFragment fragment = (PhotoFullFragment) mViewPager
                        .getAdapter()
                        .instantiateItem(mViewPager, positionBack);
                fragment.setPhotoInfoVisible(isVisible);

            }
        }

    }

    @Override
    public boolean getInfoVisibilityState() {
        return isPhotoInfoVisible;
    }

    @Override
    public void onSlideShowStarted() {
        if (!isSlideShowRun) {
            isSlideShowRun = true;
            startSlideShow();
        }
    }

    private void onSlideShowStopped() {
        isSlideShowRun = false;
        mHandler.removeCallbacksAndMessages(null);
        mViewPager.setKeepScreenOn(false);
        Toast.makeText(PhotoFullActivity.this,
                getString(R.string.slide_show_stopped),
                Toast.LENGTH_SHORT)
                .show();
    }

    class SlideShowJob implements Runnable {
        Animation animFadeIn;
        Animation animFadeOut;

        SlideShowJob () {
            mViewPager.setKeepScreenOn(true);
            animFadeIn = AnimationUtils.loadAnimation(PhotoFullActivity.this,
                    android.R.anim.fade_in);
            animFadeIn.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    mHandler.postDelayed(
                            SlideShowJob.this,
                            QueryPreferences.getSlideShowInterval(PhotoFullActivity.this));
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            animFadeOut = AnimationUtils.loadAnimation(PhotoFullActivity.this,
                    android.R.anim.fade_out);
            animFadeOut.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    mViewPager.setCurrentItem(
                            mViewPager.getCurrentItem() + 1, false);
                    mViewPager.startAnimation(animFadeIn);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
        }

        @Override
        public void run() {
            int currentIndex = mViewPager.getCurrentItem();
            if (currentIndex < mItems.size() - 1) {
                mViewPager.startAnimation(animFadeOut);
            } else {
                onSlideShowStopped();
            }
        }
    }

    /**
     * Controls animation between the slides.
     */

    private static class DepthPageTransformer implements ViewPager.PageTransformer {
        private static final float MIN_SCALE = 0.75f;

        public void transformPage(@NotNull View view, float position) {
            SlidingUpPanelLayout layout = (SlidingUpPanelLayout) view;
            FrameLayout frameLayout1 = (FrameLayout) layout.getChildAt(0);
            View userIconAndNameView = frameLayout1.getChildAt(1);
            userIconAndNameView.setTranslationX(frameLayout1.getWidth() * -position);

            FrameLayout frameLayout2 = (FrameLayout) layout.getChildAt(1);
            View panelPullButton = frameLayout2.getChildAt(0);
            panelPullButton.setTranslationX(frameLayout1.getWidth() * -position);

            View photoControlButtons = frameLayout2.getChildAt(1);
            photoControlButtons.setTranslationX(frameLayout1.getWidth() * -position);

            View photoView =  frameLayout1.getChildAt(0);

            int pageWidth = photoView.getWidth();

            if (position < -1) { // [-Infinity,-1)
                // This page is way off-screen to the left.
                photoView.setAlpha(0f);

            } else if (position <= 0) { // [-1,0]
                // Use the default slide transition when moving to the left page
                photoView.setAlpha(1f);
                photoView.setTranslationX(0f);
                photoView.setScaleX(1f);
                photoView.setScaleY(1f);

            } else if (position <= 1) { // (0,1]
                // Fade the page out.
                photoView.setAlpha(1 - position);

                // Counteract the default slide transition
                photoView.setTranslationX(pageWidth * -position);

                // Scale the page down (between MIN_SCALE and 1)
                float scaleFactor = MIN_SCALE
                        + (1 - MIN_SCALE) * (1 - Math.abs(position));
                photoView.setScaleX(scaleFactor);
                photoView.setScaleY(scaleFactor);

            } else { // (1,+Infinity]
                // This page is way off-screen to the right.
                photoView.setAlpha(0f);
            }
        }
    }

    /**
     * There was a problem with ViewPager and framework: com.github.chrisbanes:PhotoView.
     * When the user stretched the picture to the maximum, sometimes it could lead to crashes.
     * The solution was to create a class inherited from ViewPager and override two methods.
     * This methods needs to handle exceptions.
     */

    private static class ViewPagerFixed extends ViewPager {
        public ViewPagerFixed(@NonNull Context context) {
            super(context);
        }
        public ViewPagerFixed(@NonNull Context context, @Nullable AttributeSet attrs) {
            super(context, attrs);
        }

        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouchEvent(MotionEvent ev) {
            try {
                return super.onTouchEvent(ev);
            } catch (IllegalArgumentException ex) {
                ex.printStackTrace();
            }
            return false;
        }

        @Override
        public boolean onInterceptTouchEvent(MotionEvent ev) {
            try {
                return super.onInterceptTouchEvent(ev);
            } catch (IllegalArgumentException ex) {
                ex.printStackTrace();
            }
            return false;
        }
    }
}
