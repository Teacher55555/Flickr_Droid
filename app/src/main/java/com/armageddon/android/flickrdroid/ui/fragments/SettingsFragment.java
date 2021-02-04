package com.armageddon.android.flickrdroid.ui.fragments;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.armageddon.android.flickrdroid.R;
import com.armageddon.android.flickrdroid.common.ActivityUtils;
import com.armageddon.android.flickrdroid.common.Converter;
import com.armageddon.android.flickrdroid.common.QueryPreferences;
import com.armageddon.android.flickrdroid.model.GalleryItem;
import com.armageddon.android.flickrdroid.ui.activities.AboutActivity;
import com.armageddon.android.flickrdroid.ui.activities.PhotoFullActivity;
import com.armageddon.android.flickrdroid.ui.activities.SettingsActivity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.cache.DiskCache;

import java.io.File;
import java.util.Arrays;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

/**
 * Settings:
 *  - columns quantity in photo preview mode
 *  - photo display quality
 *  - quality of downloaded photos
 *  - slide show interval
 *  - Theme (light,dark)
 *  - Cash size (clear on clicks)
 *  - about app
 *  Settings stores vis QueryPreferences.class
 */

public class SettingsFragment extends Fragment implements Converter {
   private ImageView mGallery1BlockButton;
   private ImageView mGallery2BlockButton;
   private ImageView mGallery3BlockButton;

   private Button mPictureViewQualitySmall;
   private Button mPictureViewQualityNormal;
   private Button mPictureViewQualityLarge;

   private Button mPictureDownloadQualitySmall;
   private Button mPictureDownloadQualityNormal;
   private Button mPictureDownloadQualityLarge;
   private Button mPictureDownloadQualityMax;

   private Button mSlideShowInterval3;
   private Button mSlideShowInterval4;
   private Button mSlideShowInterval5;
   private Button mSlideShowInterval10;
   private Button mSlideShowInterval15;

   private Button mThemeDefaultButton;
   private Button mThemeLightButton;
   private Button mThemeDarkButton;

   private TextView mCacheView;
   private ProgressBar mProgressBar;

   private CallBacks mCallBacks;

    private enum MenuMethod {
        photoViewQuality,
        photoDownloadQuality,
        slideshowDuration,
        appTheme;
    }

    public interface CallBacks {
        void onGalleryInRowChanged ();
    }

    public static SettingsFragment newInstance() {
        Bundle args = new Bundle();
        SettingsFragment fragment = new SettingsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        mCallBacks = (CallBacks) getActivity();
    }

