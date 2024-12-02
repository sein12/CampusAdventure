package com.cookandroid.project4_1;

import androidx.appcompat.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Queue;
import java.util.LinkedList;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;



import androidx.fragment.app.FragmentTransaction;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements MiniGameOverlay.MiniGameListener {

    private final Queue<Runnable> alertQueue = new LinkedList<>(); // 알림 대기열
    private boolean isAlertShowing = false; // 현재 알림창이 표시 중인지 여부

    private static final int REQUEST_COUNT_NUMBER = 1; // CountNumber 액티비티 요청 코드

    private int roundCount = 0; // 현재 라운드
    private static final int MAX_ROUNDS = 10; // 총 4라운드

    private List<Player> players = new ArrayList<>();
    private int currentPlayerIndex = 0;  // 현재 플레이어 인덱스
    private int boardSize = 20;  // 보드의 크기 (20개의 칸)

    private String[] boardSpaces = {
            "정문", "인문과학자료실", "컴포즈커피", "오픈열람실", "집현전", "휴학",
            "노천극장", "함지마루", "연구실", "광운스퀘어",
            "공지사항", "정융과방", "융디랩", "코딩컨설팅룸", "새빛관 104호", "스타",
            "과제폭탄", "풋살장", "AI카페", "해동열람실"
    };

    private GridLayout boardGrid;
    private PlayerManager playerManager;
    private TextView currentPlayerText, currentPositionText;
    private TextView[] playerStars, playerCoins, playerNames;
    private Button[] boardButtons;
    private Button rollDiceButton;
    private SoundManager soundManager;
    private StarAnimationManager starAnimationManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        soundManager = new SoundManager(this);
        ImageView starImage = findViewById(R.id.starImage);
        starAnimationManager = new StarAnimationManager(this, starImage);


        // UI 요소 연결
        boardGrid = findViewById(R.id.boardGrid);
        rollDiceButton = findViewById(R.id.rollDiceButton);
        currentPlayerText = findViewById(R.id.currentPlayerText);
        currentPositionText = findViewById(R.id.currentPosition);

        // PlayerManager 초기화
        playerManager = PlayerManager.getInstance();
        playerManager.addPlayer("정준수(🔵)", Player.COLOR_BLUE, "DS 팀");
        playerManager.addPlayer("최세인(🔴)", Player.COLOR_RED, "DS 팀");
        playerManager.addPlayer("이정우(🟢)", Player.COLOR_GREEN, "VT 팀");
        playerManager.addPlayer("유아름(🟡)", Player.COLOR_YELLOW, "VT 팀");

// PlayerManager에서 플레이어 목록 가져오기
        List<Player> players = playerManager.getAllPlayers();

// players 리스트가 올바르게 초기화되었는지 확인
        if (players.size() >= 4) {
            players.get(0).setStar(1);
            players.get(1).setStar(1);
            players.get(2).setStar(1);
            players.get(3).setStar(1);
            players.get(0).setPoint(10);
            players.get(1).setPoint(10);
            players.get(2).setPoint(10);
            players.get(3).setPoint(10);
        } else {
            Log.e("PlayerError", "Player list is not properly initialized or has less than 4 players");
        }

        // 플레이어 상태 텍스트뷰 연결
        playerStars = new TextView[]{
                findViewById(R.id.Player1_stars),
                findViewById(R.id.Player2_stars),
                findViewById(R.id.Player3_stars),
                findViewById(R.id.Player4_stars)
        };

        playerCoins = new TextView[]{
                findViewById(R.id.Player1_coins),
                findViewById(R.id.Player2_coins),
                findViewById(R.id.Player3_coins),
                findViewById(R.id.Player4_coins)
        };

        playerNames = new TextView[]{
                findViewById(R.id.Player1_text),
                findViewById(R.id.Player2_text),
                findViewById(R.id.Player3_text),
                findViewById(R.id.Player4_text)
        };

// **boardButtons 배열 초기화 추가**
        boardButtons = new Button[boardSize];
        // 보드 생성
        createBoard();

        // 주사위 버튼 리스너
        rollDiceButton.setOnClickListener(v -> rollDice());
    }

    private void createBoard() {
        boardButtons = new Button[boardSize];

        int[][] outerCells = {
                {0, 0}, {0, 1}, {0, 2}, {0, 3}, {0, 4}, {0, 5},
                {1, 5}, {2, 5}, {3, 5}, {4, 5},
                {5, 5}, {5, 4}, {5, 3}, {5, 2}, {5, 1}, {5, 0},
                {4, 0}, {3, 0}, {2, 0}, {1, 0}
        };

        for (int i = 0; i < boardSize; i++) {
            int row = outerCells[i][0];
            int col = outerCells[i][1];

            Button button = new Button(this);
            button.setText(boardSpaces[i]);
            button.setBackgroundColor(Color.LTGRAY);
            button.setEnabled(false);

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = 0;
            params.columnSpec = GridLayout.spec(col, 1f);
            params.rowSpec = GridLayout.spec(row, 1f);
            button.setLayoutParams(params);

            boardGrid.addView(button);
            boardButtons[i] = button;
        }

        updatePosition();
    }


    private void rollDice() {
        Player currentPlayer = playerManager.getCurrentPlayer();
        if (currentPlayer.isHuehakCount()) {
            enqueueAlert("휴학 상태", currentPlayer.getName() + "이(가) 휴학 상태여서 이번 턴을 건너뜁니다.", () -> {
                currentPlayer.setHuehakCount(false);
                nextTurn(); // 다음 턴으로 이동
            });
            return;
        }

        // 사운드 효과 추가
        if (soundManager != null) {
            soundManager.playDiceSound(); // 주사위 소리 재생
        }
        starAnimationManager.startAnimation();

        Random random = new Random();
        int diceRoll = random.nextInt(6) + 1; // 주사위 굴리기
        int newPosition = (currentPlayer.getPosition() + diceRoll) % boardSize;

        String space = boardSpaces[newPosition];
        currentPlayer.setPosition(newPosition);

        enqueueAlert("주사위 결과", currentPlayer.getName() + "가 " + diceRoll + "칸 이동하여 " + space + "에 도착했습니다.", () -> {
            if ("스타".equals(space)) {
                boolean starGained = currentPlayer.gainStar();  // 한 번만 호출
                if (starGained) {
                    enqueueAlert("학년 획득", currentPlayer.getName() + "가 한 학년 올라갔습니다!", null);
                } else {
                    enqueueAlert("학년 획득 실패", currentPlayer.getName() + "의 학점이 부족해 학년이 올라가지 않았습니다.", null);
                }
            } else if ("휴학".equals(space)) {
                 enqueueAlert("휴학 신청", currentPlayer.getName() + "가 휴학을 신청했습니다. 다음 라운드는 주사위를 굴리지 못 합니다.", null);
                 currentPlayer.setHuehakCount(true);
            } else if ("컴포즈커피".equals(space)) {
                enqueueAlert("컴포즈커피", currentPlayer.getName() + "가 컴포즈커피에서 쉬면서 5학점을 얻었습니다.", null);
            } else if ("공지사항".equals(space)) {
                applyNoticeEffect(currentPlayer);
            } else if ("과제폭탄".equals(space)) {
                Koopa.activateKoopaEvent(currentPlayer, players, this);
            } else if ("인문과학자료실".equals(space)) {
                currentPlayer.setPoint(currentPlayer.getPoint() + 5);
                enqueueAlert("인문과학자료실", currentPlayer.getName() + "가 인자실에서 쉬면서 5학점을 얻었습니다.", null);
            } else if ("집현전".equals(space)) {
                currentPlayer.setPoint(currentPlayer.getPoint() + 5);
                enqueueAlert("집현전", currentPlayer.getName() + "가 집현전에서 쉬면서 5학점을 얻었습니다.", null);
            } else if ("정문".equals(space)) {
                if (currentPlayer.getPoint() >= 30){
                    currentPlayer.setStar(currentPlayer.getStar() + 1);
                    currentPlayer.setPoint(0);
                    enqueueAlert("정문", currentPlayer.getName() + "가 정문에 도착했군요! 30학점 이상을 모았으니 모든 학점을 소모시키고 특별히 한 학년을 드리겠습니다.", null);
                }
                else {
                    enqueueAlert("정문", currentPlayer.getName() + "가 정문에 도착했군요! 30학점 이상을 모아오면 좋은 일이?", null);
                }
                } else if ("함지마루".equals(space)) {
                currentPlayer.setPoint(currentPlayer.getPoint() + 5);
                enqueueAlert("함지마루", currentPlayer.getName() + "가 함지마루에서 밥을 먹으면서 5학점을 얻었습니다.", null);
            } else if ("오픈열람실".equals(space)) {
                currentPlayer.setPoint(currentPlayer.getPoint() + 5);
                enqueueAlert("오픈열람실", currentPlayer.getName() + "가 오픈열람실에서 쉬면서 5학점을 얻었습니다.", null);
            } else if ("노천극장".equals(space)) {
                currentPlayer.setPoint(currentPlayer.getPoint() - 5);
                enqueueAlert("노천극장", currentPlayer.getName() + "가 노천극장에서 공연을 보면서 5학점을 잃었습니다.", null);
            } else if ("연구실".equals(space)) {
                if ("DS 팀".equals(currentPlayer.getTeam())) {
                    currentPlayer.setPoint(currentPlayer.getPoint() * 2);
                    enqueueAlert("연구실", currentPlayer.getName() + "는 DS라 연구실에서 많은걸 배웠습니다! 학점이 2배가 되었습니다.", null);}
                else {
                currentPlayer.setPoint(currentPlayer.getPoint() - 5);
                enqueueAlert("집현전", currentPlayer.getName() + "는 VT라 연구실에서 쉴 수가 없군요! 5학점을 잃었습니다.", null);}
            } else if ("광운스퀘어".equals(space)) {
                currentPlayer.setPoint(currentPlayer.getPoint() + 5);
                enqueueAlert("광운스퀘어", currentPlayer.getName() + "가 광운스퀘어에서 쉬면서 5학점을 얻었습니다.", null);
            }  else if ("정융과방".equals(space)) {
                if ("VT 팀".equals(currentPlayer.getTeam())) {
                    currentPlayer.setPoint(currentPlayer.getPoint() * 2);
                    enqueueAlert("정융과방", currentPlayer.getName() + "는 VT라 선배들이 있는 정융과방에서 많은걸 배웠습니다! 학점이 2배가 되었습니다.", null);}
                else {
                    currentPlayer.setPoint(currentPlayer.getPoint() - 5);
                    if (currentPlayer.getPoint() == 0)
                    { enqueueAlert("정융과방", currentPlayer.getName() + "는 DS라 연구실에서 나갈 수가 없군요! 5학점을 잃었습니다.", null);} }
            } else if ("융디랩".equals(space)) {
                currentPlayer.setPoint(currentPlayer.getPoint() + 5);
                enqueueAlert("융디랩", currentPlayer.getName() + "가 융디랩에서 쉬면서 5학점을 얻었습니다.", null);
            } else if ("코딩컨설팅룸".equals(space)) {
                currentPlayer.setPoint(currentPlayer.getPoint() - 5);
                enqueueAlert("코딩컨설팅룸", currentPlayer.getName() + "가 코딩컨설팅룸에서 너무 쉬어서 5학점을 잃었습니다.", null);
            } else if ("새빛관 104호".equals(space)) {
                currentPlayer.setPoint(currentPlayer.getPoint() + 10);
                enqueueAlert("새빛관 104호", currentPlayer.getName() + "가 새빛관 104호에서 수업을 들으면서 10학점을 얻었습니다.", null);
            } else if ("풋살장".equals(space)) {
                currentPlayer.setPoint(currentPlayer.getPoint() - 5);
                enqueueAlert("풋살장", currentPlayer.getName() + "가 풋살장에서 축구를 하면서 5학점을 잃었습니다.", null);
            } else if ("AI카페".equals(space)) {
                currentPlayer.setPoint(currentPlayer.getPoint() + 10);
                enqueueAlert("AI카페", currentPlayer.getName() + "가 AI카페에서 쉬면서 10학점을 얻었습니다.", null);
            } else if ("해동열람실".equals(space)) {
                currentPlayer.setPoint(currentPlayer.getPoint() + 5);
                enqueueAlert("해동열람실", currentPlayer.getName() + "가 해동열람실에서 쉬면서 5학점을 얻었습니다.", null);
            }

            updatePosition();
            updatePlayerInfo();
            nextTurn(); // 다음 턴으로 이동
        });
    }

