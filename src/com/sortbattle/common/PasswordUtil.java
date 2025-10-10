
package com.sortbattle.common;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PasswordUtil {
    
    /**
     * Mã hóa mật khẩu bằng MD5
     * @param password Mật khẩu gốc
     * @return Chuỗi MD5 hash (32 ký tự hex)
     */
    public static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(password.getBytes());
            
            // Chuyển byte array sang hex string
            StringBuilder hexString = new StringBuilder();
            for (byte b : messageDigest) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 algorithm not found", e);
        }
    }
    
    /**
     * Kiểm tra mật khẩu có khớp với hash không
     */
    public static boolean checkPassword(String password, String hashedPassword) {
        String inputHash = hashPassword(password);
        return inputHash.equals(hashedPassword);
    }
    
    // Test
    public static void main(String[] args) {
        String password = "123456";
        String hashed = hashPassword(password);
        System.out.println("Password: " + password);
        System.out.println("MD5 Hash: " + hashed);
        System.out.println("Verify: " + checkPassword("123456", hashed));
        System.out.println("Wrong: " + checkPassword("wrong", hashed));
    }
}