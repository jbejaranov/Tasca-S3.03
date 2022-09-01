package org.nivell2.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

//Utility class: classe final, mètodes estàtics, constructor privat
public final class DBConnection {

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
        } catch (SQLException e) {
            System.out.println("Error: No s'ha pogut establir la connexió");
        }
    }

    public static Connection openConnection(String DBName) {

        if (connection == null) {
            setDBName(DBName);
            new DBConnection();
            System.out.println("Connexió a la base de dades exitosa");
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
