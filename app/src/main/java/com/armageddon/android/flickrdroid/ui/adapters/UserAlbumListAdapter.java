package com.armageddon.android.flickrdroid.ui.adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.armageddon.android.flickrdroid.R;
import com.armageddon.android.flickrdroid.common.Converter;
import com.armageddon.android.flickrdroid.model.Album;
import com.armageddon.android.flickrdroid.ui.activities.UserItemDetailActivity;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Shows album element from list. Flickr calls "ALBUM" user's FAVORITES pictures too.
 * So if mAlbumItem.getDateCreate() == null => this is FAVORITES, else => simple ALBUM
 */


public class UserAlbumListAdapter
        extends RecyclerView.Adapter <UserAlbumListAdapter.AlbumItemHolder> {


    private final List<Album> mAlbumList = new ArrayList<>();
    private String mUserName;

    public UserAlbumListAdapter(String userName) {
        mUserName = userName;
    }


    @NonNull
    @Override
    public AlbumItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.album_list_item, parent, false);
        return new AlbumItemHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlbumItemHolder holder, int position) {
        holder.onBind(mAlbumList.get(position));
    }



    @Override
    public int getItemCount() {
        return mAlbumList.size();
    }

    public void setList(List<Album> newAlbunList) {
        mAlbumList.addAll(newAlbunList);
        notifyItemInserted(mAlbumList.size() - newAlbunList.size());
    }


     static class AlbumItemHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener, Converter {
       private final ImageView mIcon;
       private final TextView mName;
       private final TextView mViewsCount;
       private final TextView mPhotosCount;
       private final TextView mDateCreated;
       private final LinearLayout mViewsLayout;
       private final LinearLayout mDateLayout;
       private Album mAlbumItem;


        public AlbumItemHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            mName = itemView.findViewById(R.id.album_name);
            mIcon = itemView.findViewById(R.id.album_icon);
            mPhotosCount = itemView.findViewById(R.id.album_photo_count);
            mViewsCount = itemView.findViewById(R.id.album_views_count);
            mDateCreated = itemView.findViewById(R.id.album_date);
            mViewsLayout = itemView.findViewById(R.id.album_views_layout);
            mDateLayout = itemView.findViewById(R.id.album_date_layout);
        }

        public void onBind(Album albumItem) {
            mAlbumItem = albumItem;

            Glide.with(AlbumItemHolder.this.itemView)
                    .load(albumItem.getCoverPhotoUrl())
                    .error(R.drawable.logo)
                    .into(mIcon);
            mName.setText(albumItem.getTitle());
            mPhotosCount.setText(albumItem.getCountPhotos());

            if (albumItem.getDateCreate() == null) {
                mViewsLayout.setVisibility(View.GONE);
                mDateLayout.setVisibility(View.GONE);
            } else {
                mViewsLayout.setVisibility(View.VISIBLE);
                mDateLayout.setVisibility(View.VISIBLE);
                mViewsCount.setText(albumItem.getCountViews());
                mDateCreated.setText(albumItem.getDateCreate());
            }
        }

        @Override
        public void onClick(View v) {
            Intent userAlbumIntent;

            if (mAlbumItem.getDateCreate() == null) {
                userAlbumIntent = UserItemDetailActivity.newIntent(
                        itemView.getContext(),
                        mAlbumItem.getOwner(),
                        mAlbumItem.getOwner(),
                        mAlbumItem.getTitle(),
                        mAlbumItem.getCoverPhotoUrl(),
                        mAlbumItem.getDescription(),
                        null,
                        mAlbumItem.getCountPhotos(),
                        null,
                        null,
                        UserItemDetailActivity.AdapterTypes.USER_FAVES);
            } else {
                userAlbumIntent = UserItemDetailActivity.newIntent(
                        itemView.getContext(),
                        mAlbumItem.getOwner(),
                        mAlbumItem.getOwner() + " " + mAlbumItem.getId(),
                        mAlbumItem.getTitle(),
                        mAlbumItem.getCoverPhotoUrl(),
                        mAlbumItem.getDescription(),
                        null,
                        mAlbumItem.getCountPhotos(),
                        mAlbumItem.getCountViews(),
                        null,
                        UserItemDetailActivity.AdapterTypes.USER_ALBUM);
            }

            itemView.getContext().startActivity(userAlbumIntent);
        }
    }
}
















