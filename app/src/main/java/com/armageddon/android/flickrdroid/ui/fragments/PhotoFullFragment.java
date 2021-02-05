package com.armageddon.android.flickrdroid.ui.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.armageddon.android.flickrdroid.R;
import com.armageddon.android.flickrdroid.api.FlickrFetchr;
import com.armageddon.android.flickrdroid.common.Converter;
import com.armageddon.android.flickrdroid.common.LogoIcon;
import com.armageddon.android.flickrdroid.common.QueryPreferences;
import com.armageddon.android.flickrdroid.model.GalleryItem;
import com.armageddon.android.flickrdroid.model.PhotoExif;
import com.armageddon.android.flickrdroid.api.RequestResponse;
import com.armageddon.android.flickrdroid.ui.activities.CommentsActivity;
import com.armageddon.android.flickrdroid.ui.activities.SearchActivity;
import com.armageddon.android.flickrdroid.ui.activities.UserPersonalPageActivity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.flexbox.FlexboxLayout;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.sothree.slidinguppanel.ScrollableViewHelper;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;

import static android.content.Context.DOWNLOAD_SERVICE;

/**
 * Shows each photo item from list in full-size mode
 * Features:
 *  - user's logo and name
 *  - photo name
 *  - favorites button
 *  - comments button
 *  - download button
 *  - share button
 *  - slideshow button
 *  - sliding panel from bottom to top with additional photo info (description, location, exif, tags)
 *  - Zooming is possible.
 */

public class PhotoFullFragment extends Fragment implements Converter {
    private static final int REQUEST_STORAGE_PERMISSION = 11;
    private static final int REQUEST_DOWNLOAD_PERMISSION = 22;
    private static final float MAP_ZOOM = 5f;
    private static final float TAP_SCALE = 3.5f;
    private static final float MAX_SCALE = 6f;
    private static final String GALLERY_ITEM = "item";
    private static final String PHOTO_INFO_FLAG = "photo_info";
    private static final String FILE_NAME_TEMPLATE = "Flick_image";
    private static final String FILE_TYPE_TEMPLATE = ".jpg";

