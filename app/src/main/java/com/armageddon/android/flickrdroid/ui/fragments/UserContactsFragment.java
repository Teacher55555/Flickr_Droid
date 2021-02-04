package com.armageddon.android.flickrdroid.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.armageddon.android.flickrdroid.R;
import com.armageddon.android.flickrdroid.common.Converter;
import com.armageddon.android.flickrdroid.model.Person;
import com.armageddon.android.flickrdroid.ui.adapters.UserContactListAdapter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class UserContactsFragment extends Fragment implements Converter {
    private static final String CONTACTS_LIST = "contacts_list";
    private List<Person.Contact> mContacts;


    public static UserContactsFragment newInstance(Serializable contacts) {
        
        Bundle args = new Bundle();
        args.putSerializable(CONTACTS_LIST, contacts);
        UserContactsFragment fragment = new UserContactsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContacts = ((ArrayList<Person.Contact>) getArguments().getSerializable(CONTACTS_LIST));
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v =  inflater.inflate(R.layout.recycle_view, container, false);
        SwipeRefreshLayout swipeRefreshLayout = v.findViewById(R.id.swipe_layout);
        RecyclerView recyclerView = v.findViewById(R.id.recycle_view);
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(),3));
        recyclerView.setAdapter(new UserContactListAdapter(mContacts, getActivity()));
        swipeRefreshLayout.setColorSchemeColors(getAttrColor(requireActivity(),R.attr.colorAccent));
        swipeRefreshLayout.setOnRefreshListener(() -> {
            recyclerView.setAdapter(new UserContactListAdapter(mContacts, getActivity()));
            swipeRefreshLayout.setRefreshing(false);
        });
        return v;
    }
}