    @Override
    public void onStop() {
        super.onStop();
        mCallBacks = null;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mGallery3BlockButton = view.findViewById(R.id.settings_3_in_row_button);
        mGallery2BlockButton = view.findViewById(R.id.settings_2_in_row_button);
        mGallery1BlockButton = view.findViewById(R.id.settings_1_in_row_button);
        mPictureViewQualitySmall = view.findViewById(R.id.picture_quality_640_button);
        mPictureViewQualityNormal = view.findViewById(R.id.picture_quality_1024_button);
        mPictureViewQualityLarge= view.findViewById(R.id.picture_quality_2048_button);
        mPictureDownloadQualitySmall = view.findViewById(R.id.picture_quality_download_640_button);
        mPictureDownloadQualityNormal = view.findViewById(R.id.picture_quality_download_1024_button);
        mPictureDownloadQualityLarge = view.findViewById(R.id.picture_quality_download_2048_button);
        mPictureDownloadQualityMax = view.findViewById(R.id.picture_quality_download_max_button);
        mSlideShowInterval3 = view.findViewById(R.id.slideshow_3_sec_button);
        mSlideShowInterval4 = view.findViewById(R.id.slideshow_4_sec_button);
        mSlideShowInterval5 = view.findViewById(R.id.slideshow_5_sec_button);
        mSlideShowInterval10 = view.findViewById(R.id.slideshow_10_sec_button);
        mSlideShowInterval15 = view.findViewById(R.id.slideshow_15_sec_button);
        mThemeDefaultButton = view.findViewById(R.id.theme_default_button);
        mThemeLightButton = view.findViewById(R.id.theme_light_button);
        mThemeDarkButton = view.findViewById(R.id.theme_dark_button);
        mCacheView = view.findViewById(R.id.cache_size_view);
        TextView aboutView = view.findViewById(R.id.about_text_view);
        mProgressBar = view.findViewById(R.id.progress);

        aboutView.setOnClickListener(v -> startActivity(AboutActivity.newIntent(requireActivity())));

        // show cash size
        new GetSizeTask(mCacheView).execute(
                new File(requireActivity().getCacheDir(), DiskCache.Factory.DEFAULT_DISK_CACHE_DIR));

        // clear cash size
        mCacheView.setOnClickListener(v -> {
            mProgressBar.setVisibility(View.VISIBLE);
            mCacheView.setText("");
            Thread clearCacheTask = new Thread(() -> {
                Glide.get(requireContext()).clearDiskCache();
                new GetSizeTask(mCacheView).execute(
                        new File(requireActivity().getCacheDir(),
                                DiskCache.Factory.DEFAULT_DISK_CACHE_DIR));
            });
            clearCacheTask.start();
        });

        switch (QueryPreferences.getGalleryViewColumns(getActivity())) {
            case 1 : mGallery1BlockButton.setColorFilter(
                    getAttrColor(requireActivity(),R.attr.colorAccent)); break;
            case 2 : mGallery2BlockButton.setColorFilter(
                    getAttrColor(requireActivity(),R.attr.colorAccent)); break;
            case 3 : mGallery3BlockButton.setColorFilter(
                    getAttrColor(requireActivity(),R.attr.colorAccent)); break;
        }

        mGallery3BlockButton.setOnClickListener(v -> setColumns( (ImageView) v,
                mGallery1BlockButton, mGallery2BlockButton, 3));
        mGallery2BlockButton.setOnClickListener(v -> setColumns( (ImageView) v,
                mGallery1BlockButton, mGallery3BlockButton, 2));
        mGallery1BlockButton.setOnClickListener(v -> setColumns( (ImageView) v,
                mGallery2BlockButton, mGallery3BlockButton, 1));


        switch (QueryPreferences.getPhotoViewQuality(getActivity())) {
            case GalleryItem.PHOTO_SIZE_2048 : setItemColor(mPictureViewQualityLarge,true);
                 break;
            case GalleryItem.PHOTO_SIZE_1024 : setItemColor(mPictureViewQualityNormal,true);
                 break;
            case GalleryItem.PHOTO_SIZE_640 : setItemColor(mPictureViewQualitySmall,true);
                 break;
        }

        mPictureViewQualitySmall.setOnClickListener(v -> setMenuItemActive(
                MenuMethod.photoViewQuality,
                GalleryItem.PHOTO_SIZE_640,
                v,
                mPictureViewQualityNormal,
                mPictureViewQualityLarge));

        mPictureViewQualityNormal.setOnClickListener(v -> setMenuItemActive(
                MenuMethod.photoViewQuality,
                GalleryItem.PHOTO_SIZE_1024,
                v,
                mPictureViewQualitySmall,
                mPictureViewQualityLarge));

        mPictureViewQualityLarge.setOnClickListener(v -> setMenuItemActive(
                MenuMethod.photoViewQuality,
                GalleryItem.PHOTO_SIZE_2048,
                v,
                mPictureViewQualitySmall,
                mPictureViewQualityNormal));


        switch (QueryPreferences.getPhotoDownloadQuality(getActivity())) {
            case GalleryItem.PHOTO_SIZE_MAX : setItemColor(mPictureDownloadQualityMax,true);
                break;
            case GalleryItem.PHOTO_SIZE_2048 : setItemColor(mPictureDownloadQualityLarge,true);
                break;
            case GalleryItem.PHOTO_SIZE_1024 : setItemColor(mPictureDownloadQualityNormal,true);
                break;
            case GalleryItem.PHOTO_SIZE_640 : setItemColor(mPictureDownloadQualitySmall,true);
                break;
        }

        mPictureDownloadQualitySmall.setOnClickListener(v -> setMenuItemActive(
                MenuMethod.photoDownloadQuality,
                GalleryItem.PHOTO_SIZE_640,
                v,
                mPictureDownloadQualityNormal,
                mPictureDownloadQualityLarge,
                mPictureDownloadQualityMax));

        mPictureDownloadQualityNormal.setOnClickListener(v -> setMenuItemActive(
                MenuMethod.photoDownloadQuality,
                GalleryItem.PHOTO_SIZE_1024,
                v,
                mPictureDownloadQualitySmall,
                mPictureDownloadQualityLarge,
                mPictureDownloadQualityMax));

        mPictureDownloadQualityLarge.setOnClickListener(v -> setMenuItemActive(
                MenuMethod.photoDownloadQuality,
                GalleryItem.PHOTO_SIZE_2048,
                v,
                mPictureDownloadQualitySmall,
                mPictureDownloadQualityNormal,
                mPictureDownloadQualityMax));

        mPictureDownloadQualityMax.setOnClickListener(v -> setMenuItemActive(
                MenuMethod.photoDownloadQuality,
                GalleryItem.PHOTO_SIZE_MAX,
                v,
                mPictureDownloadQualitySmall,
                mPictureDownloadQualityNormal,
                mPictureDownloadQualityLarge));

        switch (QueryPreferences.getSlideShowInterval(getActivity())) {
            case PhotoFullActivity
                    .SLIDESHOW_INTERVAL_3_SEC : setItemColor(mSlideShowInterval3,true);
                 break;
            case PhotoFullActivity
                    .SLIDESHOW_INTERVAL_4_SEC : setItemColor(mSlideShowInterval4,true);
                break;
            case PhotoFullActivity
                    .SLIDESHOW_INTERVAL_5_SEC : setItemColor(mSlideShowInterval5,true);
                break;
            case PhotoFullActivity
                    .SLIDESHOW_INTERVAL_10_SEC : setItemColor(mSlideShowInterval10,true);
                break;
            case PhotoFullActivity
                    .SLIDESHOW_INTERVAL_15_SEC : setItemColor(mSlideShowInterval15,true);
                break;
        }

        mSlideShowInterval3.setOnClickListener(v -> setMenuItemActive(
                MenuMethod.slideshowDuration,
                PhotoFullActivity.SLIDESHOW_INTERVAL_3_SEC,
                v,
                mSlideShowInterval4,
                mSlideShowInterval5,
                mSlideShowInterval10,
                mSlideShowInterval15));

        mSlideShowInterval4.setOnClickListener(v -> setMenuItemActive(
                MenuMethod.slideshowDuration,
                PhotoFullActivity.SLIDESHOW_INTERVAL_4_SEC,
                v,
                mSlideShowInterval3,
                mSlideShowInterval5,
                mSlideShowInterval10,
                mSlideShowInterval15));

        mSlideShowInterval5.setOnClickListener(v -> setMenuItemActive(
                MenuMethod.slideshowDuration,
                PhotoFullActivity.SLIDESHOW_INTERVAL_5_SEC,
                v,
                mSlideShowInterval3,
                mSlideShowInterval4,
                mSlideShowInterval10,
                mSlideShowInterval15));

        mSlideShowInterval10.setOnClickListener(v -> setMenuItemActive(
                MenuMethod.slideshowDuration,
                PhotoFullActivity.SLIDESHOW_INTERVAL_10_SEC,
                v,
                mSlideShowInterval3,
                mSlideShowInterval4,
                mSlideShowInterval5,
                mSlideShowInterval15));

        mSlideShowInterval15.setOnClickListener(v -> setMenuItemActive(
                MenuMethod.slideshowDuration,
                PhotoFullActivity.SLIDESHOW_INTERVAL_15_SEC,
                v,
                mSlideShowInterval3,
                mSlideShowInterval4,
                mSlideShowInterval5,
                mSlideShowInterval10));

        switch (QueryPreferences.getAppTheme(getActivity())) {
            case ActivityUtils.THEME_DEFAULT : setItemColor(mThemeDefaultButton, true);
                break;
            case ActivityUtils.THEME_WHITE : setItemColor(mThemeLightButton, true);
                break;
            case ActivityUtils.THEME_DARK : setItemColor(mThemeDarkButton, true);
                break;
        }


        mThemeDefaultButton.setOnClickListener(v -> setMenuItemActive(
                MenuMethod.appTheme,
                ActivityUtils.THEME_DEFAULT,
                v,
                mThemeLightButton,
                mThemeDarkButton));

        mThemeLightButton.setOnClickListener(v -> setMenuItemActive(
                MenuMethod.appTheme,
                ActivityUtils.THEME_WHITE,
                v,
                mThemeDefaultButton,
                mThemeDarkButton));

        mThemeDarkButton.setOnClickListener(v -> setMenuItemActive(
                MenuMethod.appTheme,
                ActivityUtils.THEME_DARK,
                v,
                mThemeDefaultButton,
                mThemeLightButton));

    }

