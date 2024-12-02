package com.cookandroid.project4_1;

import java.util.ArrayList;
import java.util.List;

public class PlayerManager {
    private static PlayerManager instance;
    private final List<Player> playerList;
    private int[] turnOrder = {0, 2, 1, 3}; // Player1 → Player3 → Player2 → Player4
    private int currentPlayerTurnIndex;

    private PlayerManager() {
        playerList = new ArrayList<>();
        currentPlayerTurnIndex = 0;
    }

    public static PlayerManager getInstance() {
        if (instance == null) {
            instance = new PlayerManager();
        }
        return instance;
    }

    public void addPlayer(String name, int color, String team) {
        playerList.add(new Player(name, color, team));
    }

    public List<Player> getAllPlayers() {
        return new ArrayList<>(playerList);
    }

    public Player getCurrentPlayer() {
        return playerList.get(turnOrder[currentPlayerTurnIndex]);
    }

    public Player getNextPlayer() {
        currentPlayerTurnIndex = (currentPlayerTurnIndex + 1) % turnOrder.length;
        return getCurrentPlayer();
    }

    public Player getPlayerByName(String name) {
        for (Player player : playerList) {
            if (player.getName().equals(name)) {
                return player;
            }
        }
        return null;
    }

    public void resetGame() {
        currentPlayerTurnIndex = 0; // 현재 순서 초기화
        for (Player player : playerList) {
            player.resetPlayer(); // 플레이어 상태 초기화
        }
    }

    // 특정 팀의 모든 멤버에게 학점을 부여하는 메서드
    public void addPointsToTeam(String teamName, int points) {
        for (Player player : playerList) {
            if (player.getTeam().equals(teamName)) {
                player.setPoint(player.getPoint() + points);
            }
        }
    }
}
