package com.armageddon.android.flickrdroid.ui.fragments;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Toast;

import com.armageddon.android.flickrdroid.R;
import com.armageddon.android.flickrdroid.api.FlickrFetchr;
import com.armageddon.android.flickrdroid.common.Converter;
import com.armageddon.android.flickrdroid.common.GalleryItemBase;
import com.armageddon.android.flickrdroid.common.QueryPreferences;
import com.armageddon.android.flickrdroid.model.Comment;
import com.armageddon.android.flickrdroid.model.GalleryItem;
import com.armageddon.android.flickrdroid.api.RequestResponse;
import com.armageddon.android.flickrdroid.ui.adapters.CommentsAdapter;
import com.armageddon.android.flickrdroid.ui.adapters.ErrorAdapter;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

/**
 * Shows comments list via vertical RecycleView.
 * Allows write/delete own comment if user logged in.
 */

public class CommentsFragment extends Fragment implements Converter, ErrorAdapter.CallBacks {
    private static final String PHOTO_ID = "photoID";
    private String photoID;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerView;
    private RequestResponse<Comment> mResponse;
    private FetchItemsTask mFetchItemsTask;
    private FrameLayout mMessageFrame;
    private EditText mTextField;
    private final Paint mItemSwipeBackgroundColor = new Paint();

