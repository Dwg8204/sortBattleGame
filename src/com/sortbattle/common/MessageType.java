// File: D:\admin\Nam4\LTM\ltmproject\src\com\sortbattle\common\MessageType.java

package com.sortbattle.common;

public enum MessageType {
    // Authentication
    LOGIN_REQUEST,
    LOGIN_SUCCESS,
    LOGIN_FAILURE,
    REGISTER_REQUEST,      // THÊM MỚI
    REGISTER_SUCCESS,      // THÊM MỚI
    REGISTER_FAILURE,      // THÊM MỚI
    LOGOUT_REQUEST,
    REMOTE_LOGOUT,
    OPPONENT_DISCONNECTED,
    
    // Lobby
    PLAYER_LIST_UPDATE,
    CHALLENGE_REQUEST,
    INCOMING_CHALLENGE,
    CHALLENGE_RESPONSE,
    CHALLENGE_ACCEPTED,
    CHALLENGE_REJECTED,
    
    // Game
    GAME_CONFIG_SUBMIT,
    GAME_START,
    PLAYER_CLICK,
    GAME_STATE_UPDATE,
    GAME_OVER,
    EXIT_GAME,
    
    // Rematch
    REQUEST_REMATCH,
    REMATCH_RESPONSE,
    REMATCH_ACCEPTED,
    REMATCH_REJECTED,
    REMATCH_REJECTED_SILENT,
    
    // Leaderboard & History - THÊM MỚI
    LEADERBOARD_REQUEST,
    LEADERBOARD_RESPONSE,
    HISTORY_REQUEST,
    HISTORY_RESPONSE
}