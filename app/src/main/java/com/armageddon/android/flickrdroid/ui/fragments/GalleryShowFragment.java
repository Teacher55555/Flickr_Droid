package com.armageddon.android.flickrdroid.ui.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.armageddon.android.flickrdroid.R;
import com.armageddon.android.flickrdroid.api.FlickrFetchr;
import com.armageddon.android.flickrdroid.common.Converter;
import com.armageddon.android.flickrdroid.common.GalleryItemBase;
import com.armageddon.android.flickrdroid.common.QueryPreferences;
import com.armageddon.android.flickrdroid.common.QueryTypes;
import com.armageddon.android.flickrdroid.model.GalleryItem;
import com.armageddon.android.flickrdroid.api.RequestResponse;
import com.armageddon.android.flickrdroid.ui.activities.PhotoFullActivity;
import com.armageddon.android.flickrdroid.ui.adapters.ErrorAdapter;
import com.armageddon.android.flickrdroid.ui.adapters.GalleryItemAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

/**
 * Shows items (photos, albums, galleries) via RecycleView in preview mode.
 * For 1 request receives "FlickrFetchr.PER_PAGE" data units.
 * When the user has viewed 50% of "FlickrFetchr.PER_PAGE" , a additional request is sent to Flickr.com
 * to receive a new portion of data.
 */

