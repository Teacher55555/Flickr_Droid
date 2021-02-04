package com.armageddon.android.flickrdroid.ui.adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.armageddon.android.flickrdroid.R;
import com.armageddon.android.flickrdroid.common.LogoIcon;
import com.armageddon.android.flickrdroid.model.Comment;
import com.armageddon.android.flickrdroid.ui.activities.UserPersonalPageActivity;
import com.bumptech.glide.Glide;

import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Controls comments list (show comment, user logo, time of comment)
 * Starts UserPersonalPageActivity when user click on comments icon.
 */

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.CommentViewHolder> {
    List<Comment> mComments;

    public CommentsAdapter(List<Comment> comments) {
        mComments = comments;
        Collections.reverse(mComments);
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new CommentViewHolder(
                LayoutInflater
                        .from(parent.getContext())
                        .inflate(R.layout.comment_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        holder.onBind(mComments.get(position));
    }

    @Override
    public int getItemCount() {
        return mComments.size();
    }

    public void removeComment (int position) {
        mComments.remove(position);
        notifyItemRemoved(position);
    }


   public class CommentViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private Comment mComment;
        private final ImageView mAuthorIcon;
        private final TextView mAuthorName;
        private final TextView mCommentText;
        private final TextView mCommentTimeValue;
        private final TextView mCommentTimeLabel;


        public String getCommentId() {
            return mComment.getId();
        }

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            mAuthorIcon = itemView.findViewById(R.id.author_icon);
            mAuthorName = itemView.findViewById(R.id.author_name);
            mCommentText = itemView.findViewById(R.id.comment_text);
            mCommentTimeValue = itemView.findViewById(R.id.comment_time_value);
            mCommentTimeLabel = itemView.findViewById(R.id.comment_time_label);
        }

        public void onBind(Comment comment) {
            mComment = comment;
            Glide.with(itemView)
                    .load(mComment.getAuthorLogo(LogoIcon.normal_100px))
                    .error(R.drawable.logo)
                    .into(mAuthorIcon);
            mAuthorName.setText(mComment.getAuthorName());
            mCommentText.setText(mComment.getComment());
            mCommentTimeValue.setText(mComment.getCommentTime(itemView.getContext(),Comment.TIME_VALUE));
            mCommentTimeLabel.setText(mComment.getCommentTime(itemView.getContext(),Comment.TIME_NAME));
        }

        @Override
        public void onClick(View v) {
            Intent intent = UserPersonalPageActivity.newIntent(
                    itemView.getContext(),
                    mComment.getAuthorId(),
                    mComment.getAuthorName(),
                    mComment.getAuthorLogo(LogoIcon.huge_300px));
            itemView.getContext().startActivity(intent);
        }
    }
}
