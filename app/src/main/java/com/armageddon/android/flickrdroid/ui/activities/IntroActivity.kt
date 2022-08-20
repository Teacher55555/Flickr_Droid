package com.armageddon.android.flickrdroid.ui.activities

import android.content.Context
import android.content.Intent
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.armageddon.android.flickrdroid.R
import com.armageddon.android.flickrdroid.databinding.ActivityIntroBinding
import com.armageddon.android.flickrdroid.ui.fragments.IntroFragment
import com.armageddon.android.flickrdroid.ui.fragments.LogoFragment
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val SHOW_LOGO_ANIMATION = "show_logo_animation"

class IntroActivity : SingleFragmentActivity(), LogoFragment.CallBacks {

    private lateinit var binding: ActivityIntroBinding

    companion object {
        fun newIntent(context: Context, showLogoAnimation: Boolean) : Intent {
            return Intent(context, IntroActivity::class.java).apply {
                putExtra(SHOW_LOGO_ANIMATION, showLogoAnimation)
            }
        }
    }

    override fun getFragment(): Fragment {
        val showAnimation = intent.getBooleanExtra(SHOW_LOGO_ANIMATION, false)
        return LogoFragment.newInstance(showAnimation)
    }

    override fun onLogoAnimationEnd() {
        binding = ActivityIntroBinding.inflate(layoutInflater)
        setContentView(binding.root)
        lifecycleScope.launch {
            delay(20)
            setIntroDotActive()
        }

        binding.apply {
            closeButton.setOnClickListener { startShowActivity() }
            introBackButton.setOnClickListener { startShowActivity() }
            introNextButton.setOnClickListener { binding.viewPager.currentItem += 1 }
            viewPager.offscreenPageLimit = 4
            viewPager.adapter = object : FragmentStateAdapter(this@IntroActivity) {
                override fun getItemCount() = IntroFragment.SLIDE_COUNT
                override fun createFragment(position: Int) = IntroFragment.newInstance(position)
            }
            viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    setIntroDotActive(position)

                    when (position) {
                        0 -> {
                            introBackButton.apply {
                                text = getString(R.string.IntroSkip)
                                setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
                                setOnClickListener { startShowActivity() }
                            }
                        }
                        IntroFragment.SLIDE_COUNT - 1 -> {
                            introNextButton.apply {
                                text = getString(R.string.introFinish)
                                setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
                                setOnClickListener { startShowActivity() }
                            }
                        }
                        else -> {
                            introBackButton.apply {
                                text = getString(R.string.Back)
                                val img = ContextCompat.getDrawable(
                                    this@IntroActivity, R.drawable.arrow_left)
                                setOnClickListener { viewPager.currentItem -= 1 }
                            }
                            introNextButton.apply {
                                text = getString(R.string.Next)
                                val img = ContextCompat.getDrawable(
                                    this@IntroActivity, R.drawable.arrow_right)
                                setOnClickListener { viewPager.currentItem += 1 }
                            }
                        }
                    }
                }
            })
        }
    }

    private fun setIntroDotActive(number: Int = 0) {
        binding.introDotLayout.removeAllViews()
        for (i in 0 until IntroFragment.SLIDE_COUNT) {
            val introDot = ImageView(this)
            if (i == number) {
                introDot.setImageDrawable(
                    ContextCompat.getDrawable(this, R.drawable.intro_activ)
                )
            } else {
                introDot.setImageDrawable(
                    ContextCompat.getDrawable(this, R.drawable.intro_inactiv)
                )
            }
            val introDotSpace = ImageView(this)
            introDotSpace.setImageDrawable(
                ContextCompat.getDrawable(this, R.drawable.intro_dot_space)
            )
           binding.introDotLayout.apply {
               addView(introDot)
               addView(introDotSpace)
           }
        }
    }

    fun startShowActivity () {
        val intent = TopPhotoActivity
            .newIntent(this@IntroActivity)
        startActivity(intent)
        finish()
    }

}
