package com.armageddon.android.flickrdroid.ui.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.armageddon.android.flickrdroid.R;
import com.armageddon.android.flickrdroid.common.ActivityUtils;
import com.armageddon.android.flickrdroid.ui.fragments.IntroFragment;


/**
 * Contains ViewPager for intro slides and
 * controls UI (buttons, dots - pages indicator).
 */

public class IntroActivity extends AppCompatActivity {
    private ViewPager mViewPager;
    private LinearLayout mIntroDotLayout;
    private Button mBackButton, mNextButton;


    public static Intent newIntent (Context context) {
        return new Intent(context, IntroActivity.class);
    }

    public void setIntroDotActive (int number) {
        mIntroDotLayout.removeAllViews();
        for (int i = 0; i < IntroFragment.SLIDE_COUNT; i++) {
            ImageView introDot = new ImageView(this);
            if (i == number) {
                introDot.setImageDrawable(ContextCompat.getDrawable(this,
                        R.drawable.intro_activ));
            } else {
                introDot.setImageDrawable(ContextCompat.getDrawable(this,
                        R.drawable.intro_inactiv));
            }
            ImageView introDotSpace = new ImageView(this);
            introDotSpace.setImageDrawable(ContextCompat.getDrawable(this,
                    R.drawable.intro_dot_space));
            mIntroDotLayout.addView(introDot);
            mIntroDotLayout.addView(introDotSpace);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ActivityUtils.onActivityCreateSetTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

         mIntroDotLayout = findViewById(R.id.intro_dot_layout);
         mBackButton = findViewById(R.id.intro_back_button);
         mNextButton = findViewById(R.id.intro_next_button);
         mBackButton.setOnClickListener(l ->
                 startActivity(SingInActivity.newIntent(IntroActivity.this)));
         mNextButton.setOnClickListener(l ->
                 mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1));
         setIntroDotActive(0);

         mViewPager = findViewById(R.id.view_pager);
         mViewPager.setAdapter(
                new FragmentStatePagerAdapter(
                        getSupportFragmentManager(),
                        PagerAdapter.POSITION_NONE) {
            @NonNull
            @Override
            public Fragment getItem(int position) {
                return IntroFragment.newInstance(position);
            }

            @Override
            public int getCount() {
                return IntroFragment.SLIDE_COUNT;
            }
        });

         mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                setIntroDotActive(position);
                if (position == 0) {
                    mBackButton.setForeground(null);
                    mBackButton.setText(getString(R.string.IntroSkip));
                    mBackButton.setOnClickListener(l ->
                            startActivity(SingInActivity.newIntent(IntroActivity.this))
                    );
                } else {
                    mBackButton.setText(null);
                    mBackButton.setForeground(ContextCompat.getDrawable(IntroActivity.this,
                            R.drawable.arrow_left));
                    mBackButton.setOnClickListener(l ->
                            mViewPager.setCurrentItem(mViewPager.getCurrentItem() - 1));
                }
                if (position == IntroFragment.SLIDE_COUNT - 1) {
                    mNextButton.setForeground(null);
                    mNextButton.setText(getString(R.string.introFinish));
                    mNextButton.setOnClickListener(l ->
                            startActivity(SingInActivity.newIntent(IntroActivity.this))
                    );
                } else {
                    mNextButton.setForeground(ContextCompat.getDrawable(IntroActivity.this,
                            R.drawable.arrow_right));
                    mNextButton.setText(null);
                    mNextButton.setOnClickListener(l ->
                            mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1));
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }
}