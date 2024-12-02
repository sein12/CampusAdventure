package com.cookandroid.project4_1;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Collections;

public class SecondMinigame extends AppCompatActivity {

    private GridLayout gridLayout;
    private TextView timerText;
    private int blueCount = 12; // 초기 파란색 버튼 수
    private int redCount = 12;  // 초기 빨간색 버튼 수
    private final int GAME_DURATION = 10000; // 20초 제한 시간

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second_minigame);

        // View 연결
        gridLayout = findViewById(R.id.grid_buttons);
        timerText = findViewById(R.id.timer_text);

        // 게임 설명 알림 창
        showGameDescriptionDialog();
    }

    // 게임 설명 알림 창
    private void showGameDescriptionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("게임 설명")
                .setMessage("빨간색 버튼이 많으면 VT팀이 승리합니다.\n파란색 버튼이 많으면 DS팀이 승리합니다.")
                .setPositiveButton("시작", (dialog, which) -> {
                    setupGrid(); // 게임 버튼 배치
                    startGameTimer(); // 게임 타이머 시작
                })
                .setCancelable(false); // 알림창 닫기 비활성화
        builder.show();
    }

    // Dynamically create buttons for the grid
    private void setupGrid() {
        ArrayList<String> buttonColors = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            buttonColors.add("blue");
            buttonColors.add("red");
        }

        // 색상을 랜덤으로 섞음
        Collections.shuffle(buttonColors);

        for (int i = 0; i < 24; i++) {
            Button button = new Button(this);

            String color = buttonColors.get(i);
            if (color.equals("blue")) {
                button.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_light));
                button.setTag("blue");
            } else {
                button.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));
                button.setTag("red");
            }

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = GridLayout.LayoutParams.WRAP_CONTENT;
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1, 1f);
            params.rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1, 1f);
            params.setMargins(10, 10, 10, 10);
            button.setLayoutParams(params);

            button.setOnClickListener(v -> toggleButtonColor(button));

            gridLayout.addView(button);
        }
    }

    // 버튼 색상을 파란색과 빨간색으로 토글
    private void toggleButtonColor(Button button) {
        String colorTag = (String) button.getTag();

        if (colorTag.equals("blue")) {
            button.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));
            button.setTag("red");
            blueCount--;
            redCount++;
        } else {
            button.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_light));
            button.setTag("blue");
            blueCount++;
            redCount--;
        }
    }

    // 게임 시간 제한 타이머
    private void startGameTimer() {
        new CountDownTimer(GAME_DURATION, 1000) { // 제한 시간: 30초
            @Override
            public void onTick(long millisUntilFinished) {
                int secondsLeft = (int) (millisUntilFinished / 1000);
                timerText.setText(String.valueOf(secondsLeft));

                // 10초 남았을 때 강조 효과 추가
                if (secondsLeft <= 10) {
                    timerText.setTextColor(getResources().getColor(android.R.color.holo_red_light)); // 텍스트를 빨간색으로 변경
                } else {
                    timerText.setTextColor(getResources().getColor(android.R.color.black)); // 텍스트를 검정색으로 유지
                }
            }

            @Override
            public void onFinish() {
                timerText.setText("0");
                determineWinner();
            }
        }.start();
    }

    // 승리 조건 계산
    private void determineWinner() {
        String winningTeam;
        if (redCount > blueCount) {
            winningTeam = "VT 팀";
        } else if (blueCount > redCount) {
            winningTeam = "DS 팀";
        } else {
            winningTeam = "DRAW";
        }

        Intent resultIntent = new Intent();
        resultIntent.putExtra("WINNING_TEAM", winningTeam);
        setResult(RESULT_OK, resultIntent); // 결과 반환
        finish(); // SecondMinigame 종료
    }



    // 모달창(다이얼로그)으로 승리 결과 표시
    protected void showWinnerDialog(String winnerMessage) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("종료")
                .setMessage(winnerMessage)
                .setCancelable(false)
                .setNegativeButton("완료", (dialog, which) -> {
                    Intent intent = new Intent();
                    if (redCount > blueCount) {
                        intent.putExtra("WINNING_TEAM", "VT 팀");
                    } else if (blueCount > redCount) {
                        intent.putExtra("WINNING_TEAM", "DS 팀");
                    } else {
                        intent.putExtra("WINNING_TEAM", "DRAW");
                    }
                    setResult(RESULT_OK, intent);
                    finish();
                });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

}
