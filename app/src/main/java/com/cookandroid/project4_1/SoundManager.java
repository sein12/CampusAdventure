package com.cookandroid.project4_1;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.SoundPool;

public class SoundManager {
    private SoundPool soundPool;
    private int diceSoundId;

    public SoundManager(Context context) {
        // SoundPool 초기화
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        soundPool = new SoundPool.Builder()
                .setMaxStreams(5)
                .setAudioAttributes(audioAttributes)
                .build();

        // 사운드 로드
        diceSoundId = soundPool.load(context, R.raw.dice_sound, 1);
    }

    // 주사위 소리 재생 메서드
    public void playDiceSound() {
        soundPool.play(diceSoundId, 1, 1, 0, 0, 1); // 볼륨 1.0, 반복 없음, 속도 1.0
    }

    // SoundPool 해제 메서드 (필수)
    public void release() {
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
    }
}