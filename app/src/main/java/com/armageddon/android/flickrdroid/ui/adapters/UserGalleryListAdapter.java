package com.armageddon.android.flickrdroid.ui.adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.armageddon.android.flickrdroid.R;
import com.armageddon.android.flickrdroid.common.Converter;
import com.armageddon.android.flickrdroid.model.UserGallery;
import com.armageddon.android.flickrdroid.ui.activities.UserItemDetailActivity;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Shows gallery element from list. Preview icons contains 3 photo like on Flickr.com wev version
 * (1 big square from the left and 2 small one over another).
 * If one of the photo doesn't exist in preview this photo will have grey fill.
 * On clicks starts UserItemDetailActivity.class (Type USER_GALLERY)
 */

public class UserGalleryListAdapter
        extends RecyclerView.Adapter <UserGalleryListAdapter.UserGalleryHolder> {

    private final List<UserGallery> mUserGalleryList = new ArrayList<>();

    @NonNull
    @Override
    public UserGalleryHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.user_gallery_list_item, parent, false);
        return new UserGalleryHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserGalleryHolder holder, int position) {
        holder.onBind(mUserGalleryList.get(position));
    }

    @Override
    public int getItemCount() {
        return mUserGalleryList.size();
    }

    public void setList(List<UserGallery> newUserGalleryList) {
        mUserGalleryList.addAll(newUserGalleryList);
        notifyItemInserted(mUserGalleryList.size() - newUserGalleryList.size());
    }


    static class UserGalleryHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener, Converter {
        ImageView mPhoto1;
        ImageView mPhoto2;
        ImageView mPhoto3;
        TextView mName;
        TextView mPhotosCount;
        TextView mViewsCount;
        TextView mCommentsCount;
        TextView mDate;
        UserGallery mUserGalleryItem;


        public UserGalleryHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);

            mName = itemView.findViewById(R.id.gallery_name);
            mPhoto1 = itemView.findViewById(R.id.gallery_icon_1);
            mPhoto2 = itemView.findViewById(R.id.gallery_icon_2);
            mPhoto3 = itemView.findViewById(R.id.gallery_icon_3);
            mPhotosCount = itemView.findViewById(R.id.gallery_photo_count);
            mViewsCount = itemView.findViewById(R.id.gallery_views_count);
            mCommentsCount = itemView.findViewById(R.id.gallery_comments_count);
            mDate = itemView.findViewById(R.id.gallery_date);

        }

        public void onBind(UserGallery userGalleryItem) {
            mUserGalleryItem = userGalleryItem;

            Glide.with(UserGalleryHolder.this.itemView)
                    .load(userGalleryItem.getCoverPhotos(0))
                    .error(R.color.colorGreyLight)
                    .into(mPhoto1);
            Glide.with(UserGalleryHolder.this.itemView)
                    .load(userGalleryItem.getCoverPhotos(1))
                    .error(R.color.colorGreyLight)
                    .into(mPhoto2);
            Glide.with(UserGalleryHolder.this.itemView)
                    .load(userGalleryItem.getCoverPhotos(2))
                    .error(R.color.colorGreyLight)
                    .into(mPhoto3);


            mName.setText(userGalleryItem.getTitle());
            mPhotosCount.setText(userGalleryItem.getCountPhotos());
            mViewsCount.setText(userGalleryItem.getCountViews());
            mCommentsCount.setText(userGalleryItem.getCountComments());
            mDate.setText(userGalleryItem.getDate_create());
        }

        @Override
        public void onClick(View v) {
           Intent userGalleryIntent = UserItemDetailActivity.newIntent(
                   itemView.getContext(),
                   mUserGalleryItem.getOwner(),
                   mUserGalleryItem.getGalleryId(),
                   mUserGalleryItem.getTitle(),
                   mUserGalleryItem.getCoverPhotos(0),
                   mUserGalleryItem.getDescription(),
                    null,
                   mUserGalleryItem.getCountPhotos(),
                   mUserGalleryItem.getCountViews(),
                   mUserGalleryItem.getCountComments(),
                   UserItemDetailActivity.AdapterTypes.USER_GALLERY);
            itemView.getContext().startActivity(userGalleryIntent);
        }
    }


}
















