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
import com.armageddon.android.flickrdroid.model.Album;
import com.armageddon.android.flickrdroid.api.RequestResponse;
import com.armageddon.android.flickrdroid.ui.adapters.ErrorAdapter;
import com.armageddon.android.flickrdroid.ui.adapters.UserAlbumListAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;


/**
 * Shows user's album items via RecycleView in detail mode.
 * For 1 request receives "FlickrFetchr.PER_PAGE" data units.
 * When the user has viewed > UNITS_LOAD_BORDER => a additional request is sent to Flickr.com
 * to receive a new portion of data.
 */

public class UserAlbumFragment extends Fragment implements Converter, ErrorAdapter.CallBacks {
    private static final String USER_ID = "user_id";
    private static final String USER_NAME = "user_name";
    private static final int UNITS_LOAD_BORDER = 50;
    private RecyclerView mRecyclerView;
    private UserAlbumListAdapter mAdapter;
    private CallBacks mCallBacks;
    private boolean isFetchingOn;
    private boolean mFirstSetup = true;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private FetchItemsTask mFetchItemsTask;
    private GridLayoutManager mLayoutManager;
    private String mUserId;
    private String mUserName;
    private RequestResponse<Album> mResponse = new RequestResponse<>();

    @Override
    public void onRefresh() {
        mSwipeRefreshLayout.setRefreshing(true);
        mFetchItemsTask.cancel(true);
        mAdapter = new UserAlbumListAdapter(mUserName);
        mRecyclerView.setAdapter(mAdapter);
        mCallBacks.setProgressBar(false);
        mResponse = new RequestResponse<>();
        updateItems();
    }

    public interface CallBacks {
        void setProgressBar(boolean on);
    }


    public static UserAlbumFragment newInstance(String userId, String userName) {
        Bundle args = new Bundle();

        args.putString(USER_ID, userId);
        args.putString(USER_NAME, userName);
        UserAlbumFragment fragment = new UserAlbumFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        assert getArguments() != null;
        mUserId = getArguments().getString(USER_ID);
        mUserName = getArguments().getString(USER_NAME);
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
            mAdapter = new UserAlbumListAdapter(mUserName);
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

    private class FetchItemsTask extends AsyncTask<String, Void, RequestResponse<Album>> {
            String page = String.valueOf(mResponse.getPage() + 1);

        @Override
        protected RequestResponse<Album> doInBackground(String... strings) {
            return new FlickrFetchr().fetchUserAlbums(getActivity(), mUserId, page);
        }

        @Override
        protected void onPostExecute(RequestResponse<Album> response) {
            mResponse = response;
            if (mResponse.getConnectionStat() == RequestResponse.CONNECTION_OK
                    && mResponse.getResponseDataStat().equals(RequestResponse.RESPONSE_DATA_OK)) {
                setupAdapter();
            } else {
                View view = getView().findViewById(R.id.margin_top_view);
                view.setVisibility(View.VISIBLE);
                mSwipeRefreshLayout.setRefreshing(false);
                mRecyclerView.setAdapter(new ErrorAdapter(response, UserAlbumFragment.this));
                mCallBacks.setProgressBar(false);
            }
        }
    }

    private void setupAdapter() {
        if (mAdapter == null) {
            mAdapter = new UserAlbumListAdapter(mUserName);
            mRecyclerView.setAdapter(mAdapter);
        } else {
            if (mFirstSetup) {
                mFirstSetup = false;
                mRecyclerView.addOnScrollListener(mScrollListener);
            }
            mAdapter.setList(mResponse.getItems());
        }
        isFetchingOn = false;
        if (mCallBacks != null) {
            mCallBacks.setProgressBar(false);
        }
        mSwipeRefreshLayout.setRefreshing(false);
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
