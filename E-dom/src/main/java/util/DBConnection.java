package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    // Aiven cloud podaci
    private static final String URL = "jdbc:mysql://mysql-1be47ebf-edom.c.aivencloud.com:13425/defaultdb?sslMode=REQUIRED&allowPublicKeyRetrieval=true";
    private static final String USERNAME = "avnadmin";
    private static final String PASSWORD = "AVNS_MBksTmnZOfyGMypiW78";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USERNAME, PASSWORD);
    }
}
