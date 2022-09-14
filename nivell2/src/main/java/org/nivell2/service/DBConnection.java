package org.nivell2.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

//Utility class: classe final, mètodes estàtics, constructor privat
public final class DBConnection {

    static {
        Logger.getLogger("org.mongodb.driver").setLevel(Level.OFF);
        Logger.getLogger("org.bson").setLevel(Level.OFF);
    }

    private static final String USERNAME = "root";
    private static final String PASSWORD = "";
    private static final String BASE_URL = "jdbc:mysql://localhost:3306/%s?serverTimezone=UTC";
    private static String DBName;
    private static Connection connection;

    public static void setDBName(String DBName) {
        DBConnection.DBName = DBName;
    }

    private DBConnection() {

        try {
            String URL = String.format(BASE_URL, DBName);
            connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            System.out.println("Connexió a la base de dades exitosa");
        } catch (SQLException e) {
            System.out.println("Error: No s'ha pogut establir la connexió");
        }
    }

    public static Connection openConnection(String DBName) {

        if (connection == null) {
            setDBName(DBName);
            new DBConnection();
        }
        return connection;
    }

    public static void closeConnection() {

        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                System.out.println("Error: No s'ha pogut tancar la connexió");
            }
        }
    }
}
