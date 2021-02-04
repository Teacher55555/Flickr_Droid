package com.armageddon.android.flickrdroid.ui.adapters;

import com.armageddon.android.flickrdroid.common.QueryTypes;
import com.armageddon.android.flickrdroid.ui.fragments.BlankFragment;
import com.armageddon.android.flickrdroid.ui.fragments.PersonsFragment;
import com.armageddon.android.flickrdroid.ui.fragments.GalleryShowFragment;
import com.armageddon.android.flickrdroid.ui.fragments.UserGroupFragment;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

/**
 * adds to activity's ViewPager 3 fragments:
 * 1. GalleryShowFragment with requested photos (mQuery)
 * 2. PersonsFragment - user search by id or e-mail
 * 3. GroupFragment - group search.
 */

public class SearchTabsAdapter extends FragmentStatePagerAdapter {
    private final String mQuery;
    public SearchTabsAdapter(@NonNull FragmentManager fm, int behavior, String query) {
        super(fm, behavior);
        mQuery = query;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        if (mQuery == null) {
            return new BlankFragment();
        }
        switch (position) {
            default: return GalleryShowFragment.newInstance(QueryTypes.PHOTO,null, mQuery);
            case 1: return PersonsFragment.newInstance(mQuery, null);
            case 2: return UserGroupFragment.newInstance(mQuery, null);
        }
    }

    @Override
    public int getCount() {
        return 3;
    }


}