//    private String[] boardSpaces = {
//            "정문", "인문과학자료실", "컴포즈커피", "오픈열람실", "집현전", "휴학",
//            "노천극장", "함지마루", "연구실", "광운스퀘어",
//            "공지사항", "정융과방", "융디랩", "코딩컨설팅룸", "새빛관 104호", "스타",
//            "과제폭탄", "풋살장", "AI카페", "해동열람실"
//    };

    // 모든 플레이어가 주사위를 던진 후 미니 게임 오버레이 표시
    private void updatePosition() {
        for (int i = 0; i < boardSize; i++) {
            Button button = boardButtons[i];
            StringBuilder playersAtThisPosition = new StringBuilder();

            for (Player player : playerManager.getAllPlayers()) {
                if (player.getPosition() == i) {
                    String playerIcon = "🔵";
                    if (player.getColor() == Player.COLOR_BLUE) {
                        playerIcon = "🔵";
                    } else if (player.getColor() == Player.COLOR_RED) {
                        playerIcon = "🔴";
                    } else if (player.getColor() == Player.COLOR_GREEN) {
                        playerIcon = "🟢";
                    } else if (player.getColor() == Player.COLOR_YELLOW) {
                        playerIcon = "🟡";
                    }
                    playersAtThisPosition.append(playerIcon).append(" ");
                }
            }

            button.setText(playersAtThisPosition.toString() + boardSpaces[i]);
            button.setBackgroundColor(Color.LTGRAY);
            button.setTextColor(Color.BLACK);
            button.setBackgroundResource(R.drawable.grid);
        }

        Player currentPlayer = playerManager.getCurrentPlayer();
        String currentSpace = boardSpaces[currentPlayer.getPosition()];
        currentPositionText.setText("현재 위치: " + currentSpace);
    }

    private void applyNoticeEffect(Player currentPlayer) {
        Random random = new Random();
        int effect = random.nextInt(5); // 0부터 4까지 랜덤 선택 (5가지 효과)

        enqueueAlert("공지사항", currentPlayer.getName() + "가 공지사항을 받았습니다!", () -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("공지사항");

            switch (effect) {
                case 0: // 효과 1: Point +10
                    currentPlayer.setPoint(currentPlayer.getPoint() + 10);
                    builder.setMessage(currentPlayer.getName() + "가 10학점을 얻었습니다!")
                            .setPositiveButton("확인", (dialog, which) -> showNextAlert());
                    break;

                case 1: // 효과 2: 다른 플레이어 선택 후 Point -5
                    builder.setMessage("다른 플레이어의 5학점을 훔칩니다.")
                            .setPositiveButton("확인", (dialog, which) -> selectPlayerToSteal(currentPlayer, () -> showNextAlert()));
                    break;

                case 2: // 효과 3: Point 절반 감소
                    int halvedPoints = currentPlayer.getPoint() / 2;
                    currentPlayer.setPoint(halvedPoints);
                    builder.setMessage(currentPlayer.getName() + "의 학점이 절반인 " + halvedPoints + "으로 감소합니다.")
                            .setPositiveButton("확인", (dialog, which) -> showNextAlert());
                    break;

                case 3: // 효과 4: 랜덤 칸 이동
                    int randomPosition = random.nextInt(boardSize);
                    currentPlayer.setPosition(randomPosition);
                    String randomSpace = boardSpaces[randomPosition];
                    builder.setMessage(currentPlayer.getName() + "가 랜덤으로 " + randomSpace + "로 이동되었습니다.")
                            .setPositiveButton("확인", (dialog, which) -> showNextAlert());
                    break;

                case 4: // 효과 5: CountNumber 게임 시작
                    builder.setMessage("CountNumber 게임을 시작합니다.")
                            .setPositiveButton("확인", (dialog, which) -> {
                                Intent intent = new Intent(this, CountNumber.class);
                                startActivityForResult(intent, REQUEST_COUNT_NUMBER);
                            });
                    break;
            }

            builder.setCancelable(false); // 알림이 종료될 때까지 취소 불가
            builder.show();
        });

        // 공지사항 처리가 끝난 뒤 미니게임 실행 등록
        enqueueAlert("공지사항 종료", "모든 공지사항 이벤트가 완료되었습니다.", this::showMiniGameOverlayOrSecondMinigame);
    }




    private void selectPlayerToSteal(Player currentPlayer, Runnable onDismiss) {
        List<Player> allPlayers = playerManager.getAllPlayers();
        CharSequence[] playerNames = new CharSequence[allPlayers.size() - 1];
        int index = 0;

        for (Player player : allPlayers) {
            if (!player.getName().equals(currentPlayer.getName())) {
                playerNames[index++] = player.getName();
            }
        }

        new AlertDialog.Builder(this)
                .setTitle("5학점을 훔칠 플레이어를 선택하세요")
                .setItems(playerNames, (dialog, which) -> {
                    // 선택한 플레이어의 이름 가져오기
                    String selectedPlayerName = playerNames[which].toString();
                    Player selectedPlayer = playerManager.getPlayerByName(selectedPlayerName);
                    if (selectedPlayer != null) {
                        if (selectedPlayer.getPoint() >= 5) {
                            selectedPlayer.setPoint(selectedPlayer.getPoint() - 5);
                            currentPlayer.setPoint(currentPlayer.getPoint() + 5);
                            Toast.makeText(this, currentPlayer.getName() + "가 5학점을 훔쳤습니다! " + selectedPlayer.getName(), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, selectedPlayer.getName() + " 훔칠 학점이 없습니다.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Player not found.", Toast.LENGTH_SHORT).show();
                    }
                    updatePlayerInfo();
                    if (onDismiss != null) onDismiss.run(); // 완료 후 다음 알림 표시
                })
                .setNegativeButton("취소", (dialog, which) -> {
                    if (onDismiss != null) onDismiss.run(); // 취소 후 다음 알림 표시
                })
                .setCancelable(false)
                .show();
    }



    private void updatePlayerInfo() {
        // 플레이어 정보를 업데이트
        List<Player> allPlayers = playerManager.getAllPlayers();
        for (int i = 0; i < allPlayers.size(); i++) {
            Player player = allPlayers.get(i);

            // 플레이어 이름, 학점, 학년 업데이트
            playerNames[i].setText(player.getName());
            playerCoins[i].setText("학점: " + player.getPoint());
            playerStars[i].setText("학년: " + player.getStar());
        }

        // 현재 플레이어 표시
        Player currentPlayer = playerManager.getCurrentPlayer();
        currentPlayerText.setText("현재 순서: " + currentPlayer.getName() + " (" + currentPlayer.getTeam() + ")");
    }

    // 게임 초기화 메서드
    private void resetGame() {
        roundCount = 0; // 라운드 초기화
        playerManager.resetGame(); // 플레이어 상태 초기화
        boardGrid.removeAllViews(); // 기존 보드 제거
        createBoard(); // 보드 재생성
        updatePlayerInfo(); // UI 갱신
    }




    private void showMiniGameOverlayOrSecondMinigame() {
        enqueueAlert("미니 게임", "한 라운드가 끝났습니다. 미니게임을 시작하시겠습니까?", () -> {
            Random random = new Random();
            int choice = random.nextInt(2); // 0 또는 1을 랜덤으로 선택
            if (choice == 0) {
                showMiniGameOverlay(); // MiniGameOverlay 실행
            } else {
                startSecondMinigame(); // SecondMinigame 실행
            }
        });
    }





    private void showMiniGameOverlay() {
        FrameLayout overlayContainer = findViewById(R.id.overlay_container);
        overlayContainer.setVisibility(View.VISIBLE); // 오버레이 보이기

        MiniGameOverlay miniGameOverlay = new MiniGameOverlay();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.overlay_container, miniGameOverlay)
                .commit();
    }

    private void startSecondMinigame() {
        Intent intent = new Intent(this, SecondMinigame.class);
        startActivityForResult(intent, REQUEST_COUNT_NUMBER); // SecondMinigame 실행
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_COUNT_NUMBER) {
            if (resultCode == RESULT_OK && data != null) {
                String winningTeam = data.getStringExtra("WINNING_TEAM");

                if (winningTeam != null) {
                    if (winningTeam.equals("DS 팀")) {
                        playerManager.addPointsToTeam("DS 팀", 5); // DS 팀에 학점 부여
                    } else if (winningTeam.equals("VT 팀")) {
                        playerManager.addPointsToTeam("VT 팀", 5); // VT 팀에 학점 부여
                    }

                    // 결과 알림 띄우기
                    String alertMessage = winningTeam.equals("DRAW")
                            ? "미니게임 결과: 무승부입니다!"
                            : winningTeam + "이(가) 미니게임에서 승리하고 5 학점을 얻었습니다!";

                    enqueueAlert("미니게임 결과", alertMessage, null);
                }

                updatePlayerInfo(); // 플레이어 정보 업데이트
            }

            // 라운드 종료 로직
            roundCount++;
            if (roundCount > MAX_ROUNDS) {
                endGame(); // 라운드 초과 시 게임 종료
                return;
            }

            enqueueAlert("라운드 종료", roundCount + " 라운드가 종료되었습니다.\n" + (roundCount + 1) + " 라운드가 시작됩니다.", () -> {
                boardGrid.removeAllViews(); // 기존 보드 제거
                createBoard(); // 새 보드 생성
                updatePlayerInfo(); // UI 갱신
            });
        } else if (resultCode == RESULT_CANCELED) {
            enqueueAlert("미니게임 실패", "미니게임에서 실패하였습니다. 게임을 계속 진행합니다.", null);
        }
    }



    @Override
    protected void onPause() {
        super.onPause();
        FrameLayout overlayContainer = findViewById(R.id.overlay_container);
        overlayContainer.setVisibility(View.GONE); // 오버레이 숨기기
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (boardGrid.getChildCount() == 0) {
            createBoard();
        }
        updatePlayerInfo();
    }

    @Override
    public void onBackPressed() {
        FrameLayout overlayContainer = findViewById(R.id.overlay_container);
        if (overlayContainer.getVisibility() == View.VISIBLE) {
            overlayContainer.setVisibility(View.GONE);
            getSupportFragmentManager().popBackStack(); // Fragment 제거
        } else {
            super.onBackPressed(); // 기본 동작
        }
    }


    @Override
    public void onMiniGameFinished(Bundle resultData) {
        FrameLayout overlayContainer = findViewById(R.id.overlay_container);
        overlayContainer.setVisibility(View.GONE); // 오버레이 숨기기

        // 학점 부여 및 결과 알림 로직
        if (resultData != null) {
            boolean dsSuccess = resultData.getBoolean("DS_SUCCESS", false);
            boolean vtSuccess = resultData.getBoolean("VT_SUCCESS", false);

            int dsPoints = dsSuccess ? 5 : 0; // DS 팀 성공 시 5점
            int vtPoints = vtSuccess ? 5 : 0; // VT 팀 성공 시 5점

            StringBuilder resultMessage = new StringBuilder("미니게임 결과:\n");
            if (dsSuccess) {
                resultMessage.append("DS 팀이 성공하여 ").append(dsPoints).append(" 학점을 얻었습니다!\n");
            } else {
                resultMessage.append("DS 팀이 실패했습니다.\n");
            }
            if (vtSuccess) {
                resultMessage.append("VT 팀이 성공하여 ").append(vtPoints).append(" 학점을 얻었습니다!");
            } else {
                resultMessage.append("VT 팀이 실패했습니다.");
            }

            // 학점 반영
            if (dsPoints > 0) {
                playerManager.addPointsToTeam("DS 팀", dsPoints);
            }
            if (vtPoints > 0) {
                playerManager.addPointsToTeam("VT 팀", vtPoints);
            }

            updatePlayerInfo(); // UI 업데이트

            // 결과 알림 -> 미니게임 종료 알림 -> 라운드 종료 알림
            enqueueAlert("미니게임 결과", resultMessage.toString(), () -> {
                roundCount++; // 라운드 증가
                if (roundCount >= MAX_ROUNDS) {
                    endGame(); // 라운드가 최대 라운드에 도달하면 게임 종료
                    return;
                }

                enqueueAlert("미니게임 종료", "미니게임이 종료되었습니다. 게임을 계속 진행합니다.", () -> {
                    enqueueAlert("라운드 종료", roundCount + " 라운드가 종료되었습니다.\n" + (roundCount + 1) + " 라운드가 시작됩니다.", () -> {
                        // 보드 초기화 및 재생성
                        boardGrid.removeAllViews(); // 기존 보드 제거
                        createBoard(); // 새 보드 생성
                        updatePlayerInfo(); // 플레이어 정보 업데이트
                    });
                });
            });
        }
    }





    private void nextTurn() {
        if (roundCount >= MAX_ROUNDS) {
            endGame(); // 현재 라운드가 최대 라운드를 초과했으면 즉시 게임 종료
            return;
        }

        playerManager.getNextPlayer(); // 다음 플레이어로 이동
        updatePlayerInfo();

        // 모든 플레이어가 한 턴을 완료했는지 확인
        if (playerManager.getCurrentPlayer() == playerManager.getAllPlayers().get(0)) {
            showMiniGameOverlayOrSecondMinigame(); // 미니게임 실행
        }
    }


    private void endGame() {
        // 팀별 학점 및 학년 합산
        int dsStars = 0, dsPoints = 0;
        int vtStars = 0, vtPoints = 0;

        for (Player player : playerManager.getAllPlayers()) {
            if ("DS 팀".equals(player.getTeam())) {
                dsStars += player.getStar();
                dsPoints += player.getPoint();
            } else if ("VT 팀".equals(player.getTeam())) {
                vtStars += player.getStar();
                vtPoints += player.getPoint();
            }
        }

        String winnerMessage;
        if (dsStars > vtStars) {
            winnerMessage = "DS 팀이 승리했습니다! 🎉\n(학년: " + dsStars + ", 학점: " + dsPoints + ")";
        } else if (vtStars > dsStars) {
            winnerMessage = "VT 팀이 승리했습니다! 🎉\n(학년: " + vtStars + ", 학점: " + vtPoints + ")";
        } else {
            if (dsPoints > vtPoints) {
                winnerMessage = "DS 팀이 승리했습니다! 🎉\n(학년: " + dsStars + ", 학점: " + dsPoints + ")";
            } else if (vtPoints > dsPoints) {
                winnerMessage = "VT 팀이 승리했습니다! 🎉\n(학년: " + vtStars + ", 학점: " + vtPoints + ")";
            } else {
                winnerMessage = "무승부입니다! 😲\n(학년: " + dsStars + ", 학점: " + dsPoints + ")";
            }
        }

        // 게임 종료 메시지 및 리셋 여부 확인
        new AlertDialog.Builder(this)
                .setTitle("게임 종료")
                .setMessage(winnerMessage)
                .setPositiveButton("게임 리셋", (dialog, which) -> resetGame())
                .setNegativeButton("종료", (dialog, which) -> finish())
                .setCancelable(false)
                .show();
    }



    // 특정 팀의 모든 멤버를 가져오는 메서드 (PlayerManager의 메서드 활용)
    public List<Player> getPlayersByTeam(String teamName) {
        List<Player> teamPlayers = new ArrayList<>();
        for (Player player : playerManager.getAllPlayers()) { // getAllPlayers() 메서드 사용
            if (player.getTeam().equals(teamName)) {
                teamPlayers.add(player);
            }
        }
        return teamPlayers;
    }



    private void showNextAlert() {
        if (isAlertShowing || alertQueue.isEmpty()) return; // 알림이 표시 중이거나 큐가 비어있으면 반환

        isAlertShowing = true; // 알림 표시 중으로 설정
        Runnable nextAlert = alertQueue.poll(); // 큐에서 다음 알림 가져오기
        if (nextAlert != null) {
            nextAlert.run(); // 알림 실행
        }
    }

    private void enqueueAlert(String title, String message, Runnable onDismiss) {
        alertQueue.add(() -> {
            new AlertDialog.Builder(this)
                    .setTitle(title)
                    .setMessage(message)
                    .setPositiveButton("확인", (dialog, which) -> {
                        isAlertShowing = false; // 알림 표시 종료
                        if (onDismiss != null) {
                            onDismiss.run(); // 알림 종료 후 실행할 작업
                        }
                        showNextAlert(); // 다음 알림 표시
                    })
                    .setCancelable(false)
                    .show();
        });

        showNextAlert(); // 알림 표시 시도
    }
}