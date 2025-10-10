/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sortbattle.client.model;

/**
 *
 * @author admin
 */

import com.sortbattle.common.GameConfig;
import com.sortbattle.common.Player;

import java.util.ArrayList;
import java.util.List;

public class ClientModel {
    private Player currentPlayer;
    private List<Player> onlinePlayers;
    
    // Trạng thái trận đấu hiện tại
    private GameConfig currentGameConfig;
    private List<String> gameItems;
    private Player opponent;

    public ClientModel() {
        this.onlinePlayers = new ArrayList<>();
    }
    
    // Getters and Setters
    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    public void setCurrentPlayer(Player currentPlayer) {
        this.currentPlayer = currentPlayer;
    }

    public List<Player> getOnlinePlayers() {
        return onlinePlayers;
    }

    public void setOnlinePlayers(List<Player> onlinePlayers) {
        this.onlinePlayers = onlinePlayers;
    }
    
    public void setCurrentGame(GameConfig config, List<String> items, Player opponent) {
        this.currentGameConfig = config;
        this.gameItems = items;
        this.opponent = opponent;
    }

    public GameConfig getCurrentGameConfig() {
        return currentGameConfig;
    }

    public List<String> getGameItems() {
        return gameItems;
    }

    public Player getOpponent() {
        return opponent;
    }
}
