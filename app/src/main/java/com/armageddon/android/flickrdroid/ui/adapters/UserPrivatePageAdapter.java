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
 * UserPrivatePageAdapter - user is logged in.
 * Adds 7 fragments to activity's ViewPager:
 * 1 - GalleryShowFragment (Type CONTACTS_PHOTO) - new photos from user's contacts.
 * 2 - GalleryShowFragment (Type CAMERA_ROLL) - all user's photos.
 * 3 - GalleryShowFragment (Type PUBLIC_PHOTO) - user's public photos.
 * 4 - UserAlbumFragment - user's albums list.
 * 5 - UserGalleryFragment - user's galleries list.
 * 6 - GroupFragment - user's groups list
 * 7 - PersonsFragment - user's personal page
 */

public class UserPrivatePageAdapter extends FragmentStatePagerAdapter {


    private final String mQuery;
    private final String mUserName;
    private final Context mContext;

    public UserPrivatePageAdapter(Context context,
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
                return GalleryShowFragment.newInstance(
                        QueryTypes.CONTACTS_PHOTO, null, null);
            case 1:
                return GalleryShowFragment.newInstance(
                        QueryTypes.CAMERA_ROLL, null, null);
            case 2:
                return GalleryShowFragment.newInstance(
                        QueryTypes.PUBLIC_PHOTO, null, mQuery);
            case 3:
                return UserAlbumFragment.newInstance(
                        mQuery, mUserName);
            case 4:
                return UserGalleryFragment.newInstance(
                        mQuery);
            case 5:
                return UserGroupFragment.newInstance(
                        null, mQuery);
            case 6:
                return PersonsFragment.newInstance(
                        null, mQuery);

        }
    }

    @Override
    public int getCount() {
        return 7;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return mContext.getString(R.string.personal_page_followings);
            case 1:
                return mContext.getString(R.string.personal_page_cameraRoll);
            case 2:
                return mContext.getString(R.string.personal_page_public);
            case 3:
                return mContext.getString(R.string.personal_page_albums);
            case 4:
                return mContext.getString(R.string.personal_page_gallery);
            case 5:
                return mContext.getString(R.string.personal_page_groups);
            case 6:
                return mContext.getString(R.string.personal_page_about);
//
        }
        return null;
    }
}