    public interface CallBacks {
        void setInfoVisibilityState (boolean isVisible);
        boolean getInfoVisibilityState ();
        void onBackPressed();
        void onSlideShowStarted();
    }
    private InitDetailItemsTask mInitDetailItemsTask;
    private boolean isSlidingPanelInitialised;
    private LinearLayout mUpPanel;
    private LinearLayout mDownPanel;
    private GalleryItem mItem;
    private PhotoView mPhotoView;
    private ImageButton mCommentsButton;
    private ImageView mCurtain;
    private ProgressBar mProgressBar;
    private ImageView mUserIcon;
    private TextView mUserName;
    private CallBacks mCallBacks;
    private TextView mPhotoName;
    private TextView mFavsQuantity;
    private TextView mCommentsQuantity;
    private View mBottomShadow;
    private View mNameDivider;
    private ProgressBar mSlidePanelProgress;
    private Handler mHandler;
    private RelativeLayout mPersonInfoPanel;
    private SlidingUpPanelLayout mSlidePanel;
    private NestedScrollView mNestedScrollView;
    private boolean mPhotoInfoVisible;
    private float scale;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mCallBacks = (CallBacks) context;
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        mSlidePanel.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
    }

    @Override
    public void onStop() {
        if (mInitDetailItemsTask != null) {
            mInitDetailItemsTask.onMapStop();
        }
        super.onStop();

    }

    @Override
    public void onDestroy() {
        if (mInitDetailItemsTask != null) {
            mInitDetailItemsTask.onMapDestroy();
        }
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallBacks = null;
    }

    @Override
    public void onLowMemory() {
        if (mInitDetailItemsTask != null) {
            mInitDetailItemsTask.onMapLowMemory();
        }
        super.onLowMemory();
    }

    @Override
    public void onResume() {
        if (mInitDetailItemsTask != null) {
            mInitDetailItemsTask.onMapResume();
        }

        super.onResume();
    }

    @Override
    public void onStart() {
        if (mInitDetailItemsTask != null) {
            mInitDetailItemsTask.onMapStart();
        }
        mCommentsQuantity.setText(mItem.getCount_comments());
        mFavsQuantity.setText(mItem.getCount_faves());
        super.onStart();
    }

    @Override
    public void onPause() {
        if (mInitDetailItemsTask != null) {
            mInitDetailItemsTask.onMapPause();
        }
        super.onPause();
        mSlidePanel.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
    }

    public static PhotoFullFragment newInstance(GalleryItem item, boolean photoInfoVisible) {
        Bundle args = new Bundle();
        args.putSerializable(GALLERY_ITEM, item);
        args.putBoolean(PHOTO_INFO_FLAG, photoInfoVisible);
        PhotoFullFragment fragment = new PhotoFullFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        assert getArguments() != null;
        mItem = (GalleryItem) getArguments().getSerializable(GALLERY_ITEM);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
      View view = inflater.inflate(R.layout.fragment_photo_full, container, false);
      mCurtain = view.findViewById(R.id.curtain);
      mPhotoView = view.findViewById(R.id.photo_detail_view);
      mProgressBar = view.findViewById(R.id.progress_bar);
      mUserIcon = view.findViewById(R.id.user_icon);
      mUserName = view.findViewById(R.id.user_name);
      mPhotoName = view.findViewById(R.id.photo_name);
      mFavsQuantity = view.findViewById(R.id.fav_quantity_text);
      mCommentsQuantity = view.findViewById(R.id.comments_quantity_text);
      mPersonInfoPanel = view.findViewById(R.id.relativeLayout);
      mNestedScrollView = view.findViewById(R.id.scrollView);
      mDownPanel = view.findViewById(R.id.photo_control_panel);
      mUpPanel = view.findViewById(R.id.photo_up_panel);
      mBottomShadow = view.findViewById(R.id.bottom_shadow_view);
      mSlidePanel = view.findViewById(R.id.sliding_layout);
      mNameDivider = view.findViewById(R.id.divider);
      mCommentsButton = view.findViewById(R.id.comments_button);
      ImageView closeButton = view.findViewById(R.id.icon_close);
      ImageButton shareButton = view.findViewById(R.id.share_button);
      ImageButton downLoadButton = view.findViewById(R.id.download_button);
      ImageButton slideShowButton = view.findViewById(R.id.slideshow_button);

      mFavsQuantity.setOnClickListener(v -> {
          if (isAdded()) {
              String appUserOwner = QueryPreferences.getUserId(requireActivity());
              if (appUserOwner != null && !appUserOwner.equals(mItem.getOwner())) {
                  mSlidePanelProgress.setVisibility(View.VISIBLE);
                  if (mItem.isfavorite()) {
                      new RemoveFavoriteTask(mItem.getId()).execute();
                  } else {
                      new AddFavoriteTask(mItem.getId()).execute();
                  }
              } else if (appUserOwner != null) {
                  Toast.makeText(getActivity(),
                          getString(R.string.own_photo_fave_warning),
                          Toast.LENGTH_SHORT)
                          .show();
              } else {
                  Toast.makeText(getActivity(),
                          getString(R.string.not_singed_up_warning),
                          Toast.LENGTH_SHORT)
                          .show();
              }
          }
      });

      mCommentsQuantity.setOnClickListener(v -> {
          RippleDrawable rippleDrawable = (RippleDrawable) mCommentsButton.getBackground();

          rippleDrawable.setState(new int[]{
                  android.R.attr.state_pressed,
                  android.R.attr.state_enabled});
          Handler handler = new Handler();
          handler.postDelayed(() -> rippleDrawable.setState(new int[]{}), 200);

      });
      mCommentsButton.setOnClickListener(v -> {
          Intent commentsIntent = CommentsActivity.newIntent(getActivity(), mItem.getId());
          startActivity(commentsIntent);
      });

      mSlidePanelProgress = view.findViewById(R.id.slide_panel_progress);

        Intent userPersonalPageIntent = UserPersonalPageActivity.newIntent(
                getActivity(),
                mItem.getOwner(),
                mItem.getOwnername(),
                mItem.getOwnerIcon(LogoIcon.huge_300px));

      mUserName.setOnClickListener(v -> startActivity(userPersonalPageIntent));
      mUserIcon.setOnClickListener(v -> startActivity(userPersonalPageIntent));

      closeButton.setOnClickListener(v -> mCallBacks.onBackPressed());

      downLoadButton.setOnClickListener(v -> {
          if (hasStoragePermissions()) {
              downloadImage();
          } else {
              shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE);
              requestPermissions(new String[] {android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                              android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                      REQUEST_DOWNLOAD_PERMISSION);
          }
        });

      slideShowButton.setOnClickListener(v -> {
          Animation animationUpPanel;
          Animation animationDownPanel;
          if (mPhotoInfoVisible) {
              animationUpPanel = AnimationUtils.loadAnimation(getActivity(),
                      R.anim.photo_up_panel_hide);
              animationDownPanel = AnimationUtils.loadAnimation(getActivity(),
                      R.anim.photo_down_panel_hide);
              mSlidePanel.setTouchEnabled(false);
              mCurtain.setVisibility(View.GONE);
              mPhotoInfoVisible = false;

          } else {
              animationUpPanel = AnimationUtils.loadAnimation(getActivity(),
                      R.anim.photo_up_panel_show);
              animationDownPanel = AnimationUtils.loadAnimation(getActivity(),
                      R.anim.photo_down_panle_show);
              mSlidePanel.setTouchEnabled(true);
              mCurtain.setVisibility(View.VISIBLE);
              mPhotoInfoVisible = true;
          }
          mUpPanel.startAnimation(animationUpPanel);
          mDownPanel.startAnimation(animationDownPanel);
          mBottomShadow.startAnimation(animationDownPanel);
          mCallBacks.setInfoVisibilityState(mPhotoInfoVisible);
          mCallBacks.onSlideShowStarted();
      });

      shareButton.setOnClickListener(v -> {
          if (hasStoragePermissions()) {
              shareImage();
          } else {
              shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE);
              requestPermissions(new String[] {android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                      android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                      REQUEST_STORAGE_PERMISSION);
          }
      });

      mSlidePanel.setScrollableViewHelper(new NestedScrollableViewHelper(mNestedScrollView));
      mSlidePanel.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
          @Override
          public void onPanelSlide(View panel, float slideOffset) {
              mPhotoName.setAlpha(1.0f - slideOffset);
              mNameDivider.setAlpha(1.0f - slideOffset);
          }

          @Override
          public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState,
                                          SlidingUpPanelLayout.PanelState newState) {

              if (newState == SlidingUpPanelLayout.PanelState.EXPANDED
                      && mNestedScrollView.getChildCount() == 0) {
                  mSlidePanelProgress.setVisibility(View.VISIBLE);
              }

              if (newState == SlidingUpPanelLayout.PanelState.DRAGGING && !isSlidingPanelInitialised) {
                  isSlidingPanelInitialised = true;
                  mHandler = new Handler();
                  mInitDetailItemsTask = new InitDetailItemsTask();
                  mInitDetailItemsTask.execute();
              }
          }

      });
      mPhotoView.setMaximumScale(MAX_SCALE);
      mPhotoView.setOnDoubleTapListener(new GestureDetector.OnDoubleTapListener() {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                Animation animationUpPanel;
                Animation animationDownPanel;
                if (mPhotoInfoVisible) {
                     animationUpPanel = AnimationUtils.loadAnimation(getActivity(),
                            R.anim.photo_up_panel_hide);
                     animationDownPanel = AnimationUtils.loadAnimation(getActivity(),
                            R.anim.photo_down_panel_hide);
                    mSlidePanel.setTouchEnabled(false);
                    mCurtain.setVisibility(View.GONE);
                    mPhotoInfoVisible = false;

                } else {
                     animationUpPanel = AnimationUtils.loadAnimation(getActivity(),
                            R.anim.photo_up_panel_show);
                     animationDownPanel = AnimationUtils.loadAnimation(getActivity(),
                            R.anim.photo_down_panle_show);
                    mSlidePanel.setTouchEnabled(true);
                    mCurtain.setVisibility(View.VISIBLE);
                    mPhotoInfoVisible = true;
                }
                mUpPanel.startAnimation(animationUpPanel);
                mDownPanel.startAnimation(animationDownPanel);
                mBottomShadow.startAnimation(animationDownPanel);
                mCallBacks.setInfoVisibilityState(mPhotoInfoVisible);
                return false;
            }
            boolean isMaximum = false;
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                if (!isMaximum) {
                    mPhotoView.setScale(TAP_SCALE, true);
                    isMaximum = true;
                } else {
                    mPhotoView.setScale(mPhotoView.getMinimumScale(), true);
                    isMaximum = false;
                }
                return false;
            }

            @Override
            public boolean onDoubleTapEvent(MotionEvent e) {
                return false;
            }
        });

        setPhotoInfoVisible(mCallBacks.getInfoVisibilityState());
      return view;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        scale = getResources().getDisplayMetrics().density;

        String photoUrl = mItem.getPhotoUrl(QueryPreferences.getPhotoViewQuality(getActivity()));
        if (photoUrl == null) {
            photoUrl = mItem.getPhotoHighestResUrl();
        }

        if (mItem.isfavorite()) {
            mFavsQuantity.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.icon_star_filled, 0, 0, 0);
        }

        // loads photo to PhotoView
        Glide.with(this)
                .load(photoUrl)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model,
                                                Target<Drawable> target, boolean isFirstResource) {
                        mProgressBar.setVisibility(View.GONE);
                        mPhotoView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                        return false;
                    }

                    // show progress bar (circle) while photo is loading
                    @Override
                    public boolean onResourceReady(Drawable resource, Object model,
                                                   Target<Drawable> target, DataSource dataSource,
                                                   boolean isFirstResource) {
                        mProgressBar.setVisibility(View.GONE);
                        return false;
                    }
                })
                .error(R.drawable.ic_outline_broken_image_24)
                .into(mPhotoView);
        mPhotoName.setText(mItem.getName());

        Glide.with(this)
                .load(mItem.getOwnerIcon(LogoIcon.normal_100px))
                .error(R.drawable.logo)
                .into(mUserIcon);
        mUserName.setText(mItem.getOwnername());
    }

    /** single tap hides UI */
    public void setPhotoInfoVisible(boolean visible) {
        mPhotoInfoVisible = visible;
        if (mPersonInfoPanel != null && mDownPanel != null) {
            if (visible) {
                mUpPanel.setVisibility(View.VISIBLE);
                mDownPanel.setVisibility(View.VISIBLE);
                mBottomShadow.setVisibility(View.VISIBLE);
                mSlidePanel.setTouchEnabled(true);
                mCurtain.setVisibility(View.VISIBLE);
            } else {
                mUpPanel.setVisibility(View.INVISIBLE);
                mDownPanel.setVisibility(View.INVISIBLE);
                mBottomShadow.setVisibility(View.INVISIBLE);
                mCurtain.setVisibility(View.GONE);
                mSlidePanel.setTouchEnabled(false);
            }
        }
    }


    private static class NestedScrollableViewHelper extends ScrollableViewHelper {
        NestedScrollView mNestedScrollView;

        public NestedScrollableViewHelper(NestedScrollView nestedScrollView) {
            mNestedScrollView = nestedScrollView;
        }

        public int getScrollableViewScrollPosition(View scrollableView, boolean isSlidingUp) {
            if (mNestedScrollView != null) {
                if(isSlidingUp){
                    return mNestedScrollView.getScrollY();
                } else {
                    View child = mNestedScrollView.getChildAt(0);
                    return (child.getBottom() -
                            (mNestedScrollView.getHeight() + mNestedScrollView.getScrollY()));
                }
            } else {
                return 0;
            }
        }
    }

    /**
     * gets on user's demand additional exif information about photo
     * shows on click dropdown-animation
     */

    @SuppressLint("StaticFieldLeak")
    private class FetchPhotoExifTask extends AsyncTask<Void, Void, RequestResponse<PhotoExif>> {
        ImageButton mExifArrow;
        TextView mPhotoLens;
        TextView mLensTitle;
        TextView mPhotoAperture;
        TextView mPhotoExposure;
        TextView mPhotoISO;
        RelativeLayout mCameraLayout;
        RelativeLayout mCameraExifLayout;
        TextView mPhotoFocalLength;

        public FetchPhotoExifTask(ImageButton exifArrow, TextView photoLens, TextView lensTitle,
                                  TextView photoAperture, TextView photoExposure, TextView photoISO,
                                  RelativeLayout cameraLayout, RelativeLayout cameraExifLayout,
                                  TextView photoFocalLength) {
            mExifArrow = exifArrow;
            mPhotoLens = photoLens;
            mLensTitle = lensTitle;
            mPhotoAperture = photoAperture;
            mPhotoExposure = photoExposure;
            mPhotoISO = photoISO;
            mCameraLayout = cameraLayout;
            mCameraExifLayout = cameraExifLayout;
            mPhotoFocalLength = photoFocalLength;
        }

        @Override
        protected RequestResponse<PhotoExif> doInBackground(Void... voids) {
            return new FlickrFetchr().getPhotoExif(mItem.getId());
        }

        @Override
        protected void onPostExecute(RequestResponse<PhotoExif> response) {
            super.onPostExecute(response);

            if (response.getConnectionStat() == RequestResponse.CONNECTION_OK
                    && response.getResponseDataStat().equals(RequestResponse.RESPONSE_DATA_OK)) {
                PhotoExif mPhotoExif = response.getItems().get(0);
                mExifArrow.setVisibility(View.VISIBLE);
                if (mPhotoExif.getLens() == null) {
                    mPhotoLens.setVisibility(View.GONE);
                    mLensTitle.setVisibility(View.GONE);
                } else {
                    mPhotoLens.setText(mPhotoExif.getLens());
                }
                mPhotoAperture.setText(mPhotoExif.getAperture());
                mPhotoExposure.setText(mPhotoExif.getExposure());
                mPhotoISO.setText(mPhotoExif.getISO());
                mPhotoFocalLength.setText(mPhotoExif.getFocalLength());
                mCameraLayout.setOnClickListener(v -> {
                    if (mCameraExifLayout.getVisibility() == View.GONE) {
                        mCameraExifLayout.setVisibility(View.VISIBLE);
                        Animation elementsAnim = AnimationUtils
                                .loadAnimation(getActivity(), R.anim.item_animation_fall_down);
                        mCameraExifLayout.startAnimation(elementsAnim);

                        Animation arrowAnim = AnimationUtils
                                .loadAnimation(getActivity(), R.anim.rotate_clockwise);
                        arrowAnim.setFillAfter(true);
                        mExifArrow.startAnimation(arrowAnim);
                    } else {
                        Animation elementsAnim = AnimationUtils
                                .loadAnimation(getActivity(), R.anim.item_animation_rise_up);

                        mCameraExifLayout.startAnimation(elementsAnim);
                        elementsAnim.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                mCameraExifLayout.setVisibility(View.GONE);
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {

                            }
                        });
                        Animation arrowAnim = AnimationUtils
                                .loadAnimation(getActivity(), R.anim.rotate_counter_clockwise);
                        arrowAnim.setFillAfter(true);
                        mExifArrow.startAnimation(arrowAnim);
                    }
                });
            }
        }
    }

    /**
     * if sliding up panel goes up shows additional info:
     *  - photo viewers count
     *  - photo description
     *  - photo created date
     *  - photo size
     *  - camera model
     *    - click starts FetchPhotoExifTask
     *  - photo location
     *    - click opens Google map in small window with photo marker on it
     *  - tags
     *  - privacy status
     */

    @SuppressLint("StaticFieldLeak")
    class InitDetailItemsTask extends AsyncTask<Void, Void, Void> {
        private ImageButton mLocationArrow;
        private MapView mMapView;
        private GoogleMap mMap;


        public void onMapStop () {
            if (mMapView != null) {
                mMapView.onStop();
            }
        }

        public void onMapDestroy () {
            if (mMapView != null) {
                mMapView.onDestroy();
            }
        }

        public void onMapPause () {
            if (mMapView != null) {
                mMapView.onPause();
            }
        }

        public void onMapResume () {
            if (mMapView != null) {
                mMapView.onResume();
            }
        }

        public void onMapStart () {
            if (mMapView != null) {
                mMapView.onStart();
            }
        }

        public void onMapLowMemory () {
            if (mMapView != null) {
                mMapView.onStart();
            }
        }

        @SuppressLint("WrongThread")
        @Override
        protected Void doInBackground(Void... voids) {
            isSlidingPanelInitialised = true;
            View photoDetailView = getLayoutInflater()
                    .inflate(R.layout.photo_info_sliding_up_panel,
                            (ViewGroup) getView(),
                            false);

            FlexboxLayout tagsWrapper = photoDetailView.findViewById(R.id.tags_wrapper);
            LinearLayout tagsLayout = photoDetailView.findViewById(R.id.photo_tags_layout);
            LinearLayout descriptionLayout = photoDetailView.findViewById(R.id.photo_description_layout);
            RelativeLayout cameraExifLayout = photoDetailView.findViewById(R.id.photo_camera_detail_layout);
            RelativeLayout cameraLayout = photoDetailView.findViewById(R.id.photo_camera_layout);
            RelativeLayout locationLayout = photoDetailView.findViewById(R.id.photo_location_layout);
            TextView photoResolution = photoDetailView.findViewById(R.id.photo_resolution_text);
            TextView viewsQuantity = photoDetailView.findViewById(R.id.views_quantity_text);
            TextView photoDate = photoDetailView.findViewById(R.id.date_taken_text);
            TextView photoCamera = photoDetailView.findViewById(R.id.photo_camera_text);
            TextView photoLens = photoDetailView.findViewById(R.id.photo_lens_text);
            TextView photoAperture = photoDetailView.findViewById(R.id.photo_aperture_text);
            TextView photoFocalLength = photoDetailView.findViewById(R.id.photo_focal_length_text);
            TextView photoExposure = photoDetailView.findViewById(R.id.photo_exposure_text);
            TextView photoISO = photoDetailView.findViewById(R.id.photo_iso_text);
            TextView photoLocation = photoDetailView.findViewById(R.id.photo_location_text);
            TextView photoDescription = photoDetailView.findViewById(R.id.photo_description);
            TextView photoLicense = photoDetailView.findViewById(R.id.photo_license_text);
            ImageButton exifArrow = photoDetailView.findViewById(R.id.photo_exif_arrow);
            mLocationArrow = photoDetailView.findViewById(R.id.photo_location_arrow);
            TextView lensTitle = photoDetailView.findViewById(R.id.photo_lens_title);

            // starts exif task
            new FetchPhotoExifTask(exifArrow, photoLens, lensTitle, photoAperture,
                    photoExposure, photoISO, cameraLayout, cameraExifLayout,
                    photoFocalLength).execute();

            viewsQuantity.setText(mItem.getViews());
            photoResolution.setText(mItem.getPhotoHighestRes());
            photoLicense.setText(mItem.getLicense());
            photoDate.setText(mItem.getDateTaken());

            // if photo has geolocation
            if (mItem.getLongitude() != 0) {
                locationLayout.setVisibility(View.VISIBLE);
                photoLocation.setText(mItem.getLocation(getActivity()));
                locationLayout.setOnClickListener(v -> {
                    if (mMapView == null) {
                        mMapView = photoDetailView.findViewById(R.id.mapView);
                        mMapView.onCreate(getArguments());
                        mMapView.onResume();
                        mMapView.getMapAsync(googleMap -> {
                            mMap = googleMap;
                            LatLng photoPoint = new LatLng(
                                    mItem.getLatitude(), mItem.getLongitude());
                            MarkerOptions myMarker = new MarkerOptions()
                                    .position(photoPoint);

                            LatLngBounds.Builder builder = new LatLngBounds.Builder();
                            builder.include(photoPoint);
                            mMap.addMarker(myMarker);
                            mMap.getUiSettings().setZoomControlsEnabled(true);
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(photoPoint, MAP_ZOOM));
                        });
                    }
                    if (mMapView.getVisibility() == View.GONE) {
                        mMapView.setVisibility(View.VISIBLE);
                        Animation mapAnim = AnimationUtils
                                .loadAnimation(getActivity(), R.anim.item_animation_fall_down);
                        mMapView.startAnimation(mapAnim);
                        Animation arrowAnim = AnimationUtils
                                .loadAnimation(getActivity(), R.anim.rotate_clockwise);
                        arrowAnim.setFillAfter(true);
                        mLocationArrow.startAnimation(arrowAnim);
                    } else {
                        Animation mapAnim = AnimationUtils
                                .loadAnimation(getActivity(),R.anim.item_animation_rise_up);
                        mMapView.startAnimation(mapAnim);
                        mapAnim.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {

                            }
                            @Override
                            public void onAnimationEnd(Animation animation) {
                                mMapView.setVisibility(View.GONE);
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {

                            }
                        });
                        Animation arrowAnim = AnimationUtils
                                .loadAnimation(getActivity(), R.anim.rotate_counter_clockwise);
                        mLocationArrow.startAnimation(arrowAnim);
                    }
                });

            }
            if (mItem.getCamera() != null) {
                cameraLayout.setVisibility(View.VISIBLE);
                photoCamera.setText(mItem.getCamera());
            }

            if (mItem.getDescription() != null) {
                descriptionLayout.setVisibility(View.VISIBLE);
                photoDescription.setText(mItem.getDescription());
            }
            // shows shows tags in sequence.
            if (mItem.getTags() != null) {
                tagsLayout.setVisibility(View.VISIBLE);

                for (String tag : mItem.getTags()) {
                    TextView textView = new TextView(getActivity());
                    textView.setText(tag);
                    textView.setTextColor(requireActivity().getColor(R.color.colorLightGrey));
                    textView.setPadding(
                            dpToPx(scale,16), 0,dpToPx(scale,16),0);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            dpToPx(scale,30)
                    );

                    params.setMargins(
                            dpToPx(scale,8), 0, 0,  dpToPx(scale,10));
                    textView.setLayoutParams(params);
                    textView.setBackground(
                            ContextCompat.getDrawable(requireActivity(),R.drawable.tag_background));
                    textView.setGravity(Gravity.CENTER);
                    textView.setOnClickListener(v -> {
                        Intent intent = SearchActivity.newIntent(
                                getActivity(), tag, SearchActivity.SEARCH_MODE, false);
                        startActivity(intent);
                    });
                    tagsWrapper.addView(textView);
                }
            }

            mHandler.post(() -> {
                Animation mapAnim = AnimationUtils
                        .loadAnimation(getActivity(), R.anim.item_animation_fall_down);
                mNestedScrollView.addView(photoDetailView);
                mNestedScrollView.startAnimation(mapAnim);
            });
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            mSlidePanelProgress.setVisibility(View.GONE);
        }
    }

    /** sends POST request to Flickr.com to add photo to user's favorites */

    private class AddFavoriteTask extends AsyncTask<String, Void, RequestResponse<?>> {

        private String photoID;

        public AddFavoriteTask(String photoID) {
            this.photoID = photoID;
        }

        @Override
        protected RequestResponse<?> doInBackground(String... strings) {
            return new FlickrFetchr().addFavs(getContext(), photoID);
        }

        @Override
        protected void onPostExecute(RequestResponse<?> response) {
            if (isAdded()) {
                if (response.getConnectionStat() == RequestResponse.CONNECTION_OK
                        && response.getResponseDataStat().equals(RequestResponse.RESPONSE_DATA_OK)) {
                    mItem.setFavorite(true);
                    mItem.increaseFavesCount();
                    mFavsQuantity.setText(mItem.getCount_faves());
                    mFavsQuantity.setCompoundDrawablesWithIntrinsicBounds(
                            R.drawable.icon_star_filled, 0, 0, 0);
                } else {
                    Toast.makeText(requireActivity(),
                            getString(R.string.internet_connection_error),
                            Toast.LENGTH_SHORT)
                            .show();
                }
                mSlidePanelProgress.setVisibility(View.INVISIBLE);
            }
        }
    }

    /** sends POST request to Flickr.com to remove photo from user's favorites */

    private class RemoveFavoriteTask extends AsyncTask<String, Void, RequestResponse<?>> {

        private String photoID;

        public RemoveFavoriteTask(String photoID) {
            this.photoID = photoID;
        }

        @Override
        protected RequestResponse<?> doInBackground(String... strings) {
            return new FlickrFetchr().removeFavs(getContext(), photoID);
        }

        @Override
        protected void onPostExecute(RequestResponse<?> response) {
            if (isAdded()) {
                if (response.getConnectionStat() == RequestResponse.CONNECTION_OK
                        && response.getResponseDataStat().equals(RequestResponse.RESPONSE_DATA_OK)) {
                    mItem.setFavorite(false);
                    mItem.decreaseFavesCount();
                    mFavsQuantity.setText(mItem.getCount_faves());
                    mFavsQuantity.setCompoundDrawablesWithIntrinsicBounds(
                            R.drawable.icon_star_stroke, 0, 0, 0);
                } else {
                    Toast.makeText(requireActivity(),
                            getString(R.string.internet_connection_error),
                            Toast.LENGTH_SHORT)
                            .show();
                }
                mSlidePanelProgress.setVisibility(View.INVISIBLE);
            }
        }
    }

    private boolean hasStoragePermissions () {
        if (Build.VERSION.SDK_INT < 29) {
            int result = ContextCompat.checkSelfPermission(requireActivity(),
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
            return result == PackageManager.PERMISSION_GRANTED;
        } else {
           return true;
        }
    }

    /** downloads image to user's device */

    private void downloadImage () {
        String photoUrl = mItem.getPhotoUrl(QueryPreferences.getPhotoDownloadQuality(requireContext()));
        if (photoUrl == null) {
            photoUrl = mItem.getPhotoHighestResUrl();
        }
        Toast.makeText(getActivity(),getString(R.string.download_start),Toast.LENGTH_SHORT)
                .show();
        DownloadManager dm = (DownloadManager) requireActivity().getSystemService(DOWNLOAD_SERVICE);
        DownloadManager.Request r = new DownloadManager.Request(Uri.parse(photoUrl));
        String fileName = mItem.getName();
        if (fileName.length() > 0) {
            fileName = fileName.replaceAll("\\W", " ");
            fileName = fileName.trim() + FILE_TYPE_TEMPLATE;
        } else {
            fileName = FILE_NAME_TEMPLATE + FILE_TYPE_TEMPLATE;
        }
        // This put the download in the same Download dir the browser uses
        r.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);

        // When downloading music and videos they will be listed in the player
        r.allowScanningByMediaScanner();

        // Notify user when download is completed
        r.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        assert dm != null;
        dm.enqueue(r);
    }

    /** first downloads image to user's device and then shares with name and original link */
    private void shareImage () {
        try {
            Bitmap bitmap = ((BitmapDrawable) mPhotoView.getDrawable()).getBitmap();
            String title = mItem.getName() + "\n\n";
            String location = "";
            if (mItem.getLatitude() != 0) {
                location = mItem.getLocation(getActivity()) + "\n\n";
            }
            String url = getString(R.string.original_link) + mItem.getPhotoHighestResUrl();

            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.putExtra(Intent.EXTRA_TEXT, title + location + url);
            String path = MediaStore.Images.Media.insertImage(
                    requireActivity().getContentResolver(), bitmap, FILE_NAME_TEMPLATE
                            + System.currentTimeMillis(), null);
            Uri screenshotUri = Uri.parse(path);
            intent.putExtra(Intent.EXTRA_STREAM, screenshotUri);
            intent.setType("image/*");
            startActivity(intent);
        } catch (ClassCastException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            Toast.makeText(requireContext(),R.string.photo_not_yet_loaded,Toast.LENGTH_SHORT)
                    .show();
        }
    }

    /** checks storage permissions and shows warning dialog window if user denied it */

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_DOWNLOAD_PERMISSION:
                if (hasStoragePermissions()) {
                    downloadImage();
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
                    builder.setMessage(getString(R.string.download_access_warning))
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                public void onClick(final DialogInterface dialog, final int id) {
                                    dialog.cancel();
                                }
                            });
                    final AlertDialog alert = builder.create();
                    alert.show();
                } break;
            case REQUEST_STORAGE_PERMISSION:
                if (hasStoragePermissions()) {
                    shareImage();
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
                    builder.setMessage(getString(R.string.storage_access_warning))
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                public void onClick(final DialogInterface dialog, final int id) {
                                    dialog.cancel();
                                }
                            });
                    final AlertDialog alert = builder.create();
                    alert.show();
                } break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}

























