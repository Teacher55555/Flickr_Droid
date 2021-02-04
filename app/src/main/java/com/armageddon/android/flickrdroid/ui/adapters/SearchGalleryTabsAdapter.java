package com.armageddon.android.flickrdroid.ui.adapters;

import com.armageddon.android.flickrdroid.common.QueryTypes;
import com.armageddon.android.flickrdroid.ui.fragments.GalleryShowFragment;
import com.armageddon.android.flickrdroid.ui.fragments.MapFragment;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

/**
 * adds to activity's ViewPager 3 fragments:
 * 1. GalleryShowFragment with interesting photos (with category filter or date)
 * 2. GalleryShowFragment with recent photos (with category filter)
 * 3. MapFragment
 */

public class SearchGalleryTabsAdapter extends FragmentStatePagerAdapter {
    private final String mQuery;
    private final String mCategory;

    public SearchGalleryTabsAdapter(
            @NonNull FragmentManager fm, int behavior, String category, String query) {
        super(fm, behavior);
        mQuery = query;
        mCategory = category;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            default:
                return GalleryShowFragment.newInstance(QueryTypes.INTERESTINGNESS, mCategory, mQuery);
            case 1:
               return GalleryShowFragment.newInstance(QueryTypes.RECENT, mCategory, null);
            case 2:
               return new MapFragment();

        }
    }

    @Override
    public int getCount() {
        return 3;
    }
}


