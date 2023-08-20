package com.example.myapplication;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.view.View;
import android.view.animation.Animation;
import android.widget.LinearLayout;

public class AnimationUtils {

    public static void applySlideInAnimation(View view) {
        AnimatorSet animatorSet = (AnimatorSet) AnimatorInflater.loadAnimator(view.getContext(), R.animator.slide_in);
        animatorSet.setTarget(view);
        animatorSet.start();
        view.setVisibility(View.VISIBLE);
    }

    public static void applySlideOutAnimation(View view) {
        AnimatorSet animatorSet = (AnimatorSet) AnimatorInflater.loadAnimator(view.getContext(), R.animator.slide_out);
        animatorSet.setTarget(view);
        animatorSet.start();
        view.setVisibility(View.GONE);
    }
}
