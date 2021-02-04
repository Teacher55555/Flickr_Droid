package com.armageddon.android.flickrdroid.ui.fragments;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;

import com.armageddon.android.flickrdroid.R;
import com.armageddon.android.flickrdroid.ui.activities.IntroActivity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

/**
 * Logo animation fragment.
 * After animation starts IntroActivity
 */

public class LogoFragment extends Fragment {
   private TextView mLogoText;
   private View mFace;
   private View mLeftEye;
   private View mRightEye;
   private View mAntennaStem;
   private View mAntennaCap;
   private boolean showIntro;
   private static final String SHOW_INTRO = "show_intro";

    public static LogoFragment newInstance(boolean showIntro) {
        Bundle args = new Bundle();
        args.putBoolean(SHOW_INTRO, showIntro);
        LogoFragment fragment = new LogoFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        assert getArguments() != null;
        showIntro = getArguments().getBoolean(SHOW_INTRO);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_logo, container, false);
        mLogoText = view.findViewById(R.id.logo_text);
        mFace = view.findViewById(R.id.face);
        mLeftEye = view.findViewById(R.id.left_eye);
        mRightEye = view.findViewById(R.id.right_eye);
        mAntennaStem = view.findViewById(R.id.antenna_stem);
        mAntennaCap = view.findViewById(R.id.antenna_cap);

