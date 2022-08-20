package com.armageddon.android.flickrdroid.ui.adapters

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.armageddon.android.flickrdroid.R
import com.bumptech.glide.Glide

private const val ICON_SCALE = 3.0f //1.5
private const val BACKGROUND_SCALE = 1.15f //1.1

class CategoryItemAdapter (
    private val mCallBacks: CallBacks,
) : RecyclerView.Adapter<CategoryItemAdapter.CategoryItemHolder>() {
    private val categoryList = CategoryFilter.values().toMutableList()

    var mPreviousHolder: CategoryItemHolder? = null

    interface CallBacks {
        fun callCategoryFilter(title: String?)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryItemHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.category_list_item, parent, false)

        return CategoryItemHolder(view)
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun onBindViewHolder(holder: CategoryItemHolder, position: Int) {
        holder.onBind(categoryList[position])
    }

    override fun onViewAttachedToWindow(holder: CategoryItemHolder) {
        super.onViewAttachedToWindow(holder)
        holder.onAttach()
    }

    override fun getItemCount(): Int {
        return categoryList.size
    }

    fun getTitle () : String? {
        return categoryList.firstOrNull { it.isActiv }?.name
    }

    fun clearFilter () {
        this.mPreviousHolder?.stopAnimation()
        this.categoryList.forEach { it.isActiv = false }
        mCallBacks.callCategoryFilter(null)
    }

    inner class CategoryItemHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView), View.OnClickListener {
        private var mCategoryHolder: CategoryFilter? = null
        private var mImageView: ImageView
        private var mCategoryBackground: ImageView
        private var mTextView: TextView
        private var categoryIconAnimSetDownBase = AnimatorSet()
        private var mPosition = layoutPosition

        fun stopAnimation() {
            mCategoryBackground.clearAnimation()
            mCategoryBackground.setImageDrawable(
                ContextCompat.getDrawable(
                    itemView.context, R.drawable.category_background
                )
            )
            categoryIconAnimSetDownBase.start()
        }

        fun onAttach() {
            if (mCategoryHolder?.isActiv == true) {
                animateCategory()
                mPreviousHolder = this
            }
        }

        fun onBind(category: CategoryFilter) {
            mPosition = layoutPosition
            mCategoryHolder = category
            Glide.with(itemView)
                .load(category.drawable)
                .into(mImageView)
            mTextView.text = category.name
        }

        override fun onClick(v: View) {
            mPreviousHolder?.let {
                if (it.mPosition > 0) {
                    categoryList[it.mPosition].isActiv = false
                }
            }

            mPreviousHolder?.mCategoryHolder?.isActiv = false
            mPreviousHolder?.stopAnimation()
            mPreviousHolder = this@CategoryItemHolder
            animateCategory()
            categoryList[layoutPosition].isActiv = true
            mCallBacks.callCategoryFilter(categoryList[layoutPosition].name)
        }

        private fun animateCategory() {
            mCategoryBackground.setImageDrawable(
                ContextCompat.getDrawable(
                    itemView.context, R.drawable.category_dashed_border
                )
            )

            mCategoryBackground.startAnimation(
                AnimationUtils
                    .loadAnimation(itemView.context, R.anim.category_back_anim)
            )

            val categoryAnimScaleYUp = ObjectAnimator
                .ofFloat(mImageView, "scaleY", mImageView.scaleY, ICON_SCALE)
                .setDuration(200)

            val categoryAnimScaleXUp = ObjectAnimator
                .ofFloat(mImageView, "scaleX", mImageView.scaleX, ICON_SCALE)
                .setDuration(200)

            val categoryAnimScaleYDown = ObjectAnimator
                .ofFloat(mImageView, "scaleY", ICON_SCALE, 1f)
                .setDuration(200)

            val categoryAnimScaleXDown = ObjectAnimator
                .ofFloat(mImageView, "scaleX", ICON_SCALE, 1f)
                .setDuration(200)

            val categoryBackAnimScaleYUp = ObjectAnimator
                .ofFloat(
                    mCategoryBackground, "scaleY",
                    mCategoryBackground.scaleY, BACKGROUND_SCALE
                ).setDuration(200)

            val categoryBackAnimScaleXUp = ObjectAnimator
                .ofFloat(
                    mCategoryBackground, "scaleX",
                    mCategoryBackground.scaleX, BACKGROUND_SCALE
                ).setDuration(200)

            val categoryBackAnimScaleYDown = ObjectAnimator
                .ofFloat(
                    mCategoryBackground, "scaleY",
                    BACKGROUND_SCALE, 1.05f
                ).setDuration(200)

            val categoryBackAnimScaleXDown = ObjectAnimator
                .ofFloat(
                    mCategoryBackground, "scaleX",
                    BACKGROUND_SCALE, 1.05f
                ).setDuration(200)

            val categoryIconAnimSetDown = AnimatorSet()
            categoryIconAnimSetDown
                .play(categoryAnimScaleYDown)
                .with(categoryAnimScaleXDown)
                .with(categoryBackAnimScaleYDown)
                .with(categoryBackAnimScaleXDown)

            val categoryIconAnimSetUp = AnimatorSet()
            categoryIconAnimSetUp
                .play(categoryAnimScaleYUp)
                .with(categoryAnimScaleXUp)
                .with(categoryBackAnimScaleXUp)
                .with(categoryBackAnimScaleYUp)
                .before(categoryIconAnimSetDown)
            categoryIconAnimSetUp.start()

            val categoryBackAnimScaleYDownBase = ObjectAnimator
                .ofFloat(
                    mPreviousHolder?.mCategoryBackground, "scaleY",
                    1.05f, 1f
                ).setDuration(200)

            val categoryBackAnimScaleXDownBase = ObjectAnimator
                .ofFloat(
                    mPreviousHolder?.mCategoryBackground, "scaleX",
                    1.05f, 1f
                ).setDuration(200)

            categoryIconAnimSetDownBase
                .play(categoryBackAnimScaleYDownBase)
                .with(categoryBackAnimScaleXDownBase)
        }

        init {
            itemView.setOnClickListener(this)
            mImageView = itemView.findViewById(R.id.category_button)
            mTextView = itemView.findViewById(R.id.text_category_view)
            mCategoryBackground = itemView.findViewById(R.id.category_back)
        }
    }


    enum class CategoryFilter (
        val drawable: Int,
        var isActiv: Boolean,
    ) {
        Sunset(R.drawable.sunset, false),
        Beach(R.drawable.beach, false),
        Water(R.drawable.water, false),
        Sky(R.drawable.sky, false),
        Flower(R.drawable.flower, false),
        Nature(R.drawable.nature, false),
        Blue(R.drawable.blue, false),
        Night(R.drawable.night, false),
        White(R.drawable.white, false),
        Tree(R.drawable.tree, false),
        Green(R.drawable.green, false),
        Flowers(R.drawable.flowers, false),
        Portrait(R.drawable.portrait, false),
        Art(R.drawable.art, false),
        Light(R.drawable.light, false),
        Snow(R.drawable.snow, false),
        Dog(R.drawable.dog, false),
        Sun(R.drawable.sun, false),
        Clouds(R.drawable.clouds, false),
        Cat(R.drawable.cat, false),
        Park(R.drawable.park, false),
        Winter(R.drawable.winter, false),
        Landscape(R.drawable.landscape, false),
        Street(R.drawable.street, false),
        Summer(R.drawable.summer, false),
        Sea(R.drawable.sea, false),
        City(R.drawable.city, false),
        Trees(R.drawable.trees, false),
        Yellow(R.drawable.yellow, false),
        Lake(R.drawable.lake, false),
        Christmas(R.drawable.christmas, false),
        People(R.drawable.people, false),
        Bridge(R.drawable.bridge, false),
        Family(R.drawable.family, false),
        Bird(R.drawable.bird, false),
        River(R.drawable.river, false),
        Pink(R.drawable.pink, false),
        House(R.drawable.house, false),
        Car(R.drawable.car, false),
        Food(R.drawable.food, false),
        Bw(R.drawable.bw, false),
        Old(R.drawable.old, false),
        Macro(R.drawable.macro, false),
        Music(R.drawable.music, false),
        New(R.drawable.newa, false),
        Moon(R.drawable.moon, false),
        Orange(R.drawable.orange, false),
        Gardens(R.drawable.gardens, false),
        BlackWhite(R.drawable.blackwhite, false);

    }
}