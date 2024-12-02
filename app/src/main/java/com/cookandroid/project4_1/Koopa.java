package com.cookandroid.project4_1;

import android.content.Context;
import android.widget.Toast;
import java.util.List;
import java.util.Random;

public class Koopa {
    private Context context;

    // Constructor에서 context 받기
    public Koopa(Context context) {
        this.context = context;
    }

    // 쿠파 칸 이벤트 메서드
    public static void activateKoopaEvent(Player currentPlayer, List<Player> players, Context context) {
        Random random = new Random();
        int eventNumber = random.nextInt(4);  // 0~3 사이의 랜덤 이벤트

        switch (eventNumber) {
            case 0:
                loseStar(currentPlayer, context);
                break;
            case 1:
                losePoints(currentPlayer, 10, context);
                break;
            case 2:
                stealStarFromOther(currentPlayer, 1, 10, context);
                break;
            case 3:
                randomgrade(currentPlayer, context);
                break;
        }
    }

    // 각 이벤트 메서드 (Toast 메시지로 변경)
    private static void loseStar(Player player, Context context) {
        if (player.getStar() > 1) {
            player.setStar(player.getStar() - 1);
            Toast.makeText(context, player.getName() + "님의 졸업이 미뤄졌습니다! (학년 감소!)", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, player.getName() + "님은 아직 1학년입니다! 사고가 발생해도 1학년이군요!", Toast.LENGTH_SHORT).show();
        }
    }

    private static void losePoints(Player player, int amount, Context context) {
        player.setPoint(Math.max(player.getPoint() - amount, 0));
        Toast.makeText(context, player.getName() + "이(가) " + amount + "학점을 잃었습니다!", Toast.LENGTH_SHORT).show();
    }

    private static void stealStarFromOther(Player player, int amount, int amount2, Context context) {
        player.setStar(Math.max(player.getStar() - amount, 0));
        player.setPoint(Math.max(player.getPoint() - amount2, 0));
        Toast.makeText(context, player.getName() + "이(가) " + amount + "학년과 " + amount2 + "학점을 잃었습니다!", Toast.LENGTH_SHORT).show();
    }

    private static void randomgrade(Player player, Context context) {
        Random random = new Random();
        int eventNumber = random.nextInt(3) + 1;  // 1~3 사이의 값
        player.setStar(eventNumber);
        Toast.makeText(context, player.getName() + "이(가) 랜덤한 학년을 부여받았습니다! " + eventNumber + "학년이 되셨습니다!", Toast.LENGTH_SHORT).show();
    }
}

