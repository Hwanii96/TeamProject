package model;

import java.sql.*;

public class JDBCUtil {

    //	상수화된 변수들은 반복적으로 수정하는 경우가 생기기에 최상단에 위치 한다.

    static final String driverName_MySQL = "com.mysql.cj.jdbc.Driver";
    static final String url_MySQL = "jdbc:mysql://localhost/starlight";
    static final String userName = "root";
    static final String password = "1234";

    public static Connection connect() {

        try {
            Class.forName(driverName_MySQL);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }

        Connection conn = null;

        try {
            conn = DriverManager.getConnection(url_MySQL, userName, password);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        return conn;
    }

    //	인자의 타입 또는 개수가 다르게 해서 메서드 오버로딩.
    public static void disconnect(PreparedStatement pstmt, Connection conn) {

        try {
            pstmt.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //	인자의 타입 또는 개수가 다르게 해서 메서드 오버로딩.
    public static void disconnect(ResultSet rs, PreparedStatement pstmt, Connection conn) {

        try {
            rs.close();
            pstmt.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}    //	JDBCUtil
