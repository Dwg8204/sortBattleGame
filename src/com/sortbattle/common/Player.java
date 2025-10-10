package com.sortbattle.common;

import java.io.Serializable;

public class Player implements Serializable {
    private static final long serialVersionUID = 1L;  // THÊM MỚI
    
    private String username;
    private int totalScore;
    private int gamesPlayed;
    private int gamesWon;
    private PlayerStatus status;

    public enum PlayerStatus {
        IDLE, BUSY
    }

    public Player(String username) {
        this.username = username;
        this.totalScore = 0;
        this.gamesPlayed = 0;
        this.gamesWon = 0;
        this.status = PlayerStatus.IDLE;
    }

    // Getters and Setters
    public String getUsername() {
        return username;
    }

    public int getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(int totalScore) {
        this.totalScore = totalScore;
    }

    public int getGamesPlayed() {
        return gamesPlayed;
    }

    public void setGamesPlayed(int gamesPlayed) {
        this.gamesPlayed = gamesPlayed;
    }

    public int getGamesWon() {
        return gamesWon;
    }

    public void setGamesWon(int gamesWon) {
        this.gamesWon = gamesWon;
    }

    public PlayerStatus getStatus() {
        return status;
    }

    public void setStatus(PlayerStatus status) {
        this.status = status;
    }

    // ========== THÊM 2 METHODS MỚI ==========
    
    /**
     * Cộng điểm (không lưu vào database, chỉ update local)
     */
    public void addScore(int points) {
        this.totalScore += points;
    }

    /**
     * Trừ điểm (không lưu vào database, chỉ update local)
     */
    public void subtractScore(int points) {
        this.totalScore = Math.max(0, this.totalScore - points);  // Không cho âm
    }
    
    // ========== HẾT PHẦN THÊM ==========

    @Override
    public String toString() {
        return username + " (" + totalScore + " điểm) - " + (status == PlayerStatus.IDLE ? "Rảnh" : "Bận");
    }
}