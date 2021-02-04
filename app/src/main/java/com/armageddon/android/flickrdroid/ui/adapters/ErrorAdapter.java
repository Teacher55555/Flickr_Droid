package com.armageddon.android.flickrdroid.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.armageddon.android.flickrdroid.R;
import com.armageddon.android.flickrdroid.api.RequestResponse;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Contains all kinds of errors.
 * Shows the error text corresponding to the mResponse.getResponseDataStat()
 */

public class ErrorAdapter extends RecyclerView.Adapter<ErrorAdapter.ErrorHolder> {
    public static final String BAD_INTERNET_CONNECTION = "bad_connection";
    private final RequestResponse<?> mResponse;
    private final CallBacks mCallBacks;

    public ErrorAdapter(RequestResponse<?> response, CallBacks callBacks) {
        mResponse = response;
        mCallBacks = callBacks;
    }

    public interface CallBacks {
        void onRefresh();
    }

    @NonNull
    @Override
    public ErrorHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.error_message, parent, false);
        return new ErrorHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ErrorHolder holder, int position) {
        if (mResponse.getConnectionStat() == RequestResponse.CONNECTION_FAIL) {
            holder.onBind(BAD_INTERNET_CONNECTION);
        } else {
            holder.onBind(mResponse.getResponseDataStat());
        }


    }

    @Override
    public int getItemCount() {
        return 1;
    }

    class ErrorHolder extends RecyclerView.ViewHolder {
        TextView mTextView;
        ImageView mImageView;
        Button mButton;

        public ErrorHolder(@NonNull View itemView) {
           super(itemView);
           mTextView = itemView.findViewById(R.id.error_message);
           mImageView = itemView.findViewById(R.id.error_image);
           mButton = itemView.findViewById(R.id.refresh_button);

           mButton.setOnClickListener(v -> mCallBacks.onRefresh());

       }

      void onBind (String errorCode) {
            switch (errorCode) {
                case BAD_INTERNET_CONNECTION:
                    mButton.setVisibility(View.VISIBLE);
                    mImageView.setImageDrawable(ContextCompat.getDrawable(itemView.getContext(),
                            R.drawable.icon_internet_connection));
                    mTextView.setText(itemView.getContext().getString(R.string.internet_connection_error));
                    break;
                case RequestResponse.RESPONSE_BAD_SEARCH:
                    mImageView.setImageDrawable(ContextCompat.getDrawable(itemView.getContext(),
                            R.drawable.icon_smile_bad));
                    mTextView.setText(itemView.getContext().getString(R.string.request_error));
                    break;
                case RequestResponse.RESPONSE_NO_PHOTOS:
                    mImageView.setImageDrawable(ContextCompat.getDrawable(itemView.getContext(),
                            R.drawable.ic_outline_broken_image_24));
                    mTextView.setText(itemView.getContext().getString(R.string.photos_error));
                    break;
                case RequestResponse.RESPONSE_USER_HAS_NO_FOLLOWINGS:
                    mImageView.setImageDrawable(ContextCompat.getDrawable(itemView.getContext(),
                            R.drawable.icon_no_followers_filled));
                    mTextView.setText(itemView.getContext().getString(R.string.user_has_no_followings_error));
                    break;
                case RequestResponse.RESPONSE_USER_HAS_NO_PHOTOS:
                    mImageView.setImageDrawable(ContextCompat.getDrawable(itemView.getContext(),
                            R.drawable.icon_visability_off));
                    mTextView.setText(itemView.getContext().getString(R.string.user_has_no_photos_error));
                    break;
                case RequestResponse.RESPONSE_USER_HAS_NO_ALBUMS:
                    mImageView.setImageDrawable(ContextCompat.getDrawable(itemView.getContext(),
                            R.drawable.icon_visability_off));
                    mTextView.setText(itemView.getContext().getString(R.string.user_has_no_albums_error));
                    break;
                case RequestResponse.RESPONSE_USER_HAS_NO_GALLERIES:
                    mImageView.setImageDrawable(ContextCompat.getDrawable(itemView.getContext(),
                            R.drawable.icon_visability_off));
                    mTextView.setText(itemView.getContext().getString(R.string.user_has_no_galleries_error));
                    break;
                case RequestResponse.RESPONSE_GROUP_HAS_PRIVATE_PHOTO:
                    mImageView.setImageDrawable(ContextCompat.getDrawable(itemView.getContext(),
                            R.drawable.icon_visability_off));
                    mTextView.setText(itemView.getContext().getString(R.string.group_has_private_photos_error));
                    break;
                case RequestResponse.RESPONSE_USER_HAS_NO_GROUPS:
                    mImageView.setImageDrawable(ContextCompat.getDrawable(itemView.getContext(),
                            R.drawable.icon_people_search_stroke));
                    mTextView.setText(itemView.getContext().getString(R.string.user_has_no_groups_error));
                    break;
                case RequestResponse.RESPONSE_NO_COMMENTS:
                    mImageView.setImageDrawable(ContextCompat.getDrawable(itemView.getContext(),
                            R.drawable.icon_no_comments));
                    mTextView.setText(itemView.getContext().getString(R.string.no_comments));
                    break;
                default:
                    mTextView.setText(itemView.getContext().getString(R.string.data_receive_fail));
                    mButton.setVisibility(View.VISIBLE);
            }
      }
   }
}
