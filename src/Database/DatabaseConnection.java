package Database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static final String URL = "jdbc:mysql://localhost:3306/tienlen";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    public static Connection connectionDB() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Kết nối đến cơ sở dữ liệu thành công!");

            return connection;

        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        System.out.println("Kết nối đến cơ sở dữ liệu Thất bại!");
        return null;

    }
}
