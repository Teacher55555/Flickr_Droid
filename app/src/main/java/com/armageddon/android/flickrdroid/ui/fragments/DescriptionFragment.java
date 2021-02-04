package com.armageddon.android.flickrdroid.ui.fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.armageddon.android.flickrdroid.R;
import com.armageddon.android.flickrdroid.api.FlickrFetchr;
import com.armageddon.android.flickrdroid.common.Converter;
import com.armageddon.android.flickrdroid.model.Group;
import com.armageddon.android.flickrdroid.api.RequestResponse;
import com.armageddon.android.flickrdroid.ui.adapters.ErrorAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;


/**
 * Group, Album or Gallery description.
 * Album and Gallery description (simple text) can be obtained from Intent and added to TextView directly.
 * But for albums needs to send a request to Flickr.com
 */

public class DescriptionFragment extends Fragment implements Converter, ErrorAdapter.CallBacks {
    private static final String DESCRIPTION = "description";
    private static final String GROUP_ID = "group_id";
    private String mDescription;
    private String mGroupId;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private TextView mTextView;
    private RecyclerView mRecyclerView;
    private FetchItemsTask mFetchItemsTask;


    public static DescriptionFragment newInstance(String description, String groupId) {
        Bundle args = new Bundle();
        args.putString(DESCRIPTION, description);
        args.putString(GROUP_ID, groupId);
        DescriptionFragment fragment = new DescriptionFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        assert getArguments() != null;
        mDescription = getArguments().getString(DESCRIPTION);
        mGroupId = getArguments().getString(GROUP_ID);
        if (mGroupId != null) {
            updateItems();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragmnet_description, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mTextView = view.findViewById(R.id.description_text_view);
        mSwipeRefreshLayout = view.findViewById(R.id.swipe_layout);
        mRecyclerView = view.findViewById(R.id.recycle_view);
        mSwipeRefreshLayout.setColorSchemeColors(getAttrColor(requireActivity(),R.attr.colorAccent));

        // Albums and galleries - only TextView
        // Groups must have a SwipeRefreshLayout and RecycleView if response will be with error.
        if (mGroupId == null) {
            mTextView.setText(mDescription);
            mSwipeRefreshLayout.setOnRefreshListener(() -> mSwipeRefreshLayout.setRefreshing(false));
        } else {
            mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            mSwipeRefreshLayout.setOnRefreshListener(this::updateItems);
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        if (mFetchItemsTask == null) {
            return;
        }
        if (mFetchItemsTask.getStatus() == AsyncTask.Status.RUNNING) {
            mSwipeRefreshLayout.setRefreshing(true);
        }
    }

    private void updateItems() {
        mFetchItemsTask = new FetchItemsTask(mGroupId);
        mFetchItemsTask.execute();
    }

    @Override
    public void onRefresh() {
        mSwipeRefreshLayout.setRefreshing(true);
        if (mGroupId == null) {
            mTextView.setText(mDescription);
            mSwipeRefreshLayout.setOnRefreshListener(() -> mSwipeRefreshLayout.setRefreshing(false));
        } else {
            mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            mSwipeRefreshLayout.setOnRefreshListener(this::updateItems);
        }
    }

    private class FetchItemsTask extends AsyncTask<String, Void, RequestResponse<Group>> {
        private final String mQuery;

        public FetchItemsTask(String query) {
            mQuery = query;
        }

        @Override
        protected RequestResponse<Group> doInBackground(String... strings) {
            return new FlickrFetchr().fetchGroupInfo(mQuery);
        }

        @Override
        protected void onPostExecute(RequestResponse<Group> response) {
            if (response.getConnectionStat() == RequestResponse.CONNECTION_OK
                    && response.getResponseDataStat().equals(RequestResponse.RESPONSE_DATA_OK)
                    && isAdded()) {
                Group group = response.getItems().get(0);
                mTextView.setText(group.getFullInfo(getActivity()));
                mRecyclerView.setVisibility(View.GONE);
            } else {
                mRecyclerView.setAdapter(new ErrorAdapter(response, DescriptionFragment.this));
            }
            mSwipeRefreshLayout.setRefreshing(false);
        }
    }
}
