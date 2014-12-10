package net.oschina.app.util;

import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;

/**
 * 录音动画类
 */
public class KJAnimations {

    /**
     * 旋转 Rotate
     */
    public static Animation getRotateAnimation(float fromDegrees,
            float toDegrees, long durationMillis) {
        RotateAnimation rotate = new RotateAnimation(fromDegrees, toDegrees,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                0.5f);
        rotate.setDuration(durationMillis);
        rotate.setFillAfter(true);
        return rotate;
    }

    /**
     * 透明度 Alpha
     */
    public static Animation getAlphaAnimation(float fromAlpha, float toAlpha,
            long durationMillis) {
        AlphaAnimation alpha = new AlphaAnimation(fromAlpha, toAlpha);
        alpha.setDuration(durationMillis);
        alpha.setFillAfter(true);
        return alpha;
    }

    /**
     * 缩放 Scale
     */
    public static Animation getScaleAnimation(float scaleXY, long durationMillis) {
        ScaleAnimation scale = new ScaleAnimation(1.0f, scaleXY, 1.0f, scaleXY,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                0.5f);
        scale.setDuration(durationMillis);
        return scale;
    }

    /**
     * 位移 Translate
     */
    public static Animation getTranslateAnimation(float fromXDelta,
            float toXDelta, float fromYDelta, float toYDelta,
            long durationMillis) {
        TranslateAnimation translate = new TranslateAnimation(fromXDelta,
                toXDelta, fromYDelta, toYDelta);
        translate.setDuration(durationMillis);
        translate.setFillAfter(true);
        return translate;
    }

    public static Animation clickAnimation(float scaleXY, long durationMillis) {
        AnimationSet set = new AnimationSet(true);
        set.addAnimation(getScaleAnimation(scaleXY, durationMillis));
        set.setDuration(durationMillis);
        return set;
    }

    /**
     * 打开的动画
     * 
     * @param relativeLayout
     *            子菜单容器
     * @param background
     *            子菜单背景
     * @param menu
     *            菜单按钮
     * @param durationMillis
     *            动画时间
     */
    public static void openAnimation(RelativeLayout relativeLayout,
            ImageView menu, long durationMillis) {
        relativeLayout.setVisibility(View.VISIBLE);
        for (int i = 1; i < relativeLayout.getChildCount(); i++) {
            ImageView imageView = null;
            if (relativeLayout.getChildAt(i) instanceof ImageView) {
                imageView = (ImageView) relativeLayout.getChildAt(i);
            } else {
                continue;
            }

            int top = imageView.getTop();
            int left = imageView.getLeft();
            if (top == 0) {
                top = menu.getTop() * (-i);
            }
            if (left == 0) {
                left = menu.getLeft();
            }
            AnimationSet set = new AnimationSet(true);
            set.addAnimation(getRotateAnimation(-360, 0, durationMillis));
            set.addAnimation(getAlphaAnimation(0.5f, 1.0f, durationMillis));
            set.addAnimation(getTranslateAnimation(menu.getLeft() - left, 0,
                    menu.getTop() - top, 0, durationMillis));
            set.setFillAfter(true);
            set.setDuration(durationMillis);
            set.setStartOffset((i * 100)
                    / (-1 + relativeLayout.getChildCount()));
            set.setInterpolator(new OvershootInterpolator(1f));
            imageView.startAnimation(set);
        }
    }

    /**
     * 关闭的动画
     * 
     * @param relativeLayout
     *            子菜单容器
     * @param background
     *            子菜单背景
     * @param menu
     *            菜单按钮
     * @param durationMillis
     *            动画时间
     */
    public static void closeAnimation(final RelativeLayout relativeLayout,
            final ImageView menu, long durationMillis) {
        for (int i = 1; i < relativeLayout.getChildCount(); i++) {
            ImageView imageView = null;
            if (relativeLayout.getChildAt(i) instanceof ImageView) {
                imageView = (ImageView) relativeLayout.getChildAt(i);
            } else {
                continue;
            }

            AnimationSet set = new AnimationSet(true);
            set.addAnimation(getRotateAnimation(0, -360, durationMillis));
            set.addAnimation(getAlphaAnimation(1.0f, 0.5f, durationMillis));
            set.addAnimation(getTranslateAnimation(0, menu.getLeft()
                    - imageView.getLeft(), 0,
                    menu.getTop() - imageView.getTop(), durationMillis));
            set.setFillAfter(true);
            set.setDuration(durationMillis);
            set.setStartOffset(((relativeLayout.getChildCount() - i) * 100)
                    / (-1 + relativeLayout.getChildCount()));
            set.setInterpolator(new AnticipateInterpolator(1f));
            set.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation arg0) {}

                @Override
                public void onAnimationRepeat(Animation arg0) {}

                @Override
                public void onAnimationEnd(Animation arg0) {
                    relativeLayout.setVisibility(View.GONE);
                }
            });
            imageView.startAnimation(set);
        }
    }

}
