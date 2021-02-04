package com.armageddon.android.flickrdroid.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.armageddon.android.flickrdroid.R;
import com.armageddon.android.flickrdroid.common.ActivityUtils;
import com.armageddon.android.flickrdroid.model.Person;
import com.armageddon.android.flickrdroid.ui.adapters.UserContactListAdapter;
import com.armageddon.android.flickrdroid.ui.fragments.PersonsFragment;
import com.armageddon.android.flickrdroid.ui.fragments.UserContactsFragment;

import java.util.ArrayList;

import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

/**
 * Shows User UserContactsFragment and switch to PersonsFragment
 * when user clicks on contact from the list
 */

public class UserContactsActivity extends SlideMenuActivity implements UserContactListAdapter.CallBacks {
    private static final String CONTACT_LIST = "contact_list";
    private static final String CONTACTS_LIST_FRAGMENT = "contact_list_fragment";
    private static final String PERSON_FRAGMENT = "person_fragment";

    private Toolbar mToolbar;


    public static Intent newIntent(Context context, ArrayList<Person.Contact> contacts) {
        Intent intent = new Intent(context, UserContactsActivity.class);
        intent.putExtra(CONTACT_LIST, contacts);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ActivityUtils.onActivityCreateSetTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_contacts);
        mToolbar = findViewById(R.id.toolbar);
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        initMenuItems(drawerLayout, mToolbar);

        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.fragment_container);

        if (fragment == null) {
            fragment = UserContactsFragment
                    .newInstance(getIntent().getSerializableExtra(CONTACT_LIST));
            fm.beginTransaction()
                    .add(R.id.fragment_container, fragment)
                    .commit();
        }
    }


    @Override
    public void onPersonItemClick(String userId, String userName) {
        FragmentManager fm = getSupportFragmentManager();
        Fragment personsFragment = PersonsFragment.newInstance(userName, userId);
        fm.beginTransaction()
                .replace(R.id.fragment_container, personsFragment, PERSON_FRAGMENT)
                .addToBackStack(CONTACTS_LIST_FRAGMENT)
                .commit();
    }

    @Override
    public void onBackPressed() {
        FragmentManager fm = getSupportFragmentManager();
        if (fm.findFragmentByTag(PERSON_FRAGMENT) != null) {
            fm.popBackStack(CONTACTS_LIST_FRAGMENT, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            mToolbar.setTitle(getString(R.string.contacts));
        } else {
            super.onBackPressed();
        }
    }
}