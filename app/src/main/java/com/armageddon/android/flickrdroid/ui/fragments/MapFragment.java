package com.armageddon.android.flickrdroid.ui.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;

import com.armageddon.android.flickrdroid.R;
import com.armageddon.android.flickrdroid.api.FlickrFetchr;
import com.armageddon.android.flickrdroid.common.ActivityUtils;
import com.armageddon.android.flickrdroid.common.Converter;
import com.armageddon.android.flickrdroid.common.EnumCategory;
import com.armageddon.android.flickrdroid.common.GalleryItemBase;
import com.armageddon.android.flickrdroid.model.GalleryItem;
import com.armageddon.android.flickrdroid.api.RequestResponse;
import com.armageddon.android.flickrdroid.ui.activities.PhotoFullActivity;
import com.armageddon.android.flickrdroid.ui.adapters.CategoryItemAdapter;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Uses Google Maps API to show pictures on the map.
 * Fetches photos only with geolocation from 1 to "SEEK_MAX"
 * Two search options :
 *   1) near me - photos in 10 km radius near user's location (need user's permission)
 *   2) around the world - without user location
 * There is a sliding panel (SlidingUpPanelLayout) from the bottom to the middle of the screen.
 * On which all search settings are located (UI). It automatically open when this fragment is visible for user.
 */

