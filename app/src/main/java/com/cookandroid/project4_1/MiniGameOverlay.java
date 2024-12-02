package com.cookandroid.project4_1;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import java.util.Random;

public class MiniGameOverlay extends Fragment {
    private int correctButtonIndex; // 정답 버튼의 인덱스
    private int round = 1; // 현재 라운드 (1: A팀, 2: B팀)
    private boolean buttonsEnabled = false; // 버튼 활성화 여부
    private TextView countdownText;
    private View rootView; // rootView를 인스턴스 변수로 정의
    private MiniGameListener listener; // 게임 종료 이벤트 전달 리스너

    private boolean DS_TEAM_SUCCESS = false;
    private boolean VT_TEAM_SUCCESS = false;


    public MiniGameOverlay() {
        // Required empty public constructor
    }

    // MiniGame 종료 이벤트 리스너 인터페이스 정의
    public interface MiniGameListener {
        void onMiniGameFinished(Bundle resultData); // Bundle 데이터를 전달
    }



    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof MiniGameListener) {
            listener = (MiniGameListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement MiniGameListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.minigame_layout, container, false);

        TextView resultText = rootView.findViewById(R.id.result_text);
        countdownText = rootView.findViewById(R.id.countdown_text);

        // 버튼 9개 가져오기
        Button button1 = rootView.findViewById(R.id.button1);
        Button button2 = rootView.findViewById(R.id.button2);
        Button button3 = rootView.findViewById(R.id.button3);
        Button button4 = rootView.findViewById(R.id.button4);
        Button button5 = rootView.findViewById(R.id.button5);
        Button button6 = rootView.findViewById(R.id.button6);
        Button button7 = rootView.findViewById(R.id.button7);
        Button button8 = rootView.findViewById(R.id.button8);
        Button button9 = rootView.findViewById(R.id.button9);

        Button[] buttons = {button1, button2, button3, button4, button5, button6, button7, button8, button9};

        // 게임 시작 전 준비 단계
        startPreparation(countdownText, resultText, buttons);
        return rootView;
    }


    // 준비 시간 동안 버튼 비활성화
    private void startPreparation(TextView countdownText, TextView resultText, Button[] buttons) {
        buttonsEnabled = false; // 버튼 비활성화
        countdownText.setVisibility(TextView.VISIBLE);
        Handler handler = new Handler();

        for (int i = 3; i > 0; i--) {
            int finalI = i;
            handler.postDelayed(() -> countdownText.setText("게임 시작 전: " + finalI), (3 - finalI) * 1000);
        }

        // 준비 시간 종료 후 게임 시작
        handler.postDelayed(() -> {
            countdownText.setVisibility(TextView.GONE);
            buttonsEnabled = true; // 버튼 활성화
            startTeamTurn(resultText, buttons); // 첫 번째 팀(A팀) 차례 시작
        }, 3000);
    }

    private void startTeamTurn(TextView resultText, Button[] buttons) {
        // 현재 팀 표시
        String currentTeam = round == 1 ? "DS팀" : "VT팀";
        resultText.setText(currentTeam + "의 차례입니다. 버튼을 눌러주세요!");

        // 랜덤으로 정답 버튼 설정
        Random random = new Random();
        correctButtonIndex = random.nextInt(9); // 0~8 중 하나의 번호를 정답으로 설정

        // 버튼 초기화: 텍스트와 상태 재설정
        for (int i = 0; i < buttons.length; i++) {
            buttons[i].setText(""); // 버튼 텍스트 초기화
            buttons[i].setEnabled(false); // 버튼 비활성화
        }

        // 3초 카운트다운 시작 (준비 단계)

        countdownText.setVisibility(TextView.VISIBLE);

        Handler handler = new Handler();
        for (int i = 3; i > 0; i--) {
            int finalI = i;
            handler.postDelayed(() -> countdownText.setText("게임 시작 전: " + finalI), (3 - finalI) * 1000);
        }

        // 준비 카운트다운 종료 후 버튼 텍스트 설정 및 활성화
        handler.postDelayed(() -> {
            countdownText.setVisibility(TextView.GONE);
            buttonsEnabled = true; // 버튼 활성화

            // 버튼 텍스트 설정: 정답은 "재촉", 나머지는 "재쵹"
            for (int i = 0; i < buttons.length; i++) {
                if (i == correctButtonIndex) {
                    buttons[i].setText("1");
                } else {
                    buttons[i].setText("2");
                }

                // 버튼 클릭 리스너 설정
                int index = i; // 내부 클래스에서 사용하기 위해 final 선언
                buttons[i].setEnabled(true); // 버튼 활성화
                buttons[i].setOnClickListener(v -> {
                    if (!buttonsEnabled) return; // 버튼이 비활성화되었으면 클릭 무시
                    buttonsEnabled = false; // 버튼 비활성화
                    handler.removeCallbacksAndMessages(null); // 타이머 취소
                    handleButtonClick(resultText, currentTeam, index); // 클릭 처리
                });
            }

            // 게임 진행 중 카운트다운 시작 (실패 조건)
            startGameCountdown(handler, countdownText, resultText, currentTeam);
        }, 3000); // 2초 후 실행
    }

    // 게임 진행 중 카운트다운
    private void startGameCountdown(Handler handler, TextView countdownText, TextView resultText, String currentTeam) {
        countdownText.setVisibility(TextView.VISIBLE);

        // 3초 카운트다운
        for (int i = 3; i > 0; i--) {
            int finalI = i;
            handler.postDelayed(() -> countdownText.setText("남은 시간: " + finalI), (3 - finalI) * 1000);
        }

        // 카운트다운 종료 시 실패 처리
        handler.postDelayed(() -> {
            if (buttonsEnabled) { // 버튼이 활성화된 상태라면 실패 처리
                buttonsEnabled = false;
                resultText.setText(currentTeam + ": 실패! (시간 초과)");
                moveToNextTeam(resultText);
            }
        }, 3000); // 3초 후 실행
    }

    // 버튼 클릭 처리
    private void handleButtonClick(TextView resultText, String currentTeam, int index) {
        boolean isCorrect = (index == correctButtonIndex);

        if (isCorrect) {
            resultText.setText(currentTeam + ": 성공!");
            if ("DS팀".equals(currentTeam)) {
                DS_TEAM_SUCCESS = true;
            } else if ("VT팀".equals(currentTeam)) {
                VT_TEAM_SUCCESS = true;
            }
        } else {
            resultText.setText(currentTeam + ": 실패! (잘못된 버튼 클릭)");
        }

        moveToNextTeam(resultText); // 다음 팀으로 이동
    }


    // 다음 팀으로 이동
    private void moveToNextTeam(TextView resultText) {
        Handler handler = new Handler();

        handler.postDelayed(() -> {
            if (round == 1) {
                round = 2; // VT 팀의 차례로 전환
                startTeamTurn(resultText, new Button[]{
                        rootView.findViewById(R.id.button1), rootView.findViewById(R.id.button2), rootView.findViewById(R.id.button3),
                        rootView.findViewById(R.id.button4), rootView.findViewById(R.id.button5), rootView.findViewById(R.id.button6),
                        rootView.findViewById(R.id.button7), rootView.findViewById(R.id.button8), rootView.findViewById(R.id.button9)
                });
            } else {
                resultText.setText("게임 종료!");

                // 결과 데이터를 동적으로 설정
                Bundle resultData = new Bundle();
                resultData.putBoolean("DS_SUCCESS", DS_TEAM_SUCCESS);
                resultData.putBoolean("VT_SUCCESS", VT_TEAM_SUCCESS);
                resultData.putInt("DS_POINTS", DS_TEAM_SUCCESS ? 5 : 0); // DS 팀 성공 시 5점
                resultData.putInt("VT_POINTS", VT_TEAM_SUCCESS ? 5 : 0); // VT 팀 성공 시 5점

                handler.postDelayed(() -> {
                    if (listener != null) {
                        listener.onMiniGameFinished(resultData); // MainActivity로 결과 전달
                    }
                    requireActivity().getSupportFragmentManager().beginTransaction()
                            .remove(MiniGameOverlay.this)
                            .commit();
                }, 3000);
            }
        }, 3000);
    }


}