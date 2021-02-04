package com.armageddon.android.flickrdroid.ui.adapters;

import android.app.Dialog;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.armageddon.android.flickrdroid.R;
import com.armageddon.android.flickrdroid.api.FlickrFetchr;
import com.armageddon.android.flickrdroid.common.Converter;
import com.armageddon.android.flickrdroid.common.QueryPreferences;
import com.armageddon.android.flickrdroid.common.QueryTypes;
import com.armageddon.android.flickrdroid.model.GalleryItem;
import com.armageddon.android.flickrdroid.api.RequestResponse;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;


/**
 * Shows list of photos in preview. Controls columns quantity (1 - 3).
 * If user logged in, shows and set photo privacy status.
 * Onclick start PhotoFullActivity.class
 */

public class GalleryItemAdapter extends RecyclerView.Adapter <GalleryItemAdapter.GalleryItemHolder>  {


    CallBacks mCallBacks;
    String mQueryType;

    public GalleryItemAdapter (CallBacks callBacks, String queryType) {
        mCallBacks = callBacks;
        mQueryType = queryType;
    }

    public interface CallBacks {
        void onItemClicked (UUID galleryItemId, int position);
    }

    private final List<GalleryItem> mGalleryItemList = new ArrayList<>();


    @NonNull
    @Override
    public GalleryItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.gallery_list_item, parent, false);
        return new GalleryItemHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GalleryItemHolder holder, int position) {
          holder.onBind(position);
    }

    @Override
    public int getItemCount() {
        return mGalleryItemList.size();
    }

    public void setList(List<GalleryItem> galleryItemList) {
        mGalleryItemList.addAll(galleryItemList);
        notifyItemInserted(mGalleryItemList.size() - galleryItemList.size());
    }

    public void clearItems () {
        mGalleryItemList.clear();
        notifyDataSetChanged();
    }


    class GalleryItemHolder extends RecyclerView.ViewHolder implements
            View.OnClickListener, Converter {
       private GalleryItem mGalleryItem;
       private final ImageView mImageView;
       private int mPosition;
       private String getString (int resId) { return itemView.getResources().getString(resId); }
       private void setPrivacyImage (ImageView statusImage, int privacy) {
            switch (privacy) {
                case 0 : statusImage.setImageDrawable(
                        ContextCompat.getDrawable(
                                itemView.getContext(),R.drawable.photo_status_public));
                        break;
                case 1 : statusImage.setImageDrawable(
                        ContextCompat.getDrawable(
                                itemView.getContext(),R.drawable.photo_status_lock));
                        break;
                case 2 : statusImage.setImageDrawable(
                        ContextCompat.getDrawable(
                                itemView.getContext(),R.drawable.photo_status_friends));
                        break;
                case 3 : statusImage.setImageDrawable(
                        ContextCompat.getDrawable(
                                itemView.getContext(),R.drawable.photo_status_family));
                        break;
                case 4 : statusImage.setImageDrawable(
                        ContextCompat.getDrawable(
                                itemView.getContext(),R.drawable.photo_status_friends_and_family));
                        break;
            }
        }

        private class SetPrivacyTask extends AsyncTask<String, Void, RequestResponse<?>> {

            private final String mPhotoId;
            private final int mPrivacy;

            public SetPrivacyTask(String photoId, int privacy) {
                this.mPhotoId = photoId;
                this.mPrivacy = privacy;
            }

            @Override
            protected RequestResponse<?> doInBackground(String... strings) {
                return new FlickrFetchr().setPhotoPrivacy(itemView.getContext(), mPhotoId, mPrivacy);
            }

            @Override
            protected void onPostExecute(RequestResponse<?> response) {
                if (response.getConnectionStat() == RequestResponse.CONNECTION_OK
                        && response.getResponseDataStat().equals(RequestResponse.RESPONSE_DATA_OK)) {
                    mGalleryItem.setPrivacy(mPrivacy);
                } else {
                    Toast.makeText(mImageView.getContext(),
                            mImageView.getContext().getString(R.string.internet_connection_error),
                            Toast.LENGTH_SHORT)
                            .show();
                    setPrivacyImage(itemView.findViewById(R.id.photo_status), mGalleryItem.getPrivacy());
                }
            }

        }

        public GalleryItemHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);


            FrameLayout frameLayout = itemView.findViewById(R.id.frameLayout);
            mImageView = itemView.findViewById(R.id.photo_view);

            // sets columns quantity
            float density = Objects.requireNonNull(itemView.getContext())
                    .getResources().getDisplayMetrics().density;
            int columns  = QueryPreferences.getGalleryViewColumns(itemView.getContext());
            frameLayout.setLayoutParams(pxToDp(frameLayout, density, columns));

        }

        public void onBind(int position) {
            mGalleryItem = mGalleryItemList.get(position);
            mPosition = position;


            String url = null;
            switch (QueryPreferences.getGalleryViewColumns(itemView.getContext())) {
                case 1 : url = mGalleryItem.getUrl_z(); break; // 1 column - highest resolution
                case 2 : url = mGalleryItem.getUrl_m(); break; // 2 columns - middle resolution
                case 3 : url = mGalleryItem.getUrl_s(); break; // 3 columns - low resolution
            }
            if (url == null) {
                url = mGalleryItem.getPhotoHighestResUrl();
            }

            Glide.with(GalleryItemHolder.this.itemView)
                    .load(url)
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.ic_outline_broken_image_24)
                    .into(mImageView);

            // if user logged in shows photo privacy status
            if (QueryTypes.CAMERA_ROLL.equals(mQueryType)) {
                ImageView statusImage = itemView.findViewById(R.id.photo_status);
                setPrivacyImage(statusImage, mGalleryItem.getPrivacy());
                statusImage.setVisibility(View.VISIBLE);
                statusImage.setOnClickListener(v -> {
                    ArrayList<String> statusList = new ArrayList<>();
                    Collections.addAll(statusList,
                            getString(R.string.photo_privacy_public),
                            getString(R.string.photo_privacy_private),
                            getString(R.string.photo_privacy_friends),
                            getString(R.string.photo_privacy_family),
                            getString(R.string.photo_privacy_family_friends));

                    LayoutInflater layoutInflater = LayoutInflater.from(itemView.getContext());
                    ListView dialogView = (ListView) layoutInflater.inflate(R.layout.list_view,
                            null);

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            itemView.getContext(),
                            R.layout.dialog_photo_status_list_item,
                            statusList);
                    dialogView.setAdapter(adapter);

                    final AlertDialog.Builder dialog =
                            new AlertDialog.Builder(itemView.getContext());
                    dialog.setView(dialogView);
                    dialogView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
                    dialogView.setItemChecked(mGalleryItem.getPrivacy(), true);

                    final Dialog privacyDialog = dialog.create();
                    privacyDialog.show();

                    //sets privacy status via dialog view
                    dialogView.setOnItemClickListener((parent, view, position1, id) -> {
                        setPrivacyImage(statusImage, position1);
                        privacyDialog.cancel();
                        new SetPrivacyTask(mGalleryItem.getId(), position1).execute();
                    });
                });
            }
    }


        @Override
        public void onClick(View v) {
            mCallBacks.onItemClicked(mGalleryItem.getItemId(), mPosition);
        }
    }


}
















