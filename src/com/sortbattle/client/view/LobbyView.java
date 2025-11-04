/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sortbattle.client.view;

/**
 *
 * @author admin
 */

import com.sortbattle.client.controller.ClientController;
import com.sortbattle.common.GameConfig;
import com.sortbattle.common.Player;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import java.awt.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

public class LobbyView extends JFrame {
    private final ClientController controller;
    private final JLabel welcomeLabel;
    private final JList<Player> playerList;
    private final DefaultListModel<Player> playerListModel;
    private final JButton challengeButton;

    public LobbyView(ClientController controller) {
        this.controller = controller;

        setTitle("Sảnh chờ - Sort Battle Online");
        setSize(500, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Panel chính
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Panel phía trên (Welcome)
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        welcomeLabel = new JLabel("Chào mừng!");
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 16));
        topPanel.add(welcomeLabel);
        mainPanel.add(topPanel, BorderLayout.NORTH);

        // Panel trung tâm (Danh sách người chơi)
        playerListModel = new DefaultListModel<>();
        playerList = new JList<>(playerListModel);
        playerList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        playerList.setFont(new Font("Arial", Font.PLAIN, 14));
        // Custom renderer để hiển thị màu sắc theo trạng thái
        playerList.setCellRenderer(new PlayerListRenderer());
        JScrollPane scrollPane = new JScrollPane(playerList);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Panel phía dưới (Nút thách đấu)
        JPanel bottomPanel = new JPanel();
        challengeButton = new JButton("Thách đấu");
        challengeButton.setFont(new Font("Arial", Font.BOLD, 14));
        challengeButton.addActionListener(e -> {
            Player selectedPlayer = playerList.getSelectedValue();
            if (selectedPlayer != null) {
                if (selectedPlayer.getStatus() == Player.PlayerStatus.IDLE) {
                    controller.challengePlayer(selectedPlayer.getUsername());
                } else {
                    JOptionPane.showMessageDialog(this, "Người chơi này đang bận!", "Không thể thách đấu", JOptionPane.WARNING_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn một người chơi để thách đấu.", "Chưa chọn đối thủ", JOptionPane.WARNING_MESSAGE);
            }
        });
        bottomPanel.add(challengeButton);

        
        JButton leaderboardButton = new JButton("🏆 Bảng xếp hạng");
        leaderboardButton.setFont(new Font("Arial", Font.BOLD, 12));
        leaderboardButton.addActionListener(e -> controller.requestLeaderboard());
        bottomPanel.add(leaderboardButton);

        JButton historyButton = new JButton("📜 Lịch sử");
        historyButton.setFont(new Font("Arial", Font.BOLD, 12));
        historyButton.addActionListener(e -> controller.requestHistory());
        bottomPanel.add(historyButton);
        
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        add(mainPanel);
    }
    
    public void updateWelcomeMessage(String username) {
        welcomeLabel.setText("Chào mừng, " + username + "!");
    }
    
    public void updatePlayerList(List<Player> players) {
        playerListModel.clear();
        players.forEach(playerListModel::addElement);
    }
    
    public void showGameConfigDialog(String opponentName) {
        // Tạo dialog
        JDialog configDialog = new JDialog(this, "Cấu hình trận đấu với " + opponentName, true);
        configDialog.setSize(400, 350);
        configDialog.setLocationRelativeTo(this);
        configDialog.setLayout(new GridLayout(6, 2, 10, 10));
        configDialog.getRootPane().setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        GameConfig config = new GameConfig(); // Lấy config mặc định

        // 1. Loại nội dung
        configDialog.add(new JLabel("Loại nội dung:"));
        JComboBox<String> typeCombo = new JComboBox<>(new String[]{"Từ", "Số"});
        typeCombo.addActionListener(e -> config.setDataType("Số".equals(typeCombo.getSelectedItem()) ? GameConfig.DataType.NUMBER : GameConfig.DataType.WORD));
        configDialog.add(typeCombo);

        // 2. Số lượng/Khoảng số
        configDialog.add(new JLabel("Số lượng/Khoảng:"));
        JComboBox<Integer> countCombo = new JComboBox<>(new Integer[]{10, 20, 30, 40, 50, 60, 80, 100});
        countCombo.setSelectedItem(30); // Default for words
        typeCombo.addActionListener(e -> {
            if ("Số".equals(typeCombo.getSelectedItem())) {
                countCombo.setModel(new DefaultComboBoxModel<>(new Integer[]{20, 40, 60, 80, 100}));
                countCombo.setSelectedItem(60);
            } else {
                countCombo.setModel(new DefaultComboBoxModel<>(new Integer[]{10, 20, 30, 40, 50}));
                countCombo.setSelectedItem(30);
            }
        });
        configDialog.add(countCombo);

        // 3. Thời gian
        configDialog.add(new JLabel("Thời gian (giây):"));
        JSpinner timeSpinner = new JSpinner(new SpinnerNumberModel(90, 30, 300, 5));
        configDialog.add(timeSpinner);

        // 4. Gợi ý
        configDialog.add(new JLabel("Khung gợi ý:"));
        JCheckBox hintCheckbox = new JCheckBox("Bật", true);
        configDialog.add(hintCheckbox);
        
        configDialog.add(new JLabel()); // Ô trống
        
        // 5. Nút Bắt đầu
        JButton startButton = new JButton("Bắt đầu");
        startButton.addActionListener(e -> {
            config.setItemCount((Integer) countCombo.getSelectedItem());
            config.setTimeLimitSeconds((Integer) timeSpinner.getValue());
            config.setHintsEnabled(hintCheckbox.isSelected());
            controller.submitGameConfig(config);
            configDialog.dispose();
        });
        configDialog.add(startButton);

        configDialog.setVisible(true);
    }

    // 🏆 Hiển thị bảng xếp hạng
    public void showLeaderboardDialog(List<Player> leaderboard) {
        JDialog dialog = new JDialog(this, "🏆 Bảng xếp hạng", true);
        dialog.setSize(550, 420);
        dialog.setLocationRelativeTo(this);

        JLabel title = new JLabel("TOP NGƯỜI CHƠI", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 20));
        title.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        String[] columns = {"Hạng", "Tên người chơi", "Thắng", "Thua", "Tỷ lệ thắng (%)", "Điểm số"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        int rank = 1;
        for (Player p : leaderboard) {
            int wins = p.getGamesWon();  // Thay p.getWins()
            int losses = p.getGamesPlayed() - wins;  // Tính losses động
            int total = p.getGamesPlayed();
            double rate = total > 0 ? (wins * 100.0 / total) : 0;
            model.addRow(new Object[]{rank++, p.getUsername(), wins, losses, String.format("%.1f", rate), p.getTotalScore()});
        }

        JTable table = new JTable(model);
        table.setFont(new Font("Arial", Font.PLAIN, 14));
        table.setRowHeight(28);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        table.setAutoCreateRowSorter(true);
        table.setDefaultEditor(Object.class, null);

        // Renderer: căn giữa và màu sắc tùy chỉnh (loại bỏ setCellRenderer để tránh xung đột)
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(SwingConstants.CENTER); // Căn giữa cho tất cả
                if (!isSelected) {
                    // Hạng 1: nền vàng nhạt
                    if (row == 0) {
                        c.setBackground(new Color(255, 255, 204)); // Vàng nhạt
                        setForeground(Color.BLACK);
                    } else {
                        c.setBackground(row % 2 == 0 ? new Color(245, 250, 255) : Color.WHITE);
                        setForeground(Color.BLACK);
                    }
                    // Cột tỷ lệ thắng: màu chữ dựa trên giá trị (ghi đè màu mặc định)
                    if (column == 4) { // Cột "Tỷ lệ thắng (%)"
                        try {
                            String strValue = value.toString().replace("%", "").trim(); // Bỏ "%" nếu có (dù data không có)
                            double rate = Double.parseDouble(strValue);
                            // Debug: In rate để kiểm tra (xóa sau khi test)
                            System.out.println("Row " + row + ", Rate: " + rate);
                            if (rate >= 70) {
                                setForeground(Color.GREEN.darker());
                            } else if (rate >= 50) {
                                setForeground(Color.ORANGE);
                            } else {
                                setForeground(Color.RED);
                            }
                        } catch (NumberFormatException e) {
                            setForeground(Color.BLACK); // Mặc định nếu parse fail
                            System.out.println("Parse fail for value: " + value);
                        }
                    } // Không cần else ở đây, vì đã setForeground(Color.BLACK) ở trên cho cột khác
                }
                return c;
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        JButton closeBtn = new JButton("Đóng");
        closeBtn.addActionListener(e -> dialog.dispose());

        JPanel bottom = new JPanel();
        bottom.add(closeBtn);

        dialog.setLayout(new BorderLayout());
        dialog.add(title, BorderLayout.NORTH);
        dialog.add(scroll, BorderLayout.CENTER);
        dialog.add(bottom, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }
    // 📜 Hiển thị lịch sử đấu
    public void showMatchHistoryDialog(List<String[]> history) {
        JDialog dialog = new JDialog(this, "📜 Lịch sử đấu", true);
        dialog.setSize(600, 420);
        dialog.setLocationRelativeTo(this);

        JLabel title = new JLabel("LỊCH SỬ TRẬN ĐẤU", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 20));
        title.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        String[] columns = {"Đối thủ", "Kết quả", "Thời gian", "Loại nội dung"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        for (String[] row : history) {
            String opponent = row[0];
            String result = row[1];
            String time = row[2];
            String type = row[3];
            
            // SỬA: Format thời gian từ "2025-11-05 00:23:17.0" thành "05/11/2025 00:23"
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                java.util.Date date = inputFormat.parse(time);
                time = outputFormat.format(date);
            } catch (ParseException e) {
                // Nếu parse fail, giữ nguyên time gốc
                System.err.println("Failed to parse time: " + time);
            }
            
            model.addRow(new Object[]{opponent, result, time, type});
        }

        JTable table = new JTable(model);
        table.setFont(new Font("Arial", Font.PLAIN, 14));
        table.setRowHeight(26);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        table.setDefaultEditor(Object.class, null);
        table.setAutoCreateRowSorter(true);


        // Renderer: màu xen kẽ, với màu đặc biệt cho kết quả
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(SwingConstants.CENTER);
                if (!isSelected) {
                    // Debug: In value và column (bỏ sau khi test)
                    System.out.println("History - Row: " + row + ", Column: " + column + ", Value: " + value);

                    c.setBackground(row % 2 == 0 ? new Color(250, 250, 250) : new Color(235, 240, 245));

                    // Cột kết quả: màu chữ dựa trên thắng/thua (index 1)
                    if (column == 1) {
                        String strValue = value.toString();
                        if ("Winner: player1".equals(strValue) || strValue.contains("player1")) { // Giả định "Winner: player1" là thắng
                            setForeground(Color.GREEN.darker());
                        } else if ("Winner: Draw".equals(strValue)) {
                            setForeground(Color.ORANGE); // Hòa
                        } else {
                            setForeground(Color.RED); // Thua
                        }
                    } else {
                        setForeground(Color.BLACK); // Màu mặc định cho cột khác
                    }
                }
                return c;
            }
        });


        JScrollPane scroll = new JScrollPane(table);
        JButton closeBtn = new JButton("Đóng");
        closeBtn.addActionListener(e -> dialog.dispose());

        JPanel bottom = new JPanel();
        bottom.add(closeBtn);

        dialog.setLayout(new BorderLayout());
        dialog.add(title, BorderLayout.NORTH);
        dialog.add(scroll, BorderLayout.CENTER);
        dialog.add(bottom, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }
    
    
    // Custom renderer class
    private static class PlayerListRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof Player) {
                Player player = (Player) value;
                setText(player.toString());
                if (player.getStatus() == Player.PlayerStatus.BUSY) {
                    setForeground(Color.GRAY);
                } else {
                    setForeground(Color.BLACK);
                }
            }
            return c;
        }
    }
}
