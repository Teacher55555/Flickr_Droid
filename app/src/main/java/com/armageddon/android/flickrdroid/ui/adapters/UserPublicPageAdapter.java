package com.armageddon.android.flickrdroid.ui.adapters;

import android.content.Context;

import com.armageddon.android.flickrdroid.R;
import com.armageddon.android.flickrdroid.common.QueryTypes;
import com.armageddon.android.flickrdroid.ui.fragments.GalleryShowFragment;
import com.armageddon.android.flickrdroid.ui.fragments.UserGroupFragment;
import com.armageddon.android.flickrdroid.ui.fragments.PersonsFragment;
import com.armageddon.android.flickrdroid.ui.fragments.UserAlbumFragment;
import com.armageddon.android.flickrdroid.ui.fragments.UserGalleryFragment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

/**
 * UserPublicPageAdapter - user is NOT logged in.
 * Adds 5 fragments to activity's ViewPager:
 * 1 - GalleryShowFragment (Type PUBLIC_PHOTO) - user's public photos.
 * 2 - UserAlbumFragment - user's albums list.
 * 3 - UserGalleryFragment - user's galleries list.
 * 4 - GroupFragment - user's groups list
 * 5 - PersonsFragment - user's personal page
 */

public class UserPublicPageAdapter extends FragmentStatePagerAdapter {


    private final String mQuery;
    private final String mUserName;
    private final Context mContext;

    public UserPublicPageAdapter(Context context,
                                 @NonNull FragmentManager fm,
                                 int behavior,
                                 String query,
                                 String userName) {
        super(fm, behavior);
        mContext = context;
        mQuery = query;
        mUserName = userName;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {

            switch (position) {
                default:
                    return GalleryShowFragment.newInstance(QueryTypes.PUBLIC_PHOTO,
                            null, mQuery);
                case 1:
                    return UserAlbumFragment.newInstance(mQuery, mUserName);
                case 2:
                    return UserGalleryFragment.newInstance(mQuery);
                case 3:
                    return UserGroupFragment.newInstance(null, mQuery);
                case 4:
                    return PersonsFragment.newInstance(null, mQuery);

            }
    }

    @Override
    public int getCount() {
        return 5;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return mContext.getString(R.string.personal_page_public);
            case 1:
                return mContext.getString(R.string.personal_page_albums);
            case 2:
                return mContext.getString(R.string.personal_page_gallery);
            case 3:
                return mContext.getString(R.string.personal_page_groups);
            case 4:
                return mContext.getString(R.string.personal_page_about);
        }
        return null;
    }
}
