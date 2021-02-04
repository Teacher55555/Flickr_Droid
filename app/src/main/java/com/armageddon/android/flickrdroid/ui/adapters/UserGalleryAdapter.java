package com.armageddon.android.flickrdroid.ui.adapters;

import android.content.Context;

import com.armageddon.android.flickrdroid.R;
import com.armageddon.android.flickrdroid.common.QueryTypes;
import com.armageddon.android.flickrdroid.ui.fragments.DescriptionFragment;
import com.armageddon.android.flickrdroid.ui.fragments.GalleryShowFragment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

/**
 * Shows gallery.
 * If gallery hasn't description - activities ViewPager get only GalleryShowFragment (Gallery type),
 * otherwise activities ViewPager get 2 Fragments:
 *  - GalleryShowFragment (Gallery type)
 *  - DescriptionFragment
 */

public class UserGalleryAdapter extends FragmentStatePagerAdapter {
    private final String userId;
    private final String description;
    private final Context mContext;

    public UserGalleryAdapter(Context context,
                              @NonNull FragmentManager fm,
                              int behavior, String userId,
                              String description) {
        super(fm, behavior);
        mContext = context;
        this.userId = userId;
        this.description = description;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        if (description == null || position == 0) {
            return GalleryShowFragment.newInstance(QueryTypes.USER_GALLERY, null, userId);
        } else {
            return DescriptionFragment.newInstance(description, null);
        }
    }

    @Override
    public int getCount() {
        return description == null ? 1 : 2;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        if (description == null) {
            return null;
        } else if (position == 0) {
            return mContext.getString(R.string.group_photos);
        } else {
            return mContext.getString(R.string.group_description);
        }
    }
}