    private void setMenuItemActive (MenuMethod menuMethod,
                                    int menuValue,
                                    View activeButton,
                                    Button ...resetButtons) {
        switch (menuMethod) {
            case photoViewQuality:
                QueryPreferences.setPhotoViewQuality(getActivity(), menuValue);
                break;
            case photoDownloadQuality:
                QueryPreferences.setPhotoDownloadQuality(getActivity(), menuValue);
                break;
            case slideshowDuration:
                QueryPreferences.setSlideShowInterval(getActivity(), menuValue);
                break;
            case appTheme:
                QueryPreferences.setAppTheme(getActivity(),menuValue);
                Intent intent = SettingsActivity.newIntent(getActivity(), true);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                requireActivity().overridePendingTransition(0, 0);
                break;
        }
        setItemColor((Button) activeButton, true);
        for (Button btn : resetButtons) {
            setItemColor(btn, false);
        }
    }

    private void setColumns(ImageView imageView, ImageView buttonClear1, ImageView buttonClear2,
                            int columns) {
        if (QueryPreferences.getGalleryViewColumns(getActivity()) != columns) {
            imageView.setColorFilter(getAttrColor(requireActivity(),R.attr.colorAccent));
            QueryPreferences.setGalleryViewColumns(getActivity(), columns);
            buttonClear1.clearColorFilter();
            buttonClear2.clearColorFilter();
            mCallBacks.onGalleryInRowChanged();
        }
    }

