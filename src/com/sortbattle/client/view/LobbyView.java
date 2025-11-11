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
import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import javax.swing.border.EmptyBorder;

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
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        // Hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor.brighter());
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor);
            }
        });
    }

    public LobbyView(ClientController controller) {
        this.controller = controller;

        setTitle("SẢNH CHỜ - SORT BATTLE ONLINE");
        setSize(650, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // ========== PANEL CHÍNH ==========
        JPanel mainPanel = new JPanel(new BorderLayout(20, 20));
        mainPanel.setBackground(new Color(240, 248, 255)); // Alice Blue
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // ========== HEADER - CHÀO MỪNG ==========
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        topPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(100, 149, 237), 2),
                new EmptyBorder(15, 15, 15, 15)));
        topPanel.setBackground(Color.WHITE);

        welcomeLabel = new JLabel("CHÀO MỪNG!", JLabel.CENTER);
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        welcomeLabel.setForeground(new Color(25, 25, 112)); // Midnight Blue
        topPanel.add(welcomeLabel, BorderLayout.CENTER);

        mainPanel.add(topPanel, BorderLayout.NORTH);

        // ========== CENTER - DANH SÁCH NGƯỜI CHƠI ==========
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setOpaque(false);

        // Tiêu đề danh sách
        JLabel listTitle = new JLabel("NGƯỜI CHƠI ĐANG ONLINE", JLabel.LEFT);
        listTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        listTitle.setForeground(new Color(25, 25, 112));
        listTitle.setBorder(new EmptyBorder(0, 5, 5, 0));
        centerPanel.add(listTitle, BorderLayout.NORTH);

        // Danh sách người chơi
        playerListModel = new DefaultListModel<>();
        playerList = new JList<>(playerListModel);
        playerList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        playerList.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        playerList.setCellRenderer(new PlayerListRenderer());
        playerList.setBackground(Color.WHITE);
        playerList.setFixedCellHeight(45);
        playerList.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        JScrollPane scrollPane = new JScrollPane(playerList);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(176, 196, 222), 2),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        scrollPane.setBackground(Color.WHITE);

        centerPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(centerPanel, BorderLayout.CENTER);

        // ========== BOTTOM - CÁC NÚT CHỨC NĂNG ==========
        JPanel bottomPanel = new JPanel(new GridLayout(1, 3, 15, 0));
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(new EmptyBorder(10, 0, 0, 0));

        // Nút Thách đấu
        challengeButton = new JButton("Thách đấu");
        styleButton(challengeButton, new Color(34, 139, 34)); // Forest Green
        challengeButton.addActionListener(e -> {
            Player selectedPlayer = playerList.getSelectedValue();
            if (selectedPlayer != null) {
                if (selectedPlayer.getStatus() == Player.PlayerStatus.IDLE) {
                    controller.challengePlayer(selectedPlayer.getUsername());
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Người chơi này đang bận!",
                            "Không thể thách đấu",
                            JOptionPane.WARNING_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this,
                        "Vui lòng chọn một người chơi để thách đấu.",
                        "Chưa chọn đối thủ",
                        JOptionPane.WARNING_MESSAGE);
            }
        });
        bottomPanel.add(challengeButton);

        // Nút Bảng xếp hạng
        JButton leaderboardButton = new JButton("Bảng xếp hạng");
        styleButton(leaderboardButton, new Color(255, 140, 0)); // Dark Orange
        leaderboardButton.addActionListener(e -> controller.requestLeaderboard());
        bottomPanel.add(leaderboardButton);

        // Nút Lịch sử đấu
        JButton historyButton = new JButton("Lịch sử đấu");
        styleButton(historyButton, new Color(70, 130, 180)); // Steel Blue
        historyButton.addActionListener(e -> controller.requestHistory());
        bottomPanel.add(historyButton);

        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        add(mainPanel);
    }

    public void updateWelcomeMessage(String username) {
        Player currentPlayer = controller.getModel().getCurrentPlayer();

        if (currentPlayer != null) {
            welcomeLabel.setText("<html><div style='text-align:center; font-family:Segoe UI;'>" +
                    "<h2 style='color:#191970; margin:5px;'>CHÀO MỪNG, " + username.toUpperCase() + "!</h2>" +
                    "<p style='font-size:13px; color:#4682B4; margin:3px;'>" +
                    "Tổng điểm: <b style='color:#FF8C00;'>" + currentPlayer.getTotalScore() + "</b> | " +
                    "Trận thắng: <b style='color:#228B22;'>" + currentPlayer.getGamesWon() + "</b>/" +
                    "<b>" + currentPlayer.getGamesPlayed() + "</b></p>" +
                    "</div></html>");
        } else {
            welcomeLabel.setText("<html><div style='text-align:center; font-family:Segoe UI;'>" +
                    "<h2 style='color:#191970;'>CHÀO MỪNG, " + username.toUpperCase() + "!</h2></div></html>");
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
        JDialog configDialog = new JDialog(this, "Cấu hình trận đấu với " + opponentName, true);
        configDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(new Color(245, 248, 250));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;

        GameConfig config = new GameConfig();

        // 1. Loại nội dung
        JPanel typePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        typePanel.setOpaque(false);
        typePanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(100, 149, 237), 1),
                "Loại nội dung",
                0, 0,
                new Font("Segoe UI", Font.BOLD, 13),
                new Color(25, 25, 112)));
        JLabel typeLabel = new JLabel("Chọn loại:");
        typeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        JComboBox<String> typeCombo = new JComboBox<>(new String[] { "Từ", "Số" });
        typeCombo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        typeCombo.setBackground(Color.WHITE);
        typeCombo.addActionListener(e -> config.setDataType(
                "Số".equals(typeCombo.getSelectedItem()) ? GameConfig.DataType.NUMBER : GameConfig.DataType.WORD));
        typePanel.add(typeLabel);
        typePanel.add(typeCombo);

        gbc.gridwidth = 2;
        mainPanel.add(typePanel, gbc);
        gbc.gridy++;

        // 2. Số lượng
        JPanel countPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        countPanel.setOpaque(false);
        countPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(100, 149, 237), 1),
                "Số lượng",
                0, 0,
                new Font("Segoe UI", Font.BOLD, 13),
                new Color(25, 25, 112)));
        JLabel countLabel = new JLabel("Số lượng:");
        countLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        JComboBox<Integer> countCombo = new JComboBox<>(new Integer[] { 10, 20, 30, 40, 50 });
        countCombo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        countCombo.setBackground(Color.WHITE);
        countCombo.setSelectedItem(30);
        typeCombo.addActionListener(e -> {
            if ("Số".equals(typeCombo.getSelectedItem())) {
                countCombo.setModel(new DefaultComboBoxModel<>(new Integer[] { 20, 40, 60, 80, 100 }));
                countCombo.setSelectedItem(60);
            } else {
                countCombo.setModel(new DefaultComboBoxModel<>(new Integer[] { 10, 20, 30, 40, 50 }));
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
        timePanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(100, 149, 237), 1),
                "Thời gian",
                0, 0,
                new Font("Segoe UI", Font.BOLD, 13),
                new Color(25, 25, 112)));
        JLabel timeLabel = new JLabel("Thời gian (giây):");
        timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        JSpinner timeSpinner = new JSpinner(new SpinnerNumberModel(90, 30, 300, 5));
        timeSpinner.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        timePanel.add(timeLabel);
        timePanel.add(timeSpinner);

        gbc.gridwidth = 2;
        mainPanel.add(timePanel, gbc);
        gbc.gridy++;

        // 4. Gợi ý
        JPanel hintPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        hintPanel.setOpaque(false);
        hintPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(100, 149, 237), 1),
                "Gợi ý",
                0, 0,
                new Font("Segoe UI", Font.BOLD, 13),
                new Color(25, 25, 112)));
        JLabel hintLabel = new JLabel("Khung gợi ý:");
        hintLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        JCheckBox hintCheckbox = new JCheckBox("Bật", true);
        hintCheckbox.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        hintCheckbox.setOpaque(false);
        hintPanel.add(hintLabel);
        hintPanel.add(hintCheckbox);

        gbc.gridwidth = 2;
        mainPanel.add(hintPanel, gbc);
        gbc.gridy++;

        // 5. Nút Bắt đầu
        JButton startButton = new JButton("BẮT ĐẦU");
        startButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
        startButton.setBackground(new Color(34, 139, 34));
        startButton.setForeground(Color.WHITE);
        startButton.setFocusPainted(false);
        startButton.setBorderPainted(false);
        startButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        startButton.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));
        startButton.addActionListener(e -> {
            config.setItemCount((Integer) countCombo.getSelectedItem());
            config.setTimeLimitSeconds((Integer) timeSpinner.getValue());
            config.setHintsEnabled(hintCheckbox.isSelected());
            controller.submitGameConfig(config);
            configDialog.dispose();
        });
        startButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                startButton.setBackground(new Color(60, 179, 113));
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                startButton.setBackground(new Color(34, 139, 34));
            }
        });

        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(20, 10, 10, 10);
        mainPanel.add(startButton, gbc);

        configDialog.add(mainPanel);
        configDialog.pack();
        configDialog.setResizable(false);
        configDialog.setLocationRelativeTo(this);
        configDialog.setVisible(true);
    }

    public void showLeaderboardDialog(List<Player> leaderboard) {
        JDialog dialog = new JDialog(this, "Bảng xếp hạng", true);
        dialog.setSize(850, 650);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(new Color(245, 248, 250));

        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(100, 149, 237));
        headerPanel.setBorder(new EmptyBorder(15, 0, 15, 0));

        JLabel title = new JLabel("BẢNG XẾP HẠNG NGƯỜI CHƠI", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(Color.WHITE);
        headerPanel.add(title);

        String[] columns = { "Hạng", "Tên người chơi", "Thắng", "Thua", "Tỷ lệ (%)", "Điểm" };
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
        table.setRowHeight(38);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 15));
        table.getTableHeader().setBackground(new Color(70, 130, 180));
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setBorder(BorderFactory.createLineBorder(new Color(70, 130, 180)));
        table.setShowGrid(true);
        table.setGridColor(new Color(220, 220, 220));
        table.setIntercellSpacing(new Dimension(1, 1));
        table.setAutoCreateRowSorter(true);
        table.setDefaultEditor(Object.class, null);

        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus,
                    int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(SwingConstants.CENTER);

                if (!isSelected) {
                    if (row == 0)
                        c.setBackground(new Color(255, 215, 0));
                    else if (row == 1)
                        c.setBackground(new Color(192, 192, 192));
                    else if (row == 2)
                        c.setBackground(new Color(205, 127, 50));
                    else
                        c.setBackground(row % 2 == 0 ? new Color(245, 250, 255) : Color.WHITE);

                    setForeground(Color.BLACK);

                    if (column == 4) {
                        try {
                            double rate = Double.parseDouble(value.toString());
                            if (rate >= 70)
                                setForeground(new Color(0, 150, 0));
                            else if (rate >= 50)
                                setForeground(new Color(255, 140, 0));
                            else
                                setForeground(new Color(220, 20, 60));
                            setFont(getFont().deriveFont(Font.BOLD));
                        } catch (Exception ignored) {
                        }
                    }

                    if (column == 5) {
                        setForeground(new Color(70, 130, 180));
                        setFont(getFont().deriveFont(Font.BOLD));
                    }
                } else {
                    c.setBackground(new Color(100, 149, 237));
                    setForeground(Color.WHITE);
                }
                return c;
            }
        };

        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JButton closeBtn = new JButton("Đóng");
        closeBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        closeBtn.setBackground(new Color(220, 20, 60));
        closeBtn.setForeground(Color.WHITE);
        closeBtn.setFocusPainted(false);
        closeBtn.setBorderPainted(false);
        closeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeBtn.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));
        closeBtn.addActionListener(e -> dialog.dispose());
        closeBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                closeBtn.setBackground(new Color(255, 69, 0));
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                closeBtn.setBackground(new Color(220, 20, 60));
            }
        });

        JPanel bottom = new JPanel();
        bottom.setBackground(new Color(245, 248, 250));
        bottom.setBorder(new EmptyBorder(10, 0, 15, 0));
        bottom.add(closeBtn);

        dialog.setLayout(new BorderLayout());
        dialog.add(headerPanel, BorderLayout.NORTH);
        dialog.add(scroll, BorderLayout.CENTER);
        dialog.add(bottom, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    public void showMatchHistoryDialog(List<String[]> history) {
        JDialog dialog = new JDialog(this, "Lịch sử đấu", true);
        dialog.setSize(850, 650);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(new Color(245, 248, 250));

        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(70, 130, 180));
        headerPanel.setBorder(new EmptyBorder(15, 0, 15, 0));

        JLabel title = new JLabel("LỊCH SỬ TRẬN ĐẤU", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(Color.WHITE);
        headerPanel.add(title);

        String[] columns = { "Đối thủ", "Kết quả", "Thời gian", "Loại" };
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
        table.setRowHeight(35);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 15));
        table.getTableHeader().setBackground(new Color(100, 149, 237));
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setBorder(BorderFactory.createLineBorder(new Color(100, 149, 237)));
        table.setShowGrid(true);
        table.setGridColor(new Color(220, 220, 220));
        table.setIntercellSpacing(new Dimension(1, 1));
        table.setDefaultEditor(Object.class, null);
        table.setAutoCreateRowSorter(true);

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus,
                    int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                c.setForeground(Color.BLACK);
                c.setFont(table.getFont());
                setHorizontalAlignment(SwingConstants.CENTER);

                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? new Color(245, 250, 255) : Color.WHITE);

                    if (column == 1 && value != null) {
                        String strValue = value.toString().toLowerCase();
                        if (strValue.contains("player1") || strValue.contains("thắng")) {
                            c.setForeground(new Color(34, 139, 34));
                            c.setFont(c.getFont().deriveFont(Font.BOLD));
                        } else if (strValue.contains("draw") || strValue.contains("hòa")) {
                            c.setForeground(new Color(255, 140, 0));
                            c.setFont(c.getFont().deriveFont(Font.BOLD));
                        } else if (strValue.contains("player2") || strValue.contains("thua")) {
                            c.setForeground(new Color(220, 20, 60));
                            c.setFont(c.getFont().deriveFont(Font.BOLD));
                        }
                    }
                } else {
                    c.setBackground(new Color(100, 149, 237));
                    c.setForeground(Color.WHITE);
                }
                return c;
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JButton closeBtn = new JButton("Đóng");
        closeBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        closeBtn.setBackground(new Color(220, 20, 60));
        closeBtn.setForeground(Color.WHITE);
        closeBtn.setFocusPainted(false);
        closeBtn.setBorderPainted(false);
        closeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeBtn.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));
        closeBtn.addActionListener(e -> dialog.dispose());
        closeBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                closeBtn.setBackground(new Color(255, 69, 0));
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                closeBtn.setBackground(new Color(220, 20, 60));
            }
        });

        JPanel bottom = new JPanel();
        bottom.setBackground(new Color(245, 248, 250));
        bottom.setBorder(new EmptyBorder(10, 0, 15, 0));
        bottom.add(closeBtn);

        dialog.setLayout(new BorderLayout());
        dialog.add(headerPanel, BorderLayout.NORTH);
        dialog.add(scroll, BorderLayout.CENTER);
        dialog.add(bottom, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    // ========== CUSTOM RENDERER CHO DANH SÁCH NGƯỜI CHƠI ==========
    private static class PlayerListRenderer extends JPanel implements ListCellRenderer<Player> {
        private StatusCirclePanel statusIcon;
        private JLabel nameLabel;

        public PlayerListRenderer() {
            setLayout(new BorderLayout(10, 0));
            setOpaque(true);
            setBorder(new EmptyBorder(8, 15, 8, 15));

            // Icon trạng thái (chấm tròn màu)
            statusIcon = new StatusCirclePanel();
            statusIcon.setPreferredSize(new Dimension(16, 16));

            // Tên người chơi
            nameLabel = new JLabel();
            nameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 15));

            add(statusIcon, BorderLayout.WEST);
            add(nameLabel, BorderLayout.CENTER);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends Player> list, Player player, int index,
                boolean isSelected, boolean cellHasFocus) {

            if (player != null) {
                // Thiết lập màu icon
                if (player.getStatus() == Player.PlayerStatus.IDLE) {
                    statusIcon.setColor(new Color(0, 200, 0)); // Xanh lá
                    nameLabel.setText(player.getUsername() + " (Rảnh)");
                } else {
                    statusIcon.setColor(new Color(220, 20, 60)); // Đỏ
                    nameLabel.setText(player.getUsername() + " (Bận)");
                }

                // Màu nền khi selected
                if (isSelected) {
                    setBackground(new Color(100, 149, 237));
                    nameLabel.setForeground(Color.WHITE);
                } else {
                    setBackground(Color.WHITE);
                    if (player.getStatus() == Player.PlayerStatus.BUSY) {
                        nameLabel.setForeground(new Color(128, 128, 128));
                    } else {
                        nameLabel.setForeground(new Color(25, 25, 112));
                    }
                }
            }

            return this;
        }
    }

    // ========== PANEL VẼ HÌNH TRÒN TRẠNG THÁI ==========
    private static class StatusCirclePanel extends JPanel {
        private Color color = new Color(0, 200, 0);

        public StatusCirclePanel() {
            setOpaque(false);
        }

        public void setColor(Color color) {
            this.color = color;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int size = Math.min(getWidth(), getHeight());
            int x = (getWidth() - size) / 2;
            int y = (getHeight() - size) / 2;

            // Vẽ hình tròn
            g2d.setColor(color);
            g2d.fillOval(x, y, size, size);
        }
    }
}