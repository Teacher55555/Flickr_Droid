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
 * Shows album.
 * If album hasn't description - activities ViewPager get only GalleryShowFragment (Album type),
 * otherwise activities ViewPager get 2 Fragments:
 *  - GalleryShowFragment (Album type)
 *  - DescriptionFragment
 *
 */

public class UserAlbumAdapter extends FragmentStatePagerAdapter {
    String userIdAndAlbumId;
    String description;
    Context mContext;

    public UserAlbumAdapter(Context context,
                            @NonNull FragmentManager fm,
                            int behavior, String userIdAndAlbumId, String description) {
        super(fm, behavior);
        this.mContext = context;
        this.userIdAndAlbumId = userIdAndAlbumId;
        this.description = description;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        if (description == null || position == 0) {
            return GalleryShowFragment.newInstance(QueryTypes.USER_ALBUM, null,
                    userIdAndAlbumId);
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
