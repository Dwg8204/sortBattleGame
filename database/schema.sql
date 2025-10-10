-- File: D:\admin\Nam4\LTM\ltmproject\database\schema.sql

CREATE DATABASE IF NOT EXISTS sortbattle_db 
CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE sortbattle_db;

-- Bảng người chơi
CREATE TABLE players (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(32) NOT NULL,  -- MD5 hash = 32 ký tự
    total_score INT DEFAULT 0,
    games_played INT DEFAULT 0,
    games_won INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP NULL,
    INDEX idx_username (username),
    INDEX idx_total_score (total_score DESC)
);

-- Bảng lịch sử trận đấu
CREATE TABLE match_history (
    id INT AUTO_INCREMENT PRIMARY KEY,
    player1_id INT NOT NULL,
    player2_id INT NOT NULL,
    winner_id INT NULL,
    player1_score INT DEFAULT 0,
    player2_score INT DEFAULT 0,
    game_mode ENUM('NUMBER', 'WORD') NOT NULL,
    sort_order ENUM('ASCENDING', 'DESCENDING') NOT NULL,
    item_count INT NOT NULL,
    time_limit INT NOT NULL,
    match_duration INT,
    played_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (player1_id) REFERENCES players(id) ON DELETE CASCADE,
    FOREIGN KEY (player2_id) REFERENCES players(id) ON DELETE CASCADE,
    FOREIGN KEY (winner_id) REFERENCES players(id) ON DELETE SET NULL,
    INDEX idx_player1 (player1_id),
    INDEX idx_player2 (player2_id),
    INDEX idx_played_at (played_at DESC)
);

-- Insert dữ liệu test
-- Password: 123456 → MD5 hash: e10adc3949ba59abbe56e057f20f883e
INSERT INTO players (username, password, total_score, games_played, games_won) VALUES
('player1', 'e10adc3949ba59abbe56e057f20f883e', 90, 10, 6),
('player2', 'e10adc3949ba59abbe56e057f20f883e', 75, 8, 4),
('player3', 'e10adc3949ba59abbe56e057f20f883e', 0, 0, 0),
('player4', 'e10adc3949ba59abbe56e057f20f883e', 50, 5, 2);