    public static CommentsFragment newInstance(String photoId) {
        Bundle args = new Bundle();
        args.putString(PHOTO_ID, photoId);
        CommentsFragment fragment = new CommentsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        assert getArguments() != null;
        photoID = getArguments().getString(PHOTO_ID);
        updateItems();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v =  inflater.inflate(R.layout.fragment_photo_comments, container, false);
        // if user swipes from left to right, it will be red background behind a comment
        mItemSwipeBackgroundColor.setColor(getResources().getColor(R.color.colorDelRed, null));
        mMessageFrame = v.findViewById(R.id.message_frame);
        mSwipeRefreshLayout = v.findViewById(R.id.swipe_layout);
        mRecyclerView = v.findViewById(R.id.recycle_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mSwipeRefreshLayout.setColorSchemeColors(getAttrColor(requireActivity(),R.attr.colorAccent));


        String userId = QueryPreferences.getUserId(getActivity());
        if (userId != null) {
            ItemTouchHelper itemTouchHelper = new ItemTouchHelper(touchHelper);
            itemTouchHelper.attachToRecyclerView(mRecyclerView);
        }
            mSwipeRefreshLayout.setOnRefreshListener(() -> {
            mRecyclerView.setAdapter(new CommentsAdapter(new ArrayList<>()));
            updateItems();
        });
        return v;
    }

    /**
     * if the user is logged in, then UI for entering and sending messages is visible.
     */
    private void mOauthUserUiCheck () {
        if (QueryPreferences.getUserId(getActivity()) != null) {
            mMessageFrame.setVisibility(View.VISIBLE);
            FrameLayout.LayoutParams params =
                    (FrameLayout.LayoutParams) mSwipeRefreshLayout.getLayoutParams();
            float scale = getResources().getDisplayMetrics().density;
            params.setMargins(0,0,0,dpToPx(scale,50));
            mTextField = getView().findViewById(R.id.message_field);
            ImageButton sendButton = getView().findViewById(R.id.message_send_button);
            sendButton.setOnClickListener(
                    v -> {
                        Context context = getContext();
                        if (mTextField.getText().length() > 0 && context != null) {
                            InputMethodManager imm = (InputMethodManager) getActivity().
                                    getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(mTextField.getWindowToken(), 0);
                            mSwipeRefreshLayout.setRefreshing(true);
                            new PostCommentTask(mTextField.getText().toString(), context).execute();
                            mTextField.getText().clear();
                            mTextField.clearFocus();
                        }
                    });
        }
    }

    @Override
    public void onResume() {
        mOauthUserUiCheck();
        super.onResume();

        if (mFetchItemsTask.getStatus() == AsyncTask.Status.RUNNING) {
            mSwipeRefreshLayout.setRefreshing(true);
        }
    }


    private void updateItems() {
        Context context = getContext();
        if (context != null) {
            mFetchItemsTask = new FetchItemsTask(photoID, context);
            mFetchItemsTask.execute();
        }
    }

    @Override
    public void onRefresh() {
        mSwipeRefreshLayout.setRefreshing(true);
        mRecyclerView.setAdapter(new CommentsAdapter(new ArrayList<>()));
        updateItems();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    /** Gets comments from Flick.com */

    private class FetchItemsTask extends AsyncTask<String, Void, RequestResponse<Comment>> {
        private final String mQuery;
        private Context mContext;

        public FetchItemsTask(String query, Context context) {
            mQuery = query;
            mContext = context;
        }

        @Override
        protected RequestResponse<Comment> doInBackground(String... strings) {
                return new FlickrFetchr().fetchCommentsList(mContext, mQuery);
        }

        @Override
        protected void onPostExecute(RequestResponse<Comment> response) {
            mResponse = response;
            if (mResponse.getConnectionStat() == RequestResponse.CONNECTION_OK
                    && mResponse.getResponseDataStat().equals(RequestResponse.RESPONSE_DATA_OK)
                    && isAdded()) {
               mRecyclerView.setAdapter(new CommentsAdapter(mResponse.getItems()));
            } else {
                mRecyclerView.setAdapter(new ErrorAdapter(response, CommentsFragment.this));
            }
            mSwipeRefreshLayout.setRefreshing(false);
        }
    }

    /** Send comments to Flick.com */
    private  class PostCommentTask extends AsyncTask<String, Void, RequestResponse<?>> {

        private final String mQuery;
        private Context context;

        public PostCommentTask(String message, Context context) {
            mQuery = message;
            this.context = context;
        }

        @Override
        protected RequestResponse<?> doInBackground(String... strings) {
            return new FlickrFetchr().postComment(context, photoID, mQuery);
        }

        @Override
        protected void onPostExecute(RequestResponse<?> response) {
            if (response.getConnectionStat() == RequestResponse.CONNECTION_OK
                    && response.getResponseDataStat().equals(RequestResponse.RESPONSE_DATA_OK)) {
                for (GalleryItem item : GalleryItemBase.getInstance().getResponse(getActivity()).getItems()) {
//                for (GalleryItem item : GalleryItemBase.getResponse(getActivity()).getItems()) {
                    if (item.getId().equals(photoID)){
                        item.increaseCommentsCount();
                        break;
                    }
                }
                new FetchItemsTask(photoID, context).execute();

            } else {
                mSwipeRefreshLayout.setRefreshing(false);
                Toast.makeText(getActivity(),
                        getString(R.string.internet_connection_error),
                        Toast.LENGTH_SHORT)
                            .show();
            }
        }
    }

    /**
     * Draws rectangle when swiping item from right to left.
     * Starts DelCommentTask if user swiped to the end.
     */

    private final ItemTouchHelper.Callback touchHelper =
        new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
        @Override
        public int getSwipeDirs(@NonNull RecyclerView recyclerView,
                                @NonNull RecyclerView.ViewHolder viewHolder) {
            String commentAuthorId =
                    mResponse.getItems().get(viewHolder.getAdapterPosition()).getAuthorId();
            if (!commentAuthorId.equals(QueryPreferences.getUserId(getActivity()))) return 0;
            return super.getSwipeDirs(recyclerView, viewHolder);
        }

    @Override
        public boolean onMove(@NonNull RecyclerView recyclerView,
                              @NonNull RecyclerView.ViewHolder viewHolder,
                              @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSelectedChanged(@Nullable RecyclerView.ViewHolder viewHolder, int actionState) {
            super.onSelectedChanged(viewHolder, actionState);
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            Comment comment = mResponse.getItems().get(viewHolder.getAdapterPosition());
            Context context = getContext();
            if (comment.getAuthorId().equals(QueryPreferences.getUserId(getActivity()))
                && context != null) {
                ((CommentsAdapter) mRecyclerView.getAdapter())
                        .removeComment(viewHolder.getAdapterPosition());
                String commentId = ((CommentsAdapter.CommentViewHolder) viewHolder).getCommentId();
                new DelCommentTask(commentId, context).execute();
            }
        }

        @Override
        public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                                @NonNull RecyclerView.ViewHolder viewHolder,
                                float dX, float dY, int actionState, boolean isCurrentlyActive) {
            if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                View itemView = viewHolder.itemView;
                    c.drawRect((float) itemView.getRight() + dX,
                            (float) itemView.getTop(),
                            (float) itemView.getRight(),
                            (float) itemView.getBottom(),
                            mItemSwipeBackgroundColor);
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        }
    };

    /**
     * Deletes comment.
     */
    private class DelCommentTask extends AsyncTask<String, Void, RequestResponse<?>> {

        private final String mQuery;
        private final Context context;

        public DelCommentTask(String commentId, Context context) {
            mQuery = commentId;
            this.context = context;
        }

        @Override
        protected RequestResponse<?> doInBackground(String... strings) {
            return new FlickrFetchr().delComment(getActivity(), mQuery);
        }

        @Override
        protected void onPostExecute(RequestResponse<?> response) {
              if (response.getConnectionStat() != RequestResponse.CONNECTION_OK) {
                  Toast.makeText(getActivity(),
                        getString(R.string.internet_connection_error),
                        Toast.LENGTH_SHORT)
                        .show();
                new FetchItemsTask(photoID, context).execute();
              } else if (!response.getResponseDataStat().equals(RequestResponse.RESPONSE_DATA_OK)) {
                Toast.makeText(getActivity(),
                        getString(R.string.comment_delete_error),
                        Toast.LENGTH_SHORT)
                        .show();
                new FetchItemsTask(photoID, context).execute();
            } else {
                  for (GalleryItem item : GalleryItemBase.getInstance().getResponse(getActivity()).getItems()) {
//                for (GalleryItem item : GalleryItemBase.getResponse(getActivity()).getItems()) {
                      if (item.getId().equals(photoID)){
                          item.decreaseCommentsCount();
                          break;
                      }
                  }
              }
        }
    }

}
