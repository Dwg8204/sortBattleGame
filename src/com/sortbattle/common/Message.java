/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sortbattle.common;

import java.io.Serializable;

/**
 * Lớp đóng gói dữ liệu truyền đi giữa Server và Client.
 * Việc sử dụng một đối tượng Message thay vì các kiểu dữ liệu nguyên thủy giúp
 * dễ dàng mở rộng và quản lý.
 */
public class Message implements Serializable {
    private static final long serialVersionUID = 1L; // Đảm bảo tương thích phiên bản

    private MessageType type; // Loại thông điệp
    private Object payload;   // Nội dung/dữ liệu đính kèm

    public Message(MessageType type, Object payload) {
        this.type = type;
        this.payload = payload;
    }

    public MessageType getType() {
        return type;
    }

    public Object getPayload() {
        return payload;
    }

    @Override
    public String toString() {
        return "Message{" +
                "type=" + type +
                ", payload=" + (payload != null ? payload.toString() : "null") +
                '}';
    }
}
