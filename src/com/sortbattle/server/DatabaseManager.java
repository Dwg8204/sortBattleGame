
package com.sortbattle.server;

import com.sortbattle.common.Player;
import com.sortbattle.common.PasswordUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:mysql://localhost:3307/sortbattle_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = ""; 
    
    private Connection connection;
    
    public DatabaseManager() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            this.connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            System.out.println("✓ Database connected successfully");
        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("✗ Database connection failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Đăng ký tài khoản mới
     */
    public boolean registerPlayer(String username, String password) {
        String sql = "INSERT INTO players (username, password) VALUES (?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, PasswordUtil.hashPassword(password));
            stmt.executeUpdate();
            System.out.println("✓ Registered new player: " + username);
            return true;
        } catch (SQLException e) {
            System.err.println("✗ Registration failed: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Đăng nhập - xác thực username + password
     */
    public Player authenticatePlayer(String username, String password) {
        String sql = "SELECT id, username, password, total_score, games_played, games_won FROM players WHERE username = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                String storedHash = rs.getString("password");
                
                if (PasswordUtil.checkPassword(password, storedHash)) {
                    Player player = new Player(username);
                    player.setTotalScore(rs.getInt("total_score"));
                    player.setGamesPlayed(rs.getInt("games_played"));
                    player.setGamesWon(rs.getInt("games_won"));
                    
                    updateLastLogin(username);
                    
                    System.out.println("✓ Player authenticated: " + username);
                    return player;
                } else {
                    System.out.println("✗ Wrong password for: " + username);
                }
            } else {
                System.out.println("✗ Username not found: " + username);
            }
        } catch (SQLException e) {
            System.err.println("✗ Authentication error: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Lấy thông tin player
     */
    public Player getPlayer(String username) {
        String sql = "SELECT username, total_score, games_played, games_won FROM players WHERE username = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                Player player = new Player(username);
                player.setTotalScore(rs.getInt("total_score"));
                player.setGamesPlayed(rs.getInt("games_played"));
                player.setGamesWon(rs.getInt("games_won"));
                return player;
            }
        } catch (SQLException e) {
            System.err.println("✗ Get player error: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Cập nhật điểm sau trận đấu
     */
    public void updatePlayerScore(String username, int scoreChange, boolean won) {
        String sql = "UPDATE players SET total_score = total_score + ?, games_played = games_played + 1, " +
                     "games_won = games_won + ? WHERE username = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, scoreChange);
            stmt.setInt(2, won ? 1 : 0);
            stmt.setString(3, username);
            stmt.executeUpdate();
            System.out.println(" Updated score for " + username + ": +" + scoreChange + " (won: " + won + ")");
        } catch (SQLException e) {
            System.err.println(" Update score error: " + e.getMessage());
        }
    }
    
    private void updateLastLogin(String username) {
        String sql = "UPDATE players SET last_login = CURRENT_TIMESTAMP WHERE username = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println(" Update last_login error: " + e.getMessage());
        }
    }
    
    /**
     * Lưu lịch sử trận đấu
     */
    public void saveMatchHistory(String player1, String player2, String winner, 
                                   int p1Score, int p2Score, String gameMode, 
                                   String sortOrder, int itemCount, int timeLimit, int duration) {
        String sql = "INSERT INTO match_history (player1_id, player2_id, winner_id, player1_score, player2_score, " +
                     "game_mode, sort_order, item_count, time_limit, match_duration) " +
                     "VALUES ((SELECT id FROM players WHERE username = ?), " +
                     "(SELECT id FROM players WHERE username = ?), " +
                     "(SELECT id FROM players WHERE username = ?), ?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, player1);
            stmt.setString(2, player2);
            stmt.setString(3, winner);
            stmt.setInt(4, p1Score);
            stmt.setInt(5, p2Score);
            stmt.setString(6, gameMode);
            stmt.setString(7, sortOrder);
            stmt.setInt(8, itemCount);
            stmt.setInt(9, timeLimit);
            stmt.setInt(10, duration);
            stmt.executeUpdate();
            System.out.println("Match history saved: " + player1 + " vs " + player2);
        } catch (SQLException e) {
            System.err.println("Save match history error: " + e.getMessage());
        }
    }
    
    /**
     * Lấy lịch sử trận đấu
     */
    public List<String> getMatchHistory(String username, int limit) {
        List<String> history = new ArrayList<>();
        String sql = "SELECT p1.username as player1, p2.username as player2, w.username as winner, " +
                     "player1_score, player2_score, game_mode, played_at " +
                     "FROM match_history m " +
                     "JOIN players p1 ON m.player1_id = p1.id " +
                     "JOIN players p2 ON m.player2_id = p2.id " +
                     "LEFT JOIN players w ON m.winner_id = w.id " +
                     "WHERE p1.username = ? OR p2.username = ? " +
                     "ORDER BY played_at DESC LIMIT ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, username);
            stmt.setInt(3, limit);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                String record = String.format("%s vs %s | %s | %d-%d | Winner: %s | %s",
                    rs.getString("player1"),
                    rs.getString("player2"),
                    rs.getString("game_mode"),
                    rs.getInt("player1_score"),
                    rs.getInt("player2_score"),
                    rs.getString("winner") != null ? rs.getString("winner") : "Draw",
                    rs.getTimestamp("played_at")
                );
                history.add(record);
            }
        } catch (SQLException e) {
            System.err.println("✗ Get match history error: " + e.getMessage());
        }
        return history;
    }
    
    /**
     * Lấy bảng xếp hạng
     */
    public List<String> getLeaderboard(int topN) {
        List<String> leaderboard = new ArrayList<>();
        String sql = "SELECT username, total_score, games_played, games_won " +
                     "FROM players ORDER BY total_score DESC LIMIT ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, topN);
            ResultSet rs = stmt.executeQuery();
            
            int rank = 1;
            while (rs.next()) {
                int played = rs.getInt("games_played");
                int won = rs.getInt("games_won");
                double winRate = played > 0 ? (won * 100.0 / played) : 0;
                
                String entry = String.format("#%d - %s: %d điểm | %d/%d thắng (%.1f%%)",
                    rank++,
                    rs.getString("username"),
                    rs.getInt("total_score"),
                    won,
                    played,
                    winRate
                );
                leaderboard.add(entry);
            }
        } catch (SQLException e) {
            System.err.println("✗ Get leaderboard error: " + e.getMessage());
        }
        return leaderboard;
    }
    
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("✓ Database connection closed");
            }
        } catch (SQLException e) {
            System.err.println("✗ Close connection error: " + e.getMessage());
        }
    }
}
