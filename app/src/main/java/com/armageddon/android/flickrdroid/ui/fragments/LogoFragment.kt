package com.armageddon.android.flickrdroid.ui.fragments

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.LinearGradient
import android.graphics.Shader
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.animation.DecelerateInterpolator
import androidx.fragment.app.Fragment
import com.armageddon.android.flickrdroid.R
import com.armageddon.android.flickrdroid.databinding.FragmentLogoBinding

private const val SHOW_ANIMATION_BUNDLE = "show_animation_bundle"

class LogoFragment : Fragment() {
    private var callBacks: CallBacks? = null
    private var _binding: FragmentLogoBinding? = null
    private val binding get() = _binding!!
    private var mShowAnimation = true

    companion object {
        fun newInstance(showAnimation: Boolean): LogoFragment {
            val args = Bundle()
            args.putBoolean(SHOW_ANIMATION_BUNDLE, showAnimation)
            val fragment = LogoFragment()
            fragment.arguments = args
            return fragment
        }
    }

    interface CallBacks {
        fun onLogoAnimationEnd ()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBacks = context as CallBacks
    }

    override fun onDetach() {
        super.onDetach()
        callBacks = null
        _binding = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mShowAnimation = arguments?.getBoolean(SHOW_ANIMATION_BUNDLE) ?: true
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLogoBinding.inflate(inflater, container, false)
        if (mShowAnimation) {
            binding.root.viewTreeObserver.addOnGlobalLayoutListener(
                object : ViewTreeObserver.OnGlobalLayoutListener {
                    override fun onGlobalLayout() {
                        animationShow()
                        binding.root.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    }
                })
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (!mShowAnimation) {
            callBacks?.onLogoAnimationEnd()
        }
    }

    private fun animationShow() {
        val mainElementsDuration = 800L
        val alphaDuration = 500L
        val rightEyeStartDelay = 500L
        val blinkAnimationDuration = 100L
        val antennaCapBaseY = binding.antennaCap.y
        val antennaStemBaseY = binding.antennaStem.y

        // Gradient for text
        val textShader = LinearGradient(
            0f, 0f, 0f,
            binding.logoText.paint.textSize,
            intArrayOf(
                resources.getColor(R.color.colorPinkFlickr, null),
                resources.getColor(R.color.colorBlueFlickr, null)
            ),
            floatArrayOf(0f, 1f),
            Shader.TileMode.CLAMP
        )

        binding.logoText.apply {
            paint.shader = textShader
            text = getString(R.string.app_name)
            alpha = 0f
        }

        binding.face.apply {
            scaleX = 0f
            scaleY = 0f
            alpha = 0f
        }

        binding.leftEye.apply {
            scaleX = 15f
            scaleY = 15f
            alpha = 0f
        }

        binding.rightEye.apply {
            scaleX = 20f
            scaleY = 20f
            alpha = 0f
        }

        binding.antennaStem.apply {
            y -= 200
            alpha = 0f
        }

        binding.antennaCap.apply {
            y -= 200
            alpha = 0f
        }

        val textAlphaAnimation = ObjectAnimator
            .ofFloat(binding.logoText, "alpha", 0f, 1f)
            .setDuration(alphaDuration)
            .apply { startDelay = mainElementsDuration - blinkAnimationDuration }

        val faceAnimationScaleY = ObjectAnimator
            .ofFloat(binding.face, "scaleY", binding.face.scaleY, 1f)
            .setDuration(mainElementsDuration)

        val faceAnimationScaleX = ObjectAnimator
            .ofFloat(binding.face, "scaleX", binding.face.scaleX, 1f)
            .setDuration(mainElementsDuration)

        val faceAnimationAlpha = ObjectAnimator
            .ofFloat(binding.face, "alpha", binding.face.alpha, 1f)
            .setDuration(mainElementsDuration)

        val leftEyeAnimationScaleY = ObjectAnimator
            .ofFloat(binding.leftEye, "scaleY", binding.leftEye.scaleY, 1f)
            .setDuration(mainElementsDuration)
            .apply { interpolator = DecelerateInterpolator() }

        val leftEyeAnimationScaleX = ObjectAnimator
            .ofFloat(binding.leftEye, "scaleX", binding.leftEye.scaleX, 1f)
            .setDuration(mainElementsDuration)
            .apply { interpolator = DecelerateInterpolator() }

        val leftEyeAnimationAlpha = ObjectAnimator
            .ofFloat(binding.leftEye, "alpha", binding.leftEye.alpha, 1f)
            .setDuration(alphaDuration)

        val rightEyeAnimationScaleY = ObjectAnimator
            .ofFloat(binding.rightEye, "scaleY", binding.leftEye.scaleY, 1f)
            .setDuration(mainElementsDuration)
            .apply {
                startDelay = rightEyeStartDelay
                interpolator = DecelerateInterpolator()
            }

        val rightEyeAnimationScaleX = ObjectAnimator
            .ofFloat(binding.rightEye, "scaleX", binding.leftEye.scaleY, 1f)
            .setDuration(mainElementsDuration)
            .apply {
                startDelay = rightEyeStartDelay
                interpolator = DecelerateInterpolator()
            }

        val rightEyeAnimationAlpha = ObjectAnimator
            .ofFloat(binding.rightEye, "alpha", binding.rightEye.alpha, 1f)
            .setDuration(alphaDuration)
            .apply { startDelay = rightEyeStartDelay }

        val antennaStemAnimationY = ObjectAnimator
            .ofFloat(binding.antennaStem, "y", binding.antennaStem.y, antennaStemBaseY)
            .setDuration(mainElementsDuration)
            .apply { interpolator = DecelerateInterpolator() }

        val antennaCapAnimationY = ObjectAnimator
            .ofFloat(binding.antennaCap, "y", binding.antennaCap.y, antennaCapBaseY)
            .setDuration(mainElementsDuration)
            .apply { interpolator = DecelerateInterpolator() }

        val antennaStemAnimationAlpha = ObjectAnimator
            .ofFloat(binding.antennaStem, "alpha", binding.antennaStem.alpha, 1f)
            .setDuration(alphaDuration)

        val antennaCapAnimationAlpha = ObjectAnimator
            .ofFloat(binding.antennaCap, "alpha", binding.antennaCap.alpha, 1f)
            .setDuration(alphaDuration)

        val leftEyeAnimationScaleYUp = ObjectAnimator
            .ofFloat(binding.leftEye, "scaleY", 0.2f, 1f)
            .setDuration(blinkAnimationDuration)

        val leftEyeAnimationScaleYDown = ObjectAnimator
            .ofFloat(binding.leftEye, "scaleY", 1f, 0.2f)
            .setDuration(blinkAnimationDuration)

        val rightEyeAnimationScaleYUp = ObjectAnimator
            .ofFloat(binding.rightEye, "scaleY", 0.2f, 1f)
            .setDuration(blinkAnimationDuration)

        val rightEyeAnimationScaleYDown = ObjectAnimator
            .ofFloat(binding.rightEye, "scaleY", 1f, 0.2f)
            .setDuration(blinkAnimationDuration)

        //7 - launching the second animation of blinking eyes (UP)

        val eyeBlink2TimesUpAnimationSet = AnimatorSet().also { it
            .play(leftEyeAnimationScaleYUp)
            .with(rightEyeAnimationScaleYUp)
        }

        //6 - start second eyes blink down animation

        val eyeBlink2TimesDownAnimationSet = AnimatorSet().also { it
            .play(leftEyeAnimationScaleYDown)
            .with(rightEyeAnimationScaleYDown)
            .before(eyeBlink2TimesUpAnimationSet)
        }.apply { startDelay = blinkAnimationDuration }

        //5 - start first eyes blink up animation

        val eyeBlinkUpAnimationSet = AnimatorSet().also { it
            .play(leftEyeAnimationScaleYUp)
            .with(rightEyeAnimationScaleYUp)
            .before(eyeBlink2TimesDownAnimationSet)
        }

        //4 - start first eyes blink down animation

        val eyeBlinkDownAnimationSet = AnimatorSet().also { it
            .play(leftEyeAnimationScaleYDown)
            .with(rightEyeAnimationScaleYDown)
            .before(eyeBlinkUpAnimationSet)
        }.apply { startDelay = rightEyeStartDelay }

        // 3 - start antenna and text animation

        val antennaAnimationSet = AnimatorSet().also { it
            .play(antennaStemAnimationY)
            .with(antennaCapAnimationY)
            .with(antennaStemAnimationAlpha)
            .with(antennaCapAnimationAlpha)
            .with(textAlphaAnimation)
            .before(eyeBlinkDownAnimationSet)
        }

        // 2 - start eyes animation

        val eyeAnimationSet = AnimatorSet().also { it
            .play(leftEyeAnimationScaleY)
            .with(leftEyeAnimationScaleX)
            .with(leftEyeAnimationAlpha)
            .with(rightEyeAnimationScaleY)
            .with(rightEyeAnimationScaleX)
            .with(rightEyeAnimationAlpha)
            .before(antennaAnimationSet)
        }.apply { startDelay = rightEyeStartDelay }

        // 1 - start face animation

        AnimatorSet().also { it
            .play(faceAnimationScaleY)
            .with(faceAnimationScaleX)
            .with(faceAnimationAlpha)
            .with(eyeAnimationSet)
        }.apply {
            start()
        }

        eyeBlink2TimesUpAnimationSet.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                callBacks?.onLogoAnimationEnd()
            }
        })

    }
}




