package com.cookandroid.project4_1;

import android.content.Context;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

public class StarAnimationManager {
    private final Context context;
    private final ImageView starImage;

    public StarAnimationManager(Context context, ImageView starImage) {
        this.context = context;
        this.starImage = starImage;
    }

    public void startAnimation() {
        if (starImage == null) {
            throw new IllegalArgumentException("Star ImageView cannot be null.");
        }

        // 매번 새로운 애니메이션 객체 생성
        Animation starAnimation = AnimationUtils.loadAnimation(context, R.anim.star_animation);

        // 애니메이션 리스너 추가
        starAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                // 애니메이션 시작 시 View 표시
                starImage.setVisibility(View.VISIBLE);
                starImage.bringToFront(); // 이미지를 화면 최상단으로 가져옴
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                // 애니메이션 종료 후 View 숨기기 및 초기화
                starImage.setVisibility(View.GONE);
                resetViewProperties();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // 반복 애니메이션 처리 없음
            }
        });

        // 애니메이션 시작
        starImage.startAnimation(starAnimation);
    }

    private void resetViewProperties() {
        // View의 속성을 초기화
        starImage.setScaleX(1.0f);
        starImage.setScaleY(1.0f);
        starImage.setAlpha(1.0f);
        starImage.setRotation(0f);
    }
}