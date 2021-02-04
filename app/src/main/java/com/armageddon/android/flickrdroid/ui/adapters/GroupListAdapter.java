package com.armageddon.android.flickrdroid.ui.adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.armageddon.android.flickrdroid.R;
import com.armageddon.android.flickrdroid.common.Converter;
import com.armageddon.android.flickrdroid.model.GroupSearchItem;
import com.armageddon.android.flickrdroid.ui.activities.UserItemDetailActivity;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Shows information about group from list
 * Onclick start UserItemDetailActivity.class
 */

public class GroupListAdapter extends RecyclerView.Adapter <GroupListAdapter.GroupItemHolder> {

    private final List<GroupSearchItem> mGroupSearchItemList = new ArrayList<>();

    @NonNull
    @Override
    public GroupItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.group_list_item, parent, false);
        return new GroupItemHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupItemHolder holder, int position) {
        holder.onBind(mGroupSearchItemList.get(position));
    }

    @Override
    public int getItemCount() {
        return mGroupSearchItemList.size();
    }

    public void setList(List<GroupSearchItem> newGroupSearchItemList) {
        mGroupSearchItemList.addAll(newGroupSearchItemList);
        notifyItemInserted(mGroupSearchItemList.size() - newGroupSearchItemList.size());
    }

    static class GroupItemHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener, Converter {
        ImageView mIcon;
        TextView mName;
        TextView mMembersCount;
        TextView mPhotosCount;
        TextView mTopicsCount;
        TextView mGroupStatus;
        LinearLayout mTopicsLayout;
        GroupSearchItem mGroupSearchItem;


        public GroupItemHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);

            mName = itemView.findViewById(R.id.group_name);
            mIcon = itemView.findViewById(R.id.group_icon);
            mPhotosCount = itemView.findViewById(R.id.group_pool_count);
            mMembersCount = itemView.findViewById(R.id.group_members_count);
            mTopicsCount = itemView.findViewById(R.id.group_topic_count);
            mGroupStatus = itemView.findViewById(R.id.group_status);
            mTopicsLayout = itemView.findViewById(R.id.group_topics_layout);

        }

        public void onBind(GroupSearchItem groupSearchItemItem) {
            mGroupSearchItem = groupSearchItemItem;

            Glide.with(GroupItemHolder.this.itemView)
                    .load(groupSearchItemItem.getIcon())
                    .error(R.drawable.logo)
                    .into(mIcon);
            mName.setText(groupSearchItemItem.getName());
            mMembersCount.setText(groupSearchItemItem.getMembers());
            mPhotosCount.setText(groupSearchItemItem.getPool_count());
            if (groupSearchItemItem.getTopic_count() == null) {
                mTopicsLayout.setVisibility(View.GONE);
            } else {
                mTopicsCount.setText(groupSearchItemItem.getTopic_count());
            }


            switch (groupSearchItemItem.getPrivacy()) {
                case GroupSearchItem.PRIVACY_PRIVATE :
                    mGroupStatus.setText(itemView.getContext()
                            .getString(R.string.group_list_private));
                    mGroupStatus.setBackgroundColor(itemView.getResources()
                            .getColor(R.color.colorPinkFlickr, null));
                    break;
                case GroupSearchItem.PRIVACY_INVITE :
                    mGroupStatus.setText(itemView.getContext()
                            .getString(R.string.group_list_invite));
                    mGroupStatus.setBackgroundColor(itemView.getResources()
                            .getColor(R.color.colorBlueFlickr, null));
                    break;
                case GroupSearchItem.PRIVACY_PUBLIC :
                    mGroupStatus.setText(itemView.getContext()
                            .getString(R.string.group_list_public));
                    mGroupStatus.setBackgroundColor(itemView.getResources()
                            .getColor(R.color.colorGreen, null));
                    break;
                default: mGroupStatus.setVisibility(View.GONE);

            }
        }

        @Override
        public void onClick(View v) {
            if (mGroupSearchItem.getPrivacy() == GroupSearchItem.PRIVACY_PRIVATE) {
                Toast.makeText(itemView.getContext(),
                        itemView.getContext().getString(R.string.private_message),
                        Toast.LENGTH_SHORT)
                        .show();
            } else {
                Intent intent = UserItemDetailActivity.newIntent(
                        itemView.getContext(),
                        null,
                        mGroupSearchItem.getNsid(),
                        mGroupSearchItem.getName(),
                        mGroupSearchItem.getIcon(),
                        null,
                        mGroupSearchItem.getMembers(),
                        mGroupSearchItem.getPool_count(),
                        null,
                        null,
                        UserItemDetailActivity.AdapterTypes.USER_GROUP);
                itemView.getContext().startActivity(intent);
            }
        }
    }

}
















