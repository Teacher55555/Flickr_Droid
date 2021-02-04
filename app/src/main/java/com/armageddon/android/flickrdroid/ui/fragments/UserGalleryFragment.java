package com.armageddon.android.flickrdroid.ui.fragments;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.armageddon.android.flickrdroid.R;
import com.armageddon.android.flickrdroid.api.FlickrFetchr;
import com.armageddon.android.flickrdroid.common.Converter;
import com.armageddon.android.flickrdroid.api.RequestResponse;
import com.armageddon.android.flickrdroid.model.UserGallery;
import com.armageddon.android.flickrdroid.ui.adapters.ErrorAdapter;
import com.armageddon.android.flickrdroid.ui.adapters.UserGalleryListAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

/**
 * Shows user's gallery items via RecycleView in detail mode.
 * For 1 request receives "FlickrFetchr.PER_PAGE" data units.
 * When the user has viewed > UNITS_LOAD_BORDER => a additional request is sent to Flickr.com
 * to receive a new portion of data.
 */

public class UserGalleryFragment extends Fragment implements Converter, ErrorAdapter.CallBacks {
    private static final int UNITS_LOAD_BORDER = 50;
    private RecyclerView mRecyclerView;
    private UserGalleryListAdapter mAdapter;
    private CallBacks mCallBacks;
    private boolean isFetchingOn;
    boolean mFirstSetup = true;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private FetchItemsTask mFetchItemsTask;
    private GridLayoutManager mLayoutManager;

    @Override
    public void onRefresh() {
        mSwipeRefreshLayout.setRefreshing(true);
        mFetchItemsTask.cancel(true);
        mAdapter = new UserGalleryListAdapter();
        mRecyclerView.setAdapter(mAdapter);
        mCallBacks.setProgressBar(false);
        mResponse = new RequestResponse<>();
        updateItems();
    }

    public interface CallBacks {
        void setProgressBar(boolean on);
    }

    private static final String QUERY = "query";
    private String mQuery;
    private RequestResponse<UserGallery> mResponse = new RequestResponse<>();


    public static UserGalleryFragment newInstance(String query) {
        Bundle args = new Bundle();
        args.putString(QUERY, query);
        UserGalleryFragment fragment = new UserGalleryFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        assert getArguments() != null;
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
        mSwipeRefreshLayout = view.findViewById(R.id.swipe_layout);
        mLayoutManager = new GridLayoutManager(getActivity(),1);
        mRecyclerView = view.findViewById(R.id.gallery_item_recycle_view);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mSwipeRefreshLayout.setColorSchemeColors(getAttrColor(requireActivity(),R.attr.colorAccent));
        mSwipeRefreshLayout.setOnRefreshListener(() -> {
            mFetchItemsTask.cancel(true);
            mAdapter = new UserGalleryListAdapter();
            mRecyclerView.setAdapter(mAdapter);
            mCallBacks.setProgressBar(false);
            mResponse = new RequestResponse<>();
            updateItems();
        });
        setupAdapter();

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

    private class FetchItemsTask extends AsyncTask<String, Void, RequestResponse<UserGallery>> {
            String page = String.valueOf(mResponse.getPage() + 1);

        @Override
        protected RequestResponse<UserGallery> doInBackground(String... strings) {
            return new FlickrFetchr().fetchUserGalleries(mQuery, page);
        }

        @Override
        protected void onPostExecute(RequestResponse<UserGallery> response) {
            mResponse = response;
            if (mResponse.getConnectionStat() == RequestResponse.CONNECTION_OK
                    && mResponse.getResponseDataStat().equals(RequestResponse.RESPONSE_DATA_OK)) {
                setupAdapter();
            } else {
                View view = getView().findViewById(R.id.margin_top_view);
                view.setVisibility(View.VISIBLE);
                mSwipeRefreshLayout.setRefreshing(false);
                mRecyclerView.setAdapter(new ErrorAdapter(response, UserGalleryFragment.this));
                mCallBacks.setProgressBar(false);
            }
        }

    }

    private void setupAdapter() {
        if (mAdapter == null) {
            mAdapter = new UserGalleryListAdapter();
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
            int firstVisiblePosition = mLayoutManager.findFirstCompletelyVisibleItemPosition();
            if (!isFetchingOn && firstVisiblePosition > mAdapter.getItemCount() - UNITS_LOAD_BORDER
                    && mResponse.getPage() < mResponse.getPages()) {
                isFetchingOn = true;
                updateItems();
            }
        }
    };

}
