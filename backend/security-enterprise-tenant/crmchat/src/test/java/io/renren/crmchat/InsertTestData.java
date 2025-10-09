package io.renren.crmchat;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class InsertTestData {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/db_crmchat?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai";
        String user = "root";
        String password = "root";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement()) {

            // 插入测试数据
            String sql = "INSERT INTO eb_chat_user (appid, nickname, avatar, is_tourist, create_time, update_time) VALUES " +
                    "('default', 'Test User 1', '', 0, NOW(), NOW()), " +
                    "('default', 'Test User 2', '', 0, NOW(), NOW()), " +
                    "('default', 'Test User 3', '', 0, NOW(), NOW()), " +
                    "('default', 'Tourist 1', '', 1, NOW(), NOW()), " +
                    "('default', 'Tourist 2', '', 1, NOW(), NOW())";

            int rows = stmt.executeUpdate(sql);
            System.out.println("插入了 " + rows + " 条测试数据");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
