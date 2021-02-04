package com.armageddon.android.flickrdroid.ui.adapters;

import com.armageddon.android.flickrdroid.common.QueryTypes;
import com.armageddon.android.flickrdroid.ui.fragments.GalleryShowFragment;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

/**
 * Adds GalleryShowFragment to activities ViewPager (type FAVES)
 */

public class UserFavesAdapter extends FragmentStatePagerAdapter {

    private final String userId;

    public UserFavesAdapter(@NonNull FragmentManager fm, int behavior, String userId) {
        super(fm, behavior);
        this.userId = userId;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        return GalleryShowFragment.newInstance(QueryTypes.USER_FAVES, null, userId);

    }

    @Override
    public int getCount() {
        return 1;
    }
}
