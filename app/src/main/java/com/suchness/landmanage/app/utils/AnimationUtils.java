package com.suchness.landmanage.app.utils;

import android.animation.ValueAnimator;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;

/**
 * @Author hejunfeng
 * @Date 14:55 2021/3/30 0030
 * @Description com.analysis.wisdomtraffic.utils
 **/
public class AnimationUtils {
    public static <V extends View> void startZoomAnim(V v,int to){
        startValAnim(v.getWidth(), to, new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                ViewGroup.LayoutParams lp = v.getLayoutParams();
                int size = Integer.valueOf(animation.getAnimatedValue().toString());
                lp.height = size;
                v.setLayoutParams(lp);
            }
        },100);
    }


    public static void startValAnim(int from, int to, ValueAnimator.AnimatorUpdateListener listener,long duration){
        ValueAnimator animator = ValueAnimator.ofInt(from,to);
        //设置动画时长
        animator.setDuration(duration);
        //设置差值器
        animator.setInterpolator(new DecelerateInterpolator());
        //回调监听
        animator.addUpdateListener(listener);
        //启动动画
        animator.start();
    }
}
