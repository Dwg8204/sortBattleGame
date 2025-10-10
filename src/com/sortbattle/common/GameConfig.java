/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sortbattle.common;

import java.io.Serializable;

/**
 * Lớp chứa thông tin cấu hình cho một trận đấu.
 */
public class GameConfig implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum DataType {
        NUMBER,
        WORD
    }

    public enum SortOrder {
        ASCENDING,  // Tăng dần
        DESCENDING  // Giảm dần
    }

    private DataType dataType;
    private int itemCount; // Số lượng từ hoặc giới hạn trên của số (e.g., 100)
    private int timeLimitSeconds; // Thời gian trận đấu (giây)
    private boolean hintsEnabled; // Bật/tắt gợi ý
    private SortOrder sortOrder; // Kiểu sắp xếp

    // Constructor với các giá trị mặc định
    public GameConfig() {
        this.dataType = DataType.WORD;
        this.itemCount = 30;
        this.timeLimitSeconds = 90;
        this.hintsEnabled = true;
        this.sortOrder = SortOrder.ASCENDING; // Server sẽ random lại sau
    }

    // Getters and Setters
    public DataType getDataType() {
        return dataType;
    }

    public void setDataType(DataType dataType) {
        this.dataType = dataType;
    }

    public int getItemCount() {
        return itemCount;
    }

    public void setItemCount(int itemCount) {
        this.itemCount = itemCount;
    }

    public int getTimeLimitSeconds() {
        return timeLimitSeconds;
    }

    public void setTimeLimitSeconds(int timeLimitSeconds) {
        this.timeLimitSeconds = timeLimitSeconds;
    }

    public boolean isHintsEnabled() {
        return hintsEnabled;
    }

    public void setHintsEnabled(boolean hintsEnabled) {
        this.hintsEnabled = hintsEnabled;
    }

    public SortOrder getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(SortOrder sortOrder) {
        this.sortOrder = sortOrder;
    }
}