public class MapFragment extends Fragment implements CategoryItemAdapter.CallBacks,
        LoadingFragment.CallBacks, ErrorLoadFragment.CallBacks, Converter {

    private final ConcurrentMap<GalleryItem,Bitmap> mBitmapsMap = new ConcurrentHashMap<>();
    private int mImageLoadFailCount;
    public static final int SEEK_MIDDLE_PROGRESS = 50;
    public static final int SEEK_MAX = 250;
    private static final int SEARCH_TYPE_WORLDWIDE = 0;
    private static final int SEARCH_TYPE_NEAR_ME = 1;
    private SlidingUpPanelLayout mPanel;
    private RecyclerView mCategoryRecyclerView;
    private SeekBar mSeekBar;
    private ImageView mNearMeIcon;
    private ImageView mWordWideIcon;
    private MapView mMapView;
    private Location mCurrentLocation;
    private GoogleMap mMap;
    private static final int REQUEST_LOCATION_PERMISSIONS = 9;
    private GoogleApiClient mClient;
    private static final String TAG = "MapFragment";
    private static final String[] LOCATION_PERMISSIONS = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
    };
    private Button mSearchButton;
    private int mSearchType;
    private EditText mEditText;
    private EnumCategory mCurrentCategory;
    private RequestResponse<GalleryItem> mResponse;
    private List<GalleryItem> mGalleryItemList = new ArrayList<>();
    private LoadingFragment mLoadDialog;
    private SearchTask mSearchTask;


    // Google recommendation
    @Override
    public void onStart() {
        super.onStart();
        mClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        mClient.disconnect();
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }


    public static MapFragment newInstance() {
        Bundle args = new Bundle();
        MapFragment fragment = new MapFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {

        // Google API client initializer
        mClient = new GoogleApiClient.Builder(getActivity())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle bundle) {

                    }

                    @Override
                    public void onConnectionSuspended(int i) {
                    }
                })
                .build();

        View view = layoutInflater.inflate(R.layout.fragment_search_map, viewGroup, false);

        mMapView = (MapView) view.findViewById(R.id.mapView);
        mMapView.onCreate(bundle);

        mMapView.onResume(); // needed to get the map to display immediately

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mMapView.getMapAsync(googleMap -> {
            mMap = googleMap;
        });

        mCategoryRecyclerView = view.findViewById(R.id.category_recycle_view);
        mPanel = view.findViewById(R.id.sliding_layout);
        mSeekBar = view.findViewById(R.id.seek_bar);
        mNearMeIcon = view.findViewById(R.id.icon_near_me);
        mWordWideIcon = view.findViewById(R.id.icon_worldwide);
        LinearLayout nearMeButton = view.findViewById(R.id.near_me_linearLayout);
        LinearLayout worldWideButton = view.findViewById(R.id.worldwide_linearLayout);
        mEditText = view.findViewById(R.id.edit_query);
        mSearchButton = view.findViewById(R.id.search_button);

        // strange situation with SeekBar
        if (Build.VERSION.SDK_INT < 28) {
            mSeekBar.setMax(SEEK_MAX + 5);
        } else {
            mSeekBar.setMax(SEEK_MAX);
        }
        mSearchButton.setText(getString(R.string.search_on_map_button_text, mSeekBar.getProgress()));

        if (ActivityUtils.getTheme() == ActivityUtils.THEME_DARK) {
            mSearchButton.setBackground(
                   ContextCompat.
                           getDrawable(getActivity(),R.drawable.search_button_backround_reverse));
        }
        mCategoryRecyclerView.setLayoutManager(
                new LinearLayoutManager(getActivity(), RecyclerView.HORIZONTAL, false));
        mCategoryRecyclerView.setAdapter(new CategoryItemAdapter(this,null));
        mCategoryRecyclerView.setHasFixedSize(true);
        mCategoryRecyclerView.setVisibility(View.VISIBLE);
        nearMeButton.setOnClickListener(
                v -> setSearchType(mNearMeIcon, mWordWideIcon, SEARCH_TYPE_NEAR_ME));
        worldWideButton.setOnClickListener(
                v -> setSearchType(mWordWideIcon, mNearMeIcon, SEARCH_TYPE_WORLDWIDE));

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mSearchButton.setText(getString(R.string.search_on_map_button_text, progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        setSearchType(mNearMeIcon, mWordWideIcon, SEARCH_TYPE_NEAR_ME);


        // Manually enter search keywords.
        mEditText.setOnKeyListener((v, keyCode, event) -> {
            if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                    (keyCode == KeyEvent.KEYCODE_ENTER)) {
                mEditText.setText(mEditText.getText().toString().trim());
                if (mEditText.getText().length() == 0
                        || mCurrentCategory == null
                        || !mCurrentCategory.name().equals(mEditText.getText().toString())) {
                    mCategoryRecyclerView.setAdapter(
                            new CategoryItemAdapter(
                                    MapFragment.this, null));
                }
                InputMethodManager imm = (InputMethodManager) getActivity()
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                assert imm != null;
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                mEditText.clearFocus();
                return true;
            }
            return false;
        });


        mSearchButton.setOnClickListener(v -> {
            final LocationManager manager = (LocationManager) requireActivity()
                    .getSystemService(Context.LOCATION_SERVICE);
            if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                    && mSearchType == SEARCH_TYPE_NEAR_ME) {
                buildAlertMessageNoGps();
                return;
            }

            if (hasLocationPermission()) {
                if (mSearchType == SEARCH_TYPE_WORLDWIDE) {
                    worldWideSearch();
                } else {
                    findImage();
                }
            } else {
                shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION);
                requestPermissions(LOCATION_PERMISSIONS, REQUEST_LOCATION_PERMISSIONS);
            }
        });


        return view;
    }

    /** warning dialog window if GPS is disabled */
    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setMessage(getString(R.string.GPS_OFF_warning))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.yes), (dialog, id) -> startActivity(
                        new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)))
                .setNegativeButton(getString(R.string.no), (dialog, id) -> dialog.cancel());
        final AlertDialog alert = builder.create();
        alert.show();
    }

    /** check if user allowed location permissions */
    private boolean hasLocationPermission() {
        if (mSearchType == SEARCH_TYPE_WORLDWIDE) {
            return true;
        }
        int result = ContextCompat
                .checkSelfPermission(getActivity(), LOCATION_PERMISSIONS[0]);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    /** Get device location and start photo search task */
    @SuppressLint("MissingPermission")
    private void findImage() {
        mSearchButton.setClickable(false);
        LocationRequest request = LocationRequest.create();
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);    // how to proceed in a situation of choosing between battery consumption and the accuracy of the query;
        request.setNumUpdates(1);                                       // how many times the positional data should be updated;
        request.setInterval(0);                                         // how often positional data should be updated;

        // from GOOGLE
        FusedLocationProviderClient fusedLocationClient =
                LocationServices.getFusedLocationProviderClient(getActivity());
        fusedLocationClient.getLastLocation().addOnSuccessListener(getActivity(), location -> {
            mCurrentLocation = location;
        // start search photos
            mSearchTask = new SearchTask();
            mSearchTask.execute();
        });
    }

    private void setSearchType(ImageView colorizeImage, ImageView clearColorImage, int searchType) {
        colorizeImage.setColorFilter(getAttrColor(requireActivity(),R.attr.colorAccent));
        clearColorImage.clearColorFilter();
        mSearchType = searchType;
        mSeekBar.setProgress(SEEK_MIDDLE_PROGRESS);
    }

    public void setSlidingUpPanelState(boolean actionOpen) {
        if (actionOpen) {
            mPanel.setPanelState(SlidingUpPanelLayout.PanelState.ANCHORED);
        } else {
            mPanel.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        }
    }


    private void worldWideSearch () {
        mSearchTask = new SearchTask();
        mSearchTask.execute();
    }


    /** Check Location Permissions and start search or show warning dialog */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_LOCATION_PERMISSIONS) {
            if (hasLocationPermission()) {
                findImage();
            } else {
                // if user denied location permissions and set "never ask again"
                final AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
                builder.setMessage(getString(R.string.location_access_deny_warning))
                        .setPositiveButton("Ok", (dialog, id) -> dialog.cancel());
                final AlertDialog alert = builder.create();
                alert.show();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onShowCancel() {
       mSearchTask.cancel(true);
       setSlidingUpPanelState(true);
    }

    /**
     * Photo search task.
     * Sends request with or without users location. Get photo only with geolocation.
     * Asynchronous creates a Bitmap from received URLs photo list.
     * When Bitmap is ready, the count is incriminated until the counter is equal to the number of photos searched
     */

    private class SearchTask extends AsyncTask<Location, Integer, Void> {
        @Override
        protected Void doInBackground(Location... params) {
            setSlidingUpPanelState(false);
            mSearchButton.setClickable(true);
            mLoadDialog = LoadingFragment.newInstance(MapFragment.this);
            mLoadDialog.show(requireActivity().getSupportFragmentManager(),null);
            mLoadDialog.setCancelable(false);

            mGalleryItemList.clear();
            mBitmapsMap.clear();
            Location location = null;
            if (mSearchType == SEARCH_TYPE_NEAR_ME) {
                location = mCurrentLocation;
            }

            mResponse = new FlickrFetchr().fetchSearchPhotos(getActivity(),
                    mEditText.getText().toString(),
                    String.valueOf(1),
                    location,
                    String.valueOf(mSeekBar.getProgress()),
                    true);

            if (mResponse.getConnectionStat() == RequestResponse.CONNECTION_FAIL) {
                mLoadDialog.dismiss();
                ErrorLoadFragment errorDialog = ErrorLoadFragment
                        .newInstance(
                                getString(R.string.internet_connection_error),
                                MapFragment.this);
                errorDialog.show(getActivity().getSupportFragmentManager(), null);
                return null;
            }

            if (!mResponse.getResponseDataStat().equals(RequestResponse.RESPONSE_DATA_OK)) {
                mLoadDialog.dismiss();
                ErrorLoadFragment errorDialog = ErrorLoadFragment
                        .newInstance(
                                getString(R.string.request_error),
                                MapFragment.this);
                errorDialog.show(getActivity().getSupportFragmentManager(), null);
                return null;
            }

            mGalleryItemList = mResponse.getItems();
            mLoadDialog.setProgressMax(mGalleryItemList.size());
            final int[] progres = {0};

        mImageLoadFailCount = 0;
        for (GalleryItem item : mGalleryItemList) {

            Glide.with(requireActivity())
                    .asBitmap()
                    .load(item.getUrl_m())
                    .into(new CustomTarget<Bitmap>(300,300) {
                        @Override
                        public void onResourceReady(
                                @NonNull Bitmap resource,
                                @Nullable Transition<? super Bitmap> transition) {
                            mBitmapsMap.put(item,resource);
                            publishProgress(++progres[0]);
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {
                        }

                        @Override
                        public void onLoadFailed(@Nullable Drawable errorDrawable) {
                            mImageLoadFailCount++;
                            super.onLoadFailed(errorDrawable);
                        }
                    });
        }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            Log.i(TAG, "onProgressUpdate: " + values[0]);
            mLoadDialog.setProgressMax(mGalleryItemList.size() - mImageLoadFailCount);
            mLoadDialog.setLoadingProgress(values[0]);

            // check if all Bitmaps created
            if (values[0] == mGalleryItemList.size() - mImageLoadFailCount) {
                updateUI();
            }
            super.onProgressUpdate(values);
        }
    }

    /** Shows photos and user's geolocation on the map*/
    private void updateUI() {
        mMap.clear();
        List<LatLng> itemPointsList = new ArrayList<>();

        if (mMap == null || mBitmapsMap.size() == 0) {
            return;
        }

        // creates markers for each photo and adds them on the map.
        // Snippet is used only to store photoId, but not for showing info.
        for (Map.Entry<GalleryItem, Bitmap> paar : mBitmapsMap.entrySet()) {
            GalleryItem galleryItem = paar.getKey();
            BitmapDescriptor itemBitmap = BitmapDescriptorFactory.fromBitmap(paar.getValue());
            LatLng coordinates = new LatLng(galleryItem.getLatitude(), galleryItem.getLongitude());
            itemPointsList.add(coordinates);
            MarkerOptions itemMarker = new MarkerOptions()
                    .position(coordinates)
                    .icon(itemBitmap)
                    .title(galleryItem.getName())
                    .snippet(galleryItem.getItemId().toString());
            itemMarker.draggable(true);
            mMap.addMarker(itemMarker);
        }

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (LatLng latLng : itemPointsList) {
            builder.include(latLng);
        }

        // adds user marker on the map
        if (mCurrentLocation != null) {
            LatLng myPoint = new LatLng(
                    mCurrentLocation.getLatitude(),
                    mCurrentLocation.getLongitude());
            MarkerOptions myMarker = new MarkerOptions().position(myPoint);
            mMap.addMarker(myMarker);
            builder.include(myPoint);
        }

        LatLngBounds bounds = builder.build();

        final int margin = getResources().getDimensionPixelSize(R.dimen.map_inset_margin);
        CameraUpdate update = CameraUpdateFactory.newLatLngBounds(bounds, margin);
        mMap.animateCamera(update);

        // Starts PhotoFullActivity with photoId from snippet.
        mMap.setOnMarkerClickListener(marker -> {
            UUID markerId = UUID.fromString(marker.getSnippet());
            GalleryItemBase.setResponse(getActivity(), mResponse);
            Intent intent = PhotoFullActivity.newIntent(getActivity(), markerId, null);
            startActivity(intent);
            return true;
        });
        setSlidingUpPanelState(false);
    }



    @Override
    public void callCategoryFilter(EnumCategory category) {
        mCurrentCategory = category;
        mEditText.setText(category.name());
    }

}
