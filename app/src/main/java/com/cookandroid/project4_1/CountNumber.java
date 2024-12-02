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

public class CountNumber extends AppCompatActivity {

    private GridLayout gridLayout;
    private TextView timerText;
    private int currentNumber = 1; // 현재 눌러야 할 숫자

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_count_number);

        // View 연결
        gridLayout = findViewById(R.id.grid_buttons);
        timerText = findViewById(R.id.timer_text);

        setupGrid();
        startCountdown();
    }

    // 버튼들을 1~20 숫자로 초기화하고 랜덤으로 배치
    private void setupGrid() {
        ArrayList<Integer> numbers = new ArrayList<>();

        // 1~20 숫자를 리스트에 추가
        for (int i = 1; i <= 20; i++) {
            numbers.add(i);
        }

        // 숫자를 랜덤으로 섞음
        Collections.shuffle(numbers);

        // 20개의 버튼을 생성
        for (int i = 0; i < 20; i++) {
            Button button = new Button(this);
            int number = numbers.get(i); // 현재 버튼의 숫자
            button.setText(String.valueOf(number));
            button.setTag(number); // 숫자를 태그로 저장

            // 버튼 레이아웃 설정
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = GridLayout.LayoutParams.WRAP_CONTENT;
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1, 1f);
            params.rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1, 1f);
            params.setMargins(10, 10, 10, 10); // 버튼 간격
            button.setLayoutParams(params);

            // 버튼 클릭 리스너 추가
            button.setOnClickListener(v -> {
                int buttonNumber = (int) button.getTag(); // 버튼의 숫자 가져오기
                if (buttonNumber == currentNumber) {
                    button.setEnabled(false); // 클릭 비활성화
                    button.setAlpha(0.0f); // 버튼 투명 처리
                    currentNumber++; // 다음 숫자로 이동

                    // 모든 숫자를 올바르게 눌렀을 경우
                    if (currentNumber > 20) {
                        showSuccessDialog();
                    }
                }
            });

            // 버튼을 그리드에 추가
            gridLayout.addView(button);
        }
    }

    // 20초 카운트다운 시작
    private void startCountdown() {
        new CountDownTimer(20000, 1000) { // 20초 카운트다운
            @Override
            public void onTick(long millisUntilFinished) {
                timerText.setText("남은 시간: " + millisUntilFinished / 1000 + "초");
            }

            @Override
            public void onFinish() {
                if (currentNumber <= 20) {
                    showFailureDialog(); // 실패 메시지 표시
                }
            }
        }.start();
    }


    protected void showSuccessDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("성공!")
                .setMessage("5학점 획득하셨습니다!")
                .setCancelable(false)
                .setNegativeButton("완료", (dialog, which) -> {
                    Intent intent = new Intent(); // 결과 데이터를 담을 인텐트 생성
                    intent.putExtra("earnedPoints", 5); // 획득한 점수(5점)를 전달
                    setResult(RESULT_OK, intent); // 성공 결과와 함께 점수 전달
                    finish(); // 액티비티 종료
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }


    protected void showFailureDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("실패!")
                .setMessage("시간 초과! 다음에 다시 도전하세요!")
                .setCancelable(false)
                .setNegativeButton("확인", (dialog, which) -> {
                    setResult(RESULT_CANCELED); // 실패 결과 전달
                    finish(); // 액티비티 종료
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

}
