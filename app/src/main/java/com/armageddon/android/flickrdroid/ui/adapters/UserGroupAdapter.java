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
 * Adds 2 Fragments to activity's ViewPager:
 * 1 - GalleryShowFragment (Type USER_GROUP)
 * 2 - DescriptionFragment
 */


public class UserGroupAdapter extends FragmentStatePagerAdapter {
    String group_id;
    Context mContext;

    public UserGroupAdapter(Context context, @NonNull FragmentManager fm, int behavior, String group_id) {
        super(fm, behavior);
        this.mContext = context;
        this.group_id = group_id;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        if (position == 1) {
            return DescriptionFragment.newInstance(null, group_id);
        }
        return GalleryShowFragment.newInstance(QueryTypes.USER_GROUP, null, group_id);
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0 : return mContext.getString(R.string.group_photos);
            case 1 : return mContext.getString(R.string.group_description);
        }
        return super.getPageTitle(position);
    }
}