public class GalleryShowFragment extends Fragment
        implements GalleryItemAdapter.CallBacks, Converter, ErrorAdapter.CallBacks {
    private static final int REQUEST_CODE_POSITION = 0;
    private RecyclerView mRecyclerView;
    private GalleryItemAdapter mAdapter;
    private List<GalleryItem> mGalleryItemList = new ArrayList<>();
    private int galleryViewColumns; // default is 3
    private CallBacks mCallBacks;
    private boolean isFetchingOn;
    private GridLayoutManager layoutManager;
    boolean mFirstSetup = true;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private FetchItemsTask mFetchItemsTask;
    private View mMarginTopView;
    private static final String CATEGORY = "category";
    private static final String QUERY_TYPE = "query_type";
    private static final String QUERY = "query";
    private String mQueryType;
    private String mQuery;
    private String mCategory;
    private RequestResponse<GalleryItem> mResponse = new RequestResponse<>();

    public interface CallBacks {
        void setProgressBar(boolean on);
    }

    public static GalleryShowFragment newInstance(String queryType, String category, String query) {
        Bundle args = new Bundle();
        args.putString(CATEGORY, category);
        args.putString(QUERY_TYPE, queryType);
        args.putString(QUERY, query);
        GalleryShowFragment fragment = new GalleryShowFragment();
        fragment.setArguments(args);
        return fragment;
    }


    /**
     * Saves response data and all other elements to base.
     * Starts PhotoFullActivity with clicked itemId
     */

    @Override
    public void onItemClicked(UUID itemId, int position) {
        mFetchItemsTask.cancel(true);
        mResponse.setItems(mGalleryItemList);
        mResponse.setCategory(mCategory);
        mResponse.setQuery(mQuery);
        mResponse.setQueryType(mQueryType);
        GalleryItemBase.setResponse(getActivity(),mResponse);
        Intent intent = PhotoFullActivity.newIntent(
                    getActivity(),
                    itemId,
                    null);
        startActivityForResult(intent, REQUEST_CODE_POSITION);
    }

    /**
     * when user press back button loads response data and all other elements from base.
     * And scrolls GridlayoutManager to last viewed item in full size view.
     */

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        if (requestCode == REQUEST_CODE_POSITION && data != null) {
            UUID currentItemId = (UUID) PhotoFullActivity.getGalleryItemPosition(data);
            mResponse = GalleryItemBase.getResponse(getActivity());
            mGalleryItemList = mResponse.getItems();
            mAdapter = null;
            setupAdapter();
            mAdapter.setList(mGalleryItemList);

            for (int i = 0; i < mGalleryItemList.size(); i++) {
                if (mGalleryItemList.get(i).getItemId().equals(currentItemId)) {
                    layoutManager.scrollToPositionWithOffset(i,0);
                }
            }
        }
    }

    @Override
    public void onRefresh() {
        mMarginTopView.setVisibility(View.GONE);
        mSwipeRefreshLayout.setRefreshing(true);
        onRestart();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        galleryViewColumns = QueryPreferences.getGalleryViewColumns(getActivity());
        assert getArguments() != null;
        mCategory = getArguments().getString(CATEGORY);
        mQueryType = getArguments().getString(QUERY_TYPE);
        mQuery = getArguments().getString(QUERY);
        updateItems();
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.gallery_show, container, false);
    }



    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mMarginTopView = view.findViewById(R.id.margin_top_view);
        mSwipeRefreshLayout = view.findViewById(R.id.swipe_layout);
        mRecyclerView = view.findViewById(R.id.gallery_item_recycle_view);
        layoutManager = new GridLayoutManager(getActivity(), galleryViewColumns);
        mRecyclerView.setLayoutManager(layoutManager);
        mSwipeRefreshLayout.setColorSchemeColors(getAttrColor(requireActivity(),R.attr.colorAccent));
        mSwipeRefreshLayout.setOnRefreshListener(this::onRestart);
        setupAdapter();
    }


    private void onRestart () {
        galleryViewColumns = QueryPreferences.getGalleryViewColumns(getActivity());
        mFetchItemsTask.cancel(true);
        mAdapter = new GalleryItemAdapter(this, mQueryType);
        mRecyclerView.setAdapter(mAdapter);
        layoutManager = new GridLayoutManager(getActivity(), galleryViewColumns);
        mRecyclerView.setLayoutManager(layoutManager);
        mGalleryItemList.clear();
        mCallBacks.setProgressBar(false);
        mResponse = new RequestResponse<>();
        mFetchItemsTask = new FetchItemsTask();
        mFetchItemsTask.execute();
    }

    @Override
    public void onStart() {
        if (galleryViewColumns != QueryPreferences.getGalleryViewColumns(getActivity())) {
            onRestart();
        }
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mFetchItemsTask.getStatus() == AsyncTask.Status.RUNNING) {
            mSwipeRefreshLayout.setRefreshing(true);
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mCallBacks = (CallBacks) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mFetchItemsTask.cancel(true);
        mCallBacks = null;
    }

    private void updateItems() {
        if (mSwipeRefreshLayout != null && !mSwipeRefreshLayout.isRefreshing()) {
            mCallBacks.setProgressBar(true);
        }
        mFetchItemsTask = new FetchItemsTask();
        mFetchItemsTask.execute();
    }

    private class FetchItemsTask extends AsyncTask<String, Void, RequestResponse<GalleryItem>> {
        @Override
        protected RequestResponse<GalleryItem> doInBackground(String... strings) {
            String page = String.valueOf(mResponse.getPage() + 1);
            switch (mQueryType) {
                case QueryTypes.INTERESTINGNESS:
                    if (mCategory == null) {
                        return new FlickrFetchr().fetchInterestingPhotos(getActivity(),mQuery, page, null);
                    } else {
                        return new FlickrFetchr().fetchInterestingPhotosByTag(getActivity(),mCategory, page);
                    }
                case QueryTypes.RECENT:
                    if (mCategory == null) {
                        return new FlickrFetchr().fetchRecentPhotos(getActivity(),page);
                    } else {
                        return new FlickrFetchr().fetchRecentPhotosByTag(getActivity(),mCategory, page);
                    }
                case QueryTypes.PHOTO:
                    return new FlickrFetchr()
                            .fetchSearchPhotos(getActivity(),mQuery, page, null, null,false);
                case QueryTypes.PUBLIC_PHOTO:
                    return new FlickrFetchr().fetchPublicPhotos(getActivity(),mQuery, page);
                case QueryTypes.USER_FAVES:
                    return new FlickrFetchr().fetchUserFaves(getActivity(), mQuery, page);
                case QueryTypes.USER_GALLERY:
                    return new FlickrFetchr().fetchUserGallery(getActivity(), mQuery, page);
                case QueryTypes.USER_GROUP:
                    return new FlickrFetchr().fetchUserGroupPhotos(getActivity(), mQuery, page);
                case QueryTypes.USER_ALBUM:
                    String [] idArray = mQuery.split(" ");
                    return new FlickrFetchr().fetchUserAlbum(getActivity(),idArray[0], idArray[1], page);
                case QueryTypes.CAMERA_ROLL:
                    return new FlickrFetchr().fetchUserCameraRollPhotos(getActivity(), page);
                case QueryTypes.CONTACTS_PHOTO:
                    return new FlickrFetchr().fetchUserContactsPhotos(getActivity(), page);
            }
            return null;
        }

        @Override
        protected void onPostExecute(RequestResponse<GalleryItem> response) {
            mResponse = response;
            if (mResponse.getConnectionStat() == RequestResponse.CONNECTION_OK
                    && mResponse.getResponseDataStat().equals(RequestResponse.RESPONSE_DATA_OK)) {
                mGalleryItemList.addAll(response.getItems());
                setupAdapter();
            } else {
                mMarginTopView.setVisibility(View.VISIBLE);
                mCallBacks.setProgressBar(false);
                mSwipeRefreshLayout.setRefreshing(false);
                mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
                mRecyclerView.setAdapter(new ErrorAdapter(response, GalleryShowFragment.this));
            }
        }
    }

    private void setupAdapter() {
        if (mAdapter == null) {
            mAdapter = new GalleryItemAdapter(this, mQueryType);
            mRecyclerView.setAdapter(mAdapter);
        } else {
            if (mFirstSetup) {
                mSwipeRefreshLayout.setRefreshing(false);
                mFirstSetup = false;
                mRecyclerView.addOnScrollListener(mScrollListener);
            }
            mAdapter.setList(mResponse.getItems());
        }
        isFetchingOn = false;
        if (mCallBacks != null) {
            mCallBacks.setProgressBar(false);
            if (!mFirstSetup) {
                mSwipeRefreshLayout.setRefreshing(false);
            }
        }
    }

    RecyclerView.OnScrollListener mScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            int firstVisiblePosition = layoutManager.findFirstCompletelyVisibleItemPosition();
            // if user scrolled > 50 % from received items => load more
                if (!isFetchingOn && firstVisiblePosition > mAdapter.getItemCount()
                        - Integer.parseInt(FlickrFetchr.PER_PAGE)/2
                        && mResponse.getPage() < mResponse.getPages()) {
                    isFetchingOn = true;
                    updateItems();
                }
        }
    };

}
