package com.armageddon.android.flickrdroid.ui.fragments;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.armageddon.android.flickrdroid.R;
import com.armageddon.android.flickrdroid.api.FlickrFetchr;
import com.armageddon.android.flickrdroid.common.Converter;
import com.armageddon.android.flickrdroid.common.LogoIcon;
import com.armageddon.android.flickrdroid.model.Person;
import com.armageddon.android.flickrdroid.api.RequestResponse;
import com.armageddon.android.flickrdroid.ui.activities.UserContactsActivity;
import com.armageddon.android.flickrdroid.ui.activities.UserPersonalPageActivity;
import com.armageddon.android.flickrdroid.ui.adapters.ErrorAdapter;
import com.bumptech.glide.Glide;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

/**
 * Shows user'sinformation.
 * Two UI modes:
 * 1. When user was searched via SearchActivity
 * 2. When in menu was clicked "About user"
 */

public class PersonsFragment extends Fragment implements Converter, ErrorAdapter.CallBacks {
    private static final String QUERY = "query";
    private static final String USER_ID = "user_id";

    private String mQuery;
    private String mUserId;

    private ImageView mImageView;
    private TextView mRealname;
    private TextView mUsername;
    private TextView mLocation;
    private TextView mDescription;
    private TextView mPhotos;
    private TextView mPhotoDate;
    private TextView mProFlag;
    private TextView mContactsCount;
    private ConstraintLayout mPhotoCountField;
    private LinearLayout mPhotoDateLayout;
    private LinearLayout mLocationLayout;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerView;
    private Person mPerson;
    private FrameLayout mPersonIconLayout;
    private LinearLayout mPersonNameLayout;
    private ConstraintLayout mContactsLayout;
    private FetchItemsTask mFetchItemsTask;


    public static PersonsFragment newInstance(String query, String userId) {

        Bundle args = new Bundle();
        args.putString(QUERY, query);
        args.putString(USER_ID, userId);
        PersonsFragment fragment = new PersonsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        assert getArguments() != null;
        mQuery = getArguments().getString(QUERY);
        mUserId = getArguments().getString(USER_ID);
        updateItems();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

         View v = inflater.inflate(R.layout.fragment_search_person, container, false);
         mImageView = v.findViewById(R.id.person_icon);
         mRealname = v.findViewById(R.id.person_real_name);
         mUsername = v.findViewById(R.id.person_user_name);
         mLocation = v.findViewById(R.id.person_location);
         mDescription = v.findViewById(R.id.person_description);
         mProFlag = v.findViewById(R.id.person_pro_flag);
         mPhotos = v.findViewById(R.id.person_photos_count);
         mContactsCount = v.findViewById(R.id.person_contacts_count);
         mPhotoDate = v.findViewById(R.id.person_photos_date);
         mPhotoDateLayout = v.findViewById(R.id.person_photos_date_layout);
         mLocationLayout = v.findViewById(R.id.person_location_layout);
         mSwipeRefreshLayout = v.findViewById(R.id.swipe_layout);
         mRecyclerView = v.findViewById(R.id.recycle_view);
         mPhotoCountField = v.findViewById(R.id.person_photos_count_layout);
         mPersonIconLayout = v.findViewById(R.id.person_icon_layout);
         mPersonNameLayout = v.findViewById(R.id.person_name_layout);
         mContactsLayout = v.findViewById(R.id.user_contacts_layout);

         mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
         mRecyclerView.setVisibility(View.VISIBLE);
         mSwipeRefreshLayout.setColorSchemeColors(getAttrColor(requireActivity(),R.attr.colorAccent));

         mSwipeRefreshLayout.setOnRefreshListener(this::updateItems);

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mFetchItemsTask.getStatus() == AsyncTask.Status.RUNNING) {
            mSwipeRefreshLayout.setRefreshing(true);
        }
    }

    private void updateItems() {
        mFetchItemsTask = new FetchItemsTask(mQuery);
        mFetchItemsTask.execute();
    }

    @Override
    public void onRefresh() {
        mSwipeRefreshLayout.setRefreshing(true);
        updateItems();
    }

    private class FetchItemsTask extends AsyncTask<String, Void, RequestResponse<Person>> {
        private String mQuery;

        public FetchItemsTask(String query) {
            mQuery = query;
        }

        @Override
        protected RequestResponse<Person> doInBackground(String... strings) {
            if (mUserId == null) {
                return new FlickrFetchr().findUser(mQuery);
            }
            return new FlickrFetchr().getPersonInfo(mUserId);
        }

        @Override
        protected void onPostExecute(RequestResponse<Person> response) {
            if (response.getConnectionStat() == RequestResponse.CONNECTION_OK
                    && response.getResponseDataStat().equals(RequestResponse.RESPONSE_DATA_OK)
                    && isAdded()) {
                mPerson = response.getItems().get(0);
                PersonClickListener personClickListener = new PersonClickListener();
                mImageView.setOnClickListener(personClickListener);
                if (mQuery != null) {
                    Glide.with(PersonsFragment.this)
                            .load(mPerson.getIconUrl(LogoIcon.big_150px))
                            .into(mImageView);
                } else {
                    mPersonIconLayout.setVisibility(View.GONE);
                }
                if (mQuery != null) {
                    mUsername.setText(mPerson.getUsername());
                    mUsername.setOnClickListener(personClickListener);
                    if (mPerson.getRealname() != null) {
                        mRealname.setText(mPerson.getRealname());
                        mRealname.setOnClickListener(personClickListener);
                    } else {
                        mRealname.setVisibility(View.GONE);
                    }
                } else {
                    mPersonNameLayout.setVisibility(View.GONE);
                }

                mPhotos.setText(mPerson.getPublicPhotoCount());
                mPhotoCountField.setOnClickListener(personClickListener);
                if (mPerson.getPhotoFirstDate() == null) {
                    mPhotoDateLayout.setVisibility(View.GONE);
                } else {
                    mPhotoDate.setText(mPerson.getPhotoFirstDate());
                }
                mContactsCount.setText(mPerson.getContactsCount());
                if (Integer.parseInt(mPerson.getContactsCount()) > 0) {
                    mContactsLayout.setOnClickListener(v -> {
                        Intent intent = UserContactsActivity.newIntent(getActivity(),
                                (ArrayList<Person.Contact>) mPerson.getContacts());
                        startActivity(intent);
                    });
                }
                if (mPerson.getLocation() == null) {
                    mLocationLayout.setVisibility(View.GONE);
                } else {
                    mLocation.setText(mPerson.getLocation());
                }

                if (mPerson.getDescription() == null) {
                    mDescription.setVisibility(View.GONE);
                } else {
                    mDescription.setText(mPerson.getDescription());
                }

                if (mPerson.isPro() == Person.USER_NOT_PRO) {
                    mProFlag.setVisibility(View.GONE);
                }
                mRecyclerView.setVisibility(View.GONE);
            } else {
                mRecyclerView.setAdapter(new ErrorAdapter(response, PersonsFragment.this));
            }
            mSwipeRefreshLayout.setRefreshing(false);
        }
    }

   private class PersonClickListener implements View.OnClickListener {

       @Override
       public void onClick(View v) {
           String name = mPerson.getRealname();
           if (name == null) {
               name = mPerson.getUsername();
           }

           Intent intent = UserPersonalPageActivity.newIntent(
                   getActivity(),
                   mPerson.getId(),
                   name,
                   mPerson.getIconUrl(LogoIcon.huge_300px));
           startActivity(intent);
       }
   }

}