        view.getViewTreeObserver().addOnGlobalLayoutListener (
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                       animationShow(view);
                       view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                });
        return view;

    }

    private void animationShow (View view) {

        // Gradient for text initializer
        Shader textShader = new LinearGradient(0, 0, 0,
                        mLogoText.getPaint().getTextSize(),
                        new int[]{getResources().getColor(R.color.colorPinkFlickr, null),
                                getResources().getColor(R.color.colorBlueFlickr, null)},
                        new float[]{0, 1}, Shader.TileMode.CLAMP);
        mLogoText.getPaint().setShader(textShader);
        mLogoText.setText(getText(R.string.app_name));
        mLogoText.setAlpha(0);

        float antennaCapBaseY = mAntennaCap.getY();
        float antennaStemBaseY = mAntennaStem.getY();

        mFace.setScaleX(0);
        mFace.setScaleY(0);
        mFace.setAlpha(0);

        mLeftEye.setScaleX(15);
        mLeftEye.setScaleY(15);
        mLeftEye.setAlpha(0);

        mRightEye.setScaleX(20);
        mRightEye.setScaleY(20);
        mRightEye.setAlpha(0);

        mAntennaStem.setY(antennaStemBaseY - 200);
        mAntennaStem.setAlpha(0);

        mAntennaCap.setY(antennaCapBaseY - 200);
        mAntennaCap.setAlpha(0);

        int mainElementsDuration = 800;
        int alphaDuration = 500;
        int rightEyeStartDelay = 500;
        int blinkAnimationDuration = 100;

        ObjectAnimator textAlphaAnimation = ObjectAnimator
                .ofFloat(mLogoText, "alpha", 0, 1.0f)
                .setDuration(alphaDuration);
        textAlphaAnimation.setStartDelay(mainElementsDuration - 100);

        ObjectAnimator faceAnimationScaleY = ObjectAnimator
                .ofFloat(mFace, "scaleY", mFace.getScaleY(), 1)
                .setDuration(mainElementsDuration);

        ObjectAnimator faceAnimationScaleX = ObjectAnimator
                .ofFloat(mFace, "scaleX", mFace.getScaleX(), 1)
                .setDuration(mainElementsDuration);

        ObjectAnimator faceAnimationAlpha = ObjectAnimator
                .ofFloat(mFace, "alpha", mFace.getAlpha(), 1.0f)
                .setDuration(mainElementsDuration);

        ObjectAnimator leftEyeAnimationScaleY = ObjectAnimator
                .ofFloat(mLeftEye, "scaleY", mLeftEye.getScaleY(), 1)
                .setDuration(mainElementsDuration);
        leftEyeAnimationScaleY.setInterpolator(new DecelerateInterpolator());

        ObjectAnimator leftEyeAnimationScaleX = ObjectAnimator
                .ofFloat(mLeftEye, "scaleX", mLeftEye.getScaleX(), 1)
                .setDuration(mainElementsDuration);
        leftEyeAnimationScaleX.setInterpolator(new DecelerateInterpolator());

        ObjectAnimator leftEyeAnimationAlpha = ObjectAnimator
                .ofFloat(mLeftEye, "alpha", mLeftEye.getAlpha(), 1.0f)
                .setDuration(alphaDuration);

        ObjectAnimator rightEyeAnimationScaleY = ObjectAnimator
                .ofFloat(mRightEye, "scaleY", mRightEye.getScaleY(), 1)
                .setDuration(mainElementsDuration);
        rightEyeAnimationScaleY.setStartDelay(rightEyeStartDelay);
        rightEyeAnimationScaleY.setInterpolator(new DecelerateInterpolator());

        ObjectAnimator rightEyeAnimationScaleX = ObjectAnimator
                .ofFloat(mRightEye, "scaleX", mRightEye.getScaleX(), 1)
                .setDuration(mainElementsDuration);
        rightEyeAnimationScaleX.setStartDelay(rightEyeStartDelay);
        rightEyeAnimationScaleX.setInterpolator(new DecelerateInterpolator());

        ObjectAnimator rightEyeAnimationAlpha = ObjectAnimator
                .ofFloat(mRightEye, "alpha", mRightEye.getAlpha(), 1.0f)
                .setDuration(alphaDuration);
        rightEyeAnimationAlpha.setStartDelay(rightEyeStartDelay);

        ObjectAnimator antennaStemAnimationY = ObjectAnimator
                .ofFloat(mAntennaStem, "y", mAntennaStem.getY(), antennaStemBaseY)
                .setDuration(mainElementsDuration);
        antennaStemAnimationY.setInterpolator(new DecelerateInterpolator());

        ObjectAnimator antennaCapAnimationY = ObjectAnimator
                .ofFloat(mAntennaCap, "y", mAntennaCap.getY(), antennaCapBaseY)
                .setDuration(mainElementsDuration);
        antennaCapAnimationY.setInterpolator(new DecelerateInterpolator());

        ObjectAnimator antennaStemAnimationAlpha = ObjectAnimator
                .ofFloat(mAntennaStem, "alpha", mAntennaStem.getAlpha(), 1.0f)
                .setDuration(alphaDuration);

        ObjectAnimator antennaCapAnimationAlpha = ObjectAnimator
                .ofFloat(mAntennaCap, "alpha", mAntennaCap.getAlpha(), 1.0f)
                .setDuration(alphaDuration);

        ObjectAnimator leftEyeAnimationScaleYUp = ObjectAnimator
                .ofFloat(mLeftEye, "scaleY", 0.2f, 1)
                .setDuration(blinkAnimationDuration);

        ObjectAnimator leftEyeAnimationScaleYDown = ObjectAnimator
                .ofFloat(mLeftEye, "scaleY", 1, 0.2f)
                .setDuration(blinkAnimationDuration);

        ObjectAnimator rightEyeAnimationScaleYUp = ObjectAnimator
                .ofFloat(mRightEye, "scaleY", 0.2f, 1)
                .setDuration(blinkAnimationDuration);

        ObjectAnimator rightEyeAnimationScaleYDown = ObjectAnimator
                .ofFloat(mRightEye, "scaleY", 1, 0.2f)
                .setDuration(blinkAnimationDuration);

        //7 - start second eyes blink up animation
        AnimatorSet eyeBlink2TimesUpAnimationSet = new AnimatorSet();
        eyeBlink2TimesUpAnimationSet
                .play(leftEyeAnimationScaleYUp)
                .with(rightEyeAnimationScaleYUp);


        //6 - start second eyes blink down animation
        AnimatorSet eyeBlink2TimesDownAnimationSet = new AnimatorSet();
        eyeBlink2TimesDownAnimationSet
                .play(leftEyeAnimationScaleYDown)
                .with(rightEyeAnimationScaleYDown)
                .before(eyeBlink2TimesUpAnimationSet);
        eyeBlink2TimesDownAnimationSet.setStartDelay(100);

        //5 - start first eyes blink up animation
        AnimatorSet eyeBlinkUpAnimationSet = new AnimatorSet();
        eyeBlinkUpAnimationSet
                .play(leftEyeAnimationScaleYUp)
                .with(rightEyeAnimationScaleYUp)
                .before(eyeBlink2TimesDownAnimationSet);

        //4 - start first eyes blink down animation
        AnimatorSet eyeBlinkDownAnimationSet = new AnimatorSet();
        eyeBlinkDownAnimationSet
                .play(leftEyeAnimationScaleYDown)
                .with(rightEyeAnimationScaleYDown)
                .before(eyeBlinkUpAnimationSet);
        eyeBlinkDownAnimationSet.setStartDelay(500);

        // 3 - start antenna and text animation
        AnimatorSet antennaAnimationSet = new AnimatorSet();
        antennaAnimationSet
                .play(antennaStemAnimationY)
                .with(antennaCapAnimationY)
                .with(antennaStemAnimationAlpha)
                .with(antennaCapAnimationAlpha)
                .with(textAlphaAnimation)
                .before(eyeBlinkDownAnimationSet);

        // 2 - start eyes animation
        AnimatorSet eyeAnimationSet = new AnimatorSet();
        eyeAnimationSet
                .play(leftEyeAnimationScaleY)
                .with(leftEyeAnimationScaleX)
                .with(leftEyeAnimationAlpha)
                .with(rightEyeAnimationScaleY)
                .with(rightEyeAnimationScaleX)
                .with(rightEyeAnimationAlpha)
                .before(antennaAnimationSet);
        eyeAnimationSet.setStartDelay(500);

        // 1 - start face animation
        AnimatorSet logoAnimationSet = new AnimatorSet();
        logoAnimationSet
                .play(faceAnimationScaleY)
                .with(faceAnimationScaleX)
                .with(faceAnimationAlpha)
                .with(eyeAnimationSet);
        logoAnimationSet.start();

        // 8 - what to do after animation
        eyeBlink2TimesUpAnimationSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (showIntro && isAdded()) {
                    startActivity(IntroActivity
                            .newIntent(view.getContext()).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }







}
