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
import java.util.Date;
import java.util.List;

public class LobbyView extends JFrame {

    private final ClientController controller;
    private final JLabel welcomeLabel;
    private final JList<Player> playerList;
    private final DefaultListModel<Player> playerListModel;
    private final JButton challengeButton;

    private void styleButton(JButton button, Color bgColor) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        // Hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor.darker());
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor);
            }
        });
    }

    public LobbyView(ClientController controller) {
        this.controller = controller;

        setTitle("Sảnh chờ - Sort Battle Online");
        setSize(500, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Panel chính
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBackground(new Color(240, 242, 245)); // màu xám nhạt, dịu mắt
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Panel phía trên (Welcome)
        JPanel topPanel = new JPanel();
        topPanel.setOpaque(false);
        welcomeLabel = new JLabel("Chào mừng!");
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        welcomeLabel.setForeground(new Color(50, 50, 50));
        topPanel.add(welcomeLabel);
        mainPanel.add(topPanel, BorderLayout.NORTH);

        // Panel trung tâm (Danh sách người chơi)
        playerListModel = new DefaultListModel<>();
        playerList = new JList<>(playerListModel);
        playerList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        playerList.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        playerList.setCellRenderer(new PlayerListRenderer());
        playerList.setBackground(new Color(250, 250, 250));
        playerList.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
        JScrollPane scrollPane = new JScrollPane(playerList);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Panel phía dưới (Nút thách đấu)
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        bottomPanel.setOpaque(false);

        challengeButton = new JButton("Thách đấu");
        styleButton(challengeButton, new Color(70, 130, 180)); // màu xanh dịu
        challengeButton.addActionListener(e -> {
            Player selectedPlayer = playerList.getSelectedValue();
            if (selectedPlayer != null) {
                if (selectedPlayer.getStatus() == Player.PlayerStatus.IDLE) {
                    controller.challengePlayer(selectedPlayer.getUsername());
                } else {
                    JOptionPane.showMessageDialog(this, "Người chơi này đang bận!", "Không thể thách đấu",
                            JOptionPane.WARNING_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn một người chơi để thách đấu.", "Chưa chọn đối thủ",
                        JOptionPane.WARNING_MESSAGE);
            }
        });
        bottomPanel.add(challengeButton);

        JButton leaderboardButton = new JButton("Bảng xếp hạng");
        styleButton(leaderboardButton, new Color(100, 180, 100)); // xanh lá dịu
        leaderboardButton.addActionListener(e -> controller.requestLeaderboard());
        bottomPanel.add(leaderboardButton);

        JButton historyButton = new JButton("Lịch sử đấu");
        styleButton(historyButton, new Color(200, 160, 80)); // vàng nhạt
        historyButton.addActionListener(e -> controller.requestHistory());
        bottomPanel.add(historyButton);

        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        add(mainPanel);
    }

    public void updateWelcomeMessage(String username) {
        // Lấy thông tin player mới nhất từ model
        Player currentPlayer = controller.getModel().getCurrentPlayer();

        if (currentPlayer != null) {
            welcomeLabel.setText("<html><h2>Chào mừng, " + username + "!</h2>" +
                    "<p>Tổng điểm: <b>" + currentPlayer.getTotalScore() + "</b> | " +
                    "Trận thắng: <b>" + currentPlayer.getGamesWon() + "</b>/" +
                    currentPlayer.getGamesPlayed() + "</p></html>");
        } else {
            welcomeLabel.setText("<html><h2>Chào mừng, " + username + "!</h2></html>");
        }
    }

    public void updatePlayerList(List<Player> players, String currentPlayerUsername) {
        playerListModel.clear();
        for (Player player : players) {
            if (!player.getUsername().equals(currentPlayerUsername)) {
                playerListModel.addElement(player);
            }
        }
    }

    public void showGameConfigDialog(String opponentName) {
        // Tạo dialog
        JDialog configDialog = new JDialog(this, "Cấu hình trận đấu với " + opponentName, true);
        configDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        // Panel chính với GridBagLayout
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(new Color(245, 245, 245)); // nền dịu mắt
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;

        GameConfig config = new GameConfig(); // config mặc định

        // 1. Loại nội dung
        JPanel typePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        typePanel.setOpaque(false);
        typePanel.setBorder(BorderFactory.createTitledBorder("Loại nội dung"));
        JLabel typeLabel = new JLabel("Chọn loại:");
        typeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        JComboBox<String> typeCombo = new JComboBox<>(new String[]{"Từ", "Số"});
        typeCombo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        typeCombo.addActionListener(e -> config.setDataType(
                "Số".equals(typeCombo.getSelectedItem()) ? GameConfig.DataType.NUMBER : GameConfig.DataType.WORD));
        typePanel.add(typeLabel);
        typePanel.add(typeCombo);

        gbc.gridwidth = 2;
        mainPanel.add(typePanel, gbc);
        gbc.gridy++;

        // 2. Số lượng/Khoảng
        JPanel countPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        countPanel.setOpaque(false);
        countPanel.setBorder(BorderFactory.createTitledBorder("Số lượng/Khoảng"));
        JLabel countLabel = new JLabel("Số lượng:");
        countLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        JComboBox<Integer> countCombo = new JComboBox<>(new Integer[]{10, 20, 30, 40, 50, 60, 80, 100});
        countCombo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        countCombo.setSelectedItem(30);
        typeCombo.addActionListener(e -> {
            if ("Số".equals(typeCombo.getSelectedItem())) {
                countCombo.setModel(new DefaultComboBoxModel<>(new Integer[]{20, 40, 60, 80, 100}));
                countCombo.setSelectedItem(60);
            } else {
                countCombo.setModel(new DefaultComboBoxModel<>(new Integer[]{10, 20, 30, 40, 50}));
                countCombo.setSelectedItem(30);
            }
        });
        countPanel.add(countLabel);
        countPanel.add(countCombo);

        gbc.gridwidth = 2;
        mainPanel.add(countPanel, gbc);
        gbc.gridy++;

        // 3. Thời gian
        JPanel timePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        timePanel.setOpaque(false);
        timePanel.setBorder(BorderFactory.createTitledBorder("Thời gian"));
        JLabel timeLabel = new JLabel("Thời gian (giây):");
        timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        JSpinner timeSpinner = new JSpinner(new SpinnerNumberModel(90, 30, 300, 5));
        timePanel.add(timeLabel);
        timePanel.add(timeSpinner);

        gbc.gridwidth = 2;
        mainPanel.add(timePanel, gbc);
        gbc.gridy++;

        // 4. Gợi ý
        JPanel hintPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        hintPanel.setOpaque(false);
        hintPanel.setBorder(BorderFactory.createTitledBorder("Gợi ý"));
        JLabel hintLabel = new JLabel("Khung gợi ý:");
        hintLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        JCheckBox hintCheckbox = new JCheckBox("Bật", true);
        hintPanel.add(hintLabel);
        hintPanel.add(hintCheckbox);

        gbc.gridwidth = 2;
        mainPanel.add(hintPanel, gbc);
        gbc.gridy++;

        // 5. Nút Bắt đầu
        JButton startButton = new JButton("Bắt đầu");
        startButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        startButton.setBackground(new Color(70, 130, 180));
        startButton.setForeground(Color.WHITE);
        startButton.setFocusPainted(false);
        startButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        startButton.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        startButton.addActionListener(e -> {
            config.setItemCount((Integer) countCombo.getSelectedItem());
            config.setTimeLimitSeconds((Integer) timeSpinner.getValue());
            config.setHintsEnabled(hintCheckbox.isSelected());
            controller.submitGameConfig(config);
            configDialog.dispose();
        });
        // Hover effect
        startButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                startButton.setBackground(new Color(100, 149, 237));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                startButton.setBackground(new Color(70, 130, 180));
            }
        });

        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        mainPanel.add(startButton, gbc);

        configDialog.add(mainPanel);
        configDialog.pack();
        configDialog.setResizable(false);
        configDialog.setLocationRelativeTo(this);
        configDialog.setVisible(true);
    }



    // Hiển thị bảng xếp hạng
    public void showLeaderboardDialog(List<Player> leaderboard) {
        JDialog dialog = new JDialog(this, "Bảng xếp hạng", true);
        dialog.setSize(800, 600);
        dialog.setLocationRelativeTo(this);

        // --- Header ---
        JLabel title = new JLabel("BẢNG XẾP HẠNG NGƯỜI CHƠI", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(new Color(30, 60, 90));
        title.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));

        // --- Table ---
        String[] columns = { "Hạng", "Tên người chơi", "Thắng", "Thua", "Tỷ lệ thắng (%)", "Điểm số" };
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        int rank = 1;
        for (Player p : leaderboard) {
            int wins = p.getGamesWon();
            int losses = p.getGamesPlayed() - wins;
            int total = p.getGamesPlayed();
            double rate = total > 0 ? (wins * 100.0 / total) : 0;
            model.addRow(new Object[] { rank++, p.getUsername(), wins, losses, String.format("%.1f", rate),
                    p.getTotalScore() });
        }

        JTable table = new JTable(model);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setRowHeight(32);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 15));
        table.getTableHeader().setBackground(new Color(220, 230, 240));
        table.getTableHeader().setForeground(new Color(30, 60, 90));
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setAutoCreateRowSorter(true);
        table.setDefaultEditor(Object.class, null);

        // --- Custom Renderer ---
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus,
                    int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(SwingConstants.CENTER);

                if (!isSelected) {
                    // Màu nền xen kẽ
                    if (row == 0)
                        c.setBackground(new Color(255, 245, 170)); // vàng - top 1
                    else if (row == 1)
                        c.setBackground(new Color(230, 230, 230)); // bạc - top 2
                    else if (row == 2)
                        c.setBackground(new Color(255, 220, 180)); // đồng - top 3
                    else
                        c.setBackground(row % 2 == 0 ? new Color(245, 250, 255) : Color.WHITE);

                    setForeground(Color.BLACK);
                    // Tỷ lệ thắng - tô màu theo giá trị
                    if (column == 4) {
                        try {
                            double rate = Double.parseDouble(value.toString());
                            if (rate >= 70)
                                setForeground(new Color(0, 150, 0));
                            else if (rate >= 50)
                                setForeground(new Color(255, 140, 0));
                            else
                                setForeground(Color.RED);
                        } catch (Exception ignored) {
                        }
                    }
                } else {
                    c.setBackground(new Color(204, 229, 255));
                    setForeground(Color.BLACK);
                }
                return c;
            }
        };

        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        JButton closeBtn = new JButton("Đóng");
        closeBtn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        closeBtn.setBackground(new Color(40, 120, 200));
        closeBtn.setForeground(Color.WHITE);
        closeBtn.setFocusPainted(false);
        closeBtn.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        closeBtn.addActionListener(e -> dialog.dispose());

        JPanel bottom = new JPanel();
        bottom.setBackground(new Color(250, 250, 250));
        bottom.add(closeBtn);

        dialog.setLayout(new BorderLayout());
        dialog.add(title, BorderLayout.NORTH);
        dialog.add(scroll, BorderLayout.CENTER);
        dialog.add(bottom, BorderLayout.SOUTH);
        dialog.getContentPane().setBackground(Color.WHITE);
        dialog.setVisible(true);
    }

    // 📜 Hiển thị lịch sử đấu (bản fix màu)
    public void showMatchHistoryDialog(List<String[]> history) {
        JDialog dialog = new JDialog(this, "Lịch sử đấu", true);
        dialog.setSize(800, 600);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(Color.WHITE);

        JLabel title = new JLabel("LỊCH SỬ TRẬN ĐẤU", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(new Color(30, 60, 90));
        title.setBorder(BorderFactory.createEmptyBorder(15, 0, 10, 0));

        String[] columns = { "Đối thủ", "Kết quả", "Thời gian", "Loại nội dung" };
        DefaultTableModel model = new DefaultTableModel(columns, 0);

        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat inputFormatAlt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
        SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");

        for (String[] row : history) {
            String opponent = row[0];
            String result = row[1];
            String time = row[2];
            String type = row[3];

            try {
                Date date;
                try {
                    date = inputFormat.parse(time);
                } catch (ParseException ex) {
                    date = inputFormatAlt.parse(time);
                }
                time = outputFormat.format(date);
            } catch (Exception ex) {
            }

            model.addRow(new Object[] { opponent, result, time, type });
        }

        JTable table = new JTable(model);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setRowHeight(28);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 15));
        table.getTableHeader().setBackground(new Color(220, 230, 240));
        table.getTableHeader().setForeground(new Color(30, 60, 90));
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setDefaultEditor(Object.class, null);
        table.setAutoCreateRowSorter(true);

        // --- Renderer fix màu chính xác ---
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus,
                    int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                // reset về mặc định mỗi lần render
                c.setForeground(Color.BLACK);
                c.setFont(table.getFont());
                setHorizontalAlignment(SwingConstants.CENTER);

                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? new Color(245, 250, 255) : Color.WHITE);

                    if (column == 1 && value != null) {
                        String strValue = value.toString().toLowerCase();
                        if (strValue.contains("player1")) {
                            c.setForeground(new Color(0, 150, 0)); // ✅ thắng
                            c.setFont(c.getFont().deriveFont(Font.BOLD));
                        } else if (strValue.contains("draw") || strValue.contains("hòa")) {
                            c.setForeground(new Color(255, 140, 0)); // 🟠 hòa
                            c.setFont(c.getFont().deriveFont(Font.BOLD));
                        } else if (strValue.contains("player2")) {
                            c.setForeground(new Color(200, 0, 0)); // 🔴 thua
                            c.setFont(c.getFont().deriveFont(Font.BOLD));
                        }
                    }
                } else {
                    c.setBackground(new Color(204, 229, 255));
                }
                return c;
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        JButton closeBtn = new JButton("Đóng");
        closeBtn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        closeBtn.setBackground(new Color(40, 120, 200));
        closeBtn.setForeground(Color.WHITE);
        closeBtn.setFocusPainted(false);
        closeBtn.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        closeBtn.addActionListener(e -> dialog.dispose());

        JPanel bottom = new JPanel();
        bottom.setBackground(new Color(250, 250, 250));
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
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
                boolean cellHasFocus) {
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
