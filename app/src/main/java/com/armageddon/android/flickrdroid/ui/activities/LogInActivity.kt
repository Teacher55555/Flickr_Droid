package com.armageddon.android.flickrdroid.ui.activities


import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.armageddon.android.flickrdroid.R
import com.armageddon.android.flickrdroid.databinding.ActivityIntroBinding
import com.armageddon.android.flickrdroid.ui.fragments.LogInFragment
import com.armageddon.android.flickrdroid.ui.fragments.LogoFragment
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


private const val SHOW_LOGO_ANIMATION = "show_logo_animation"

class LogInActivity : SingleFragmentActivity(), LogoFragment.CallBacks {

    private lateinit var binding: ActivityIntroBinding

    companion object {
        fun newIntent(context: Context, url: String = ""): Intent {
            return Intent(context, LogInActivity::class.java)
        }
    }

    override fun getFragment(): Fragment {
        return LogoFragment.newInstance(false)
    }

    override fun onLogoAnimationEnd() {
        binding = ActivityIntroBinding.inflate(layoutInflater)
        setContentView(binding.root)
        lifecycleScope.launch {
            delay(20)
            setIntroDotActive()
        }

        binding.apply {
            closeButton.visibility = View.GONE
            introBackButton.setOnClickListener { startOauthActivity() }
            introNextButton.setOnClickListener { binding.viewPager.currentItem += 1 }
            viewPager.offscreenPageLimit = 3
            viewPager.adapter = object : FragmentStateAdapter(this@LogInActivity) {
                override fun getItemCount() = LogInFragment.SLIDE_COUNT
                override fun createFragment(position: Int) = LogInFragment.newInstance(position)
            }
            viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    setIntroDotActive(position)
                    when (position) {
                        0 -> {
                            introBackButton.apply {
                                text = getString(R.string.Back)
                                setOnClickListener { onBackPressed() }
                            }
                        }
                        LogInFragment.SLIDE_COUNT - 1 -> {
                            introNextButton.apply {
                                text = getString(R.string.login)
                                val img = ContextCompat.getDrawable(
                                    this@LogInActivity, R.drawable.ic_baseline_login_24)
                                setCompoundDrawablesWithIntrinsicBounds(null, null, img, null)
                                setOnClickListener { startOauthActivity() }
                            }
                        }
                        else -> {
                            introBackButton.apply {
                                setOnClickListener { viewPager.currentItem -= 1 }
                            }
                            introNextButton.apply {
                                text = getString(R.string.Next)
                                setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
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
        for (i in 0 until LogInFragment.SLIDE_COUNT) {
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

    fun startOauthActivity () {
        startActivity(OauthActivity.newIntent(this))
        finish()
    }

}






