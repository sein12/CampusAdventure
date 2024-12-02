package com.cookandroid.project4_1;

import android.graphics.Color;
import android.widget.Toast;

import java.util.List;

public class Player {
    private String name;
    private int star;
    private int point;
    private int position;
    private boolean huehakCount;
    private int color;
    private String team; // 팀 추가

    public static final int COLOR_BLUE = Color.BLUE;
    public static final int COLOR_RED = Color.RED;
    public static final int COLOR_GREEN = Color.GREEN;
    public static final int COLOR_YELLOW = Color.YELLOW;

    public Player(String name, int color, String team) {
        this.name = name;
        this.color = color;
        this.team = team;
        this.star = 0;
        this.point = 0;
        this.position = 0;
        this.huehakCount = false;
    }

    public String getName() {
        return name;
    }

    public int getStar() {
        return star;
    }

    public void setStar(int star) {
        this.star = Math.max(star,1);
    }

    public int getPoint() {
        return point;
    }

    public void setPoint(int point) {
        this.point = Math.max(point, 0);
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public boolean isHuehakCount() {
        return huehakCount;
    }

    public void setHuehakCount(boolean huehakCount) {
        this.huehakCount = huehakCount;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public String getTeam() {
        return team;
    }

    public void resetPlayer() {
        this.star = 0;
        this.point = 0;
        this.position = 0;
        this.huehakCount = false;
    }

    public boolean gainStar() {
        if (this.point >= 10) {
            this.star++;
            this.point -= 10;
            return true;
        }
        return false;
    }

    public void goOnHuehak() {
        this.huehakCount = true;
    }
}