    private void setItemColor (Button button, boolean isActive) {
        if (isActive) {
            button.setBackground(ContextCompat.getDrawable(
                    requireActivity(),
                    R.drawable.settings_active_background));
            button.setTextColor(ContextCompat.getColor(requireActivity(), R.color.colorWhite));
        } else {
            button.setBackground(ContextCompat.getDrawable(
                    requireActivity(),
                    R.drawable.settings_default_background));
              button.setTextColor(getAttrColor(requireActivity(),R.attr.titleTextColor));

        }
    }

    class GetSizeTask extends AsyncTask<File, Long, Long> {
        private final TextView resultView;
        public GetSizeTask(TextView resultView) { this.resultView = resultView; }
        @Override protected Long doInBackground(File... dirs) {
            long totalSize = 0;
            try {

                for (File dir : dirs) {
                    publishProgress(totalSize);
                    totalSize += calculateSize(dir);
                }
            } catch (RuntimeException ex) {
                final String message = String.format("Cannot get size of %s: %s",
                        Arrays.toString(dirs), ex);
                new Handler(Looper.getMainLooper()).post(() -> {
                    resultView.setText(R.string.error);
                    Toast.makeText(resultView.getContext(), message, Toast.LENGTH_LONG).show();
                });
            }
            return totalSize;
        }

        @Override protected void onPostExecute(Long size) {
            String sizeText = android.text.format.Formatter
                    .formatFileSize(resultView.getContext(), size);
            resultView.setText(sizeText);
            mProgressBar.setVisibility(View.GONE);
        }
        private long calculateSize(File dir) {
            if (dir == null) return 0;
            if (!dir.isDirectory()) return dir.length();
            long result = 0;
            File[] children = dir.listFiles();
            if (children != null)
                for (File child : children)
                    result += calculateSize(child);
            return result;
        }
    }
}

