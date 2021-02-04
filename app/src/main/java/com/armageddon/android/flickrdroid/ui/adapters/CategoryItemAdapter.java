package com.armageddon.android.flickrdroid.ui.adapters;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.armageddon.android.flickrdroid.R;
import com.armageddon.android.flickrdroid.common.EnumCategory;
import com.bumptech.glide.Glide;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Controls item animation and send to activity information witch category was selected
 */

public class CategoryItemAdapter extends RecyclerView.Adapter<CategoryItemAdapter.CategoryItemHolder> {
    private static final float ICON_SCALE = 2.0f; //1.5
    private static final float BACKGROUND_SCALE = 1.15f; //1.1

    CallBacks mCallBacks;
    CategoryItemHolder mPreviousHolder;
    String mOnStartNameActiveCategory;

    public interface CallBacks{
        void callCategoryFilter (EnumCategory category);
    }

    public CategoryItemAdapter(CallBacks callBacks, String onStartNameActiveCategory) {
        mCallBacks = callBacks;
        mOnStartNameActiveCategory = onStartNameActiveCategory;
    }


    @NonNull
    @Override
    public CategoryItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.category_list_item, parent, false);
        return new CategoryItemHolder(view);
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryItemHolder holder, int position) {
        holder.onBind(EnumCategory.values()[position]);
    }

    @Override
    public void onViewAttachedToWindow(@NonNull CategoryItemHolder holder) {
        super.onViewAttachedToWindow(holder);
        holder.startAnimation();
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull CategoryItemHolder holder) {
        super.onViewDetachedFromWindow(holder);
    }

    @Override
    public int getItemCount() {
        return EnumCategory.values().length;
    }

     class CategoryItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        EnumCategory mCategory;
        ImageView mImageView;
        ImageView mCategoryBackground;
        TextView mTextView;
        boolean mActive;
        AnimatorSet categoryIconAnimSetDownBase = new AnimatorSet();

         public void setActive(boolean active) {
             mActive = active;
         }

         public void startAnimation () {
             if (mActive) {
                 mCategoryBackground.startAnimation(AnimationUtils
                         .loadAnimation(itemView.getContext(), R.anim.category_back_anim));
             }
         }

         public void stopAnimation () {
             mActive = false;
             mCategoryBackground.clearAnimation();
             mCategoryBackground.setImageDrawable(
                     ContextCompat.getDrawable(
                             itemView.getContext(),R.drawable.category_background));
             categoryIconAnimSetDownBase.start();
         }

         public CategoryItemHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            mImageView = itemView.findViewById(R.id.category_button);
            mTextView = itemView.findViewById(R.id.text_category_view);
            mCategoryBackground = itemView.findViewById(R.id.category_back);

        }

        public void onBind(EnumCategory enumCategory) {
            if (mOnStartNameActiveCategory != null
                    && mOnStartNameActiveCategory.equals(enumCategory.name())) {
                mOnStartNameActiveCategory = null;
                animateCategory();
            }
            mCategory = enumCategory;
            Glide.with(CategoryItemHolder.this.itemView)
                    .load(enumCategory.getDrawble())
                    .into(mImageView);

            mTextView.setText(enumCategory.name());
        }

         @Override
         public void onClick(View v) {
             if (mPreviousHolder != null) {
                 mPreviousHolder.stopAnimation();
             }
             animateCategory();
             mCallBacks.callCategoryFilter(mCategory);
         }

         private void animateCategory () {


             mPreviousHolder = CategoryItemHolder.this;
             setActive(true);

             mCategoryBackground.setImageDrawable(
                     ContextCompat.getDrawable(
                             itemView.getContext(),R.drawable.category_dashed_border));
             mCategoryBackground.startAnimation(AnimationUtils
                     .loadAnimation(itemView.getContext(),R.anim.category_back_anim));

             ObjectAnimator categoryAnimScaleYUp = ObjectAnimator
                     .ofFloat(mImageView, "scaleY", mImageView.getScaleY(), ICON_SCALE)
                     .setDuration(200);

             ObjectAnimator categoryAnimScaleXUp = ObjectAnimator
                     .ofFloat(mImageView, "scaleX", mImageView.getScaleX(), ICON_SCALE)
                     .setDuration(200);

             ObjectAnimator categoryAnimScaleYDown = ObjectAnimator
                     .ofFloat(mImageView, "scaleY", ICON_SCALE, 1)
                     .setDuration(200);

             ObjectAnimator categoryAnimScaleXDown = ObjectAnimator
                     .ofFloat(mImageView, "scaleX", ICON_SCALE, 1)
                     .setDuration(200);

             ObjectAnimator categoryBackAnimScaleYUp = ObjectAnimator
                     .ofFloat(mCategoryBackground, "scaleY",
                             mCategoryBackground.getScaleY(), BACKGROUND_SCALE)
                     .setDuration(200);

             ObjectAnimator categoryBackAnimScaleXUp = ObjectAnimator
                     .ofFloat(mCategoryBackground, "scaleX",
                             mCategoryBackground.getScaleX(), BACKGROUND_SCALE)
                     .setDuration(200);

             ObjectAnimator categoryBackAnimScaleYDown = ObjectAnimator
                     .ofFloat(mCategoryBackground, "scaleY",
                             BACKGROUND_SCALE, 1.05f)
                     .setDuration(200);

             ObjectAnimator categoryBackAnimScaleXDown = ObjectAnimator
                     .ofFloat(mCategoryBackground, "scaleX",
                             BACKGROUND_SCALE, 1.05f)
                     .setDuration(200);

             AnimatorSet categoryIconAnimSetDown = new AnimatorSet();
             categoryIconAnimSetDown
                     .play(categoryAnimScaleYDown)
                     .with(categoryAnimScaleXDown)
                     .with(categoryBackAnimScaleYDown)
                     .with(categoryBackAnimScaleXDown);

             AnimatorSet categoryIconAnimSetUp = new AnimatorSet();
             categoryIconAnimSetUp
                     .play(categoryAnimScaleYUp)
                     .with(categoryAnimScaleXUp)
                     .with(categoryBackAnimScaleXUp)
                     .with(categoryBackAnimScaleYUp)
                     .before(categoryIconAnimSetDown);
             categoryIconAnimSetUp.start();

             ObjectAnimator categoryBackAnimScaleYDownBase = ObjectAnimator
                     .ofFloat(mPreviousHolder.mCategoryBackground, "scaleY",
                             1.05f, 1)
                     .setDuration(200);

             ObjectAnimator categoryBackAnimScaleXDownBase = ObjectAnimator
                     .ofFloat(mPreviousHolder.mCategoryBackground, "scaleX",
                             1.05f, 1)
                     .setDuration(200);

             categoryIconAnimSetDownBase = new AnimatorSet();
             categoryIconAnimSetDownBase
                     .play(categoryBackAnimScaleYDownBase)
                     .with(categoryBackAnimScaleXDownBase);
         }
     }
}


