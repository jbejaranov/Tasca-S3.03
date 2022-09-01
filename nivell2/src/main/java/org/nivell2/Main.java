package org.nivell2;

import org.nivell2.service.DBConnection;
import org.nivell2.service.StoreManager;

import java.sql.*;
import java.util.Scanner;

public class Main {
    private static final String currentStore = "floristeria";
    private static final Connection connection = DBConnection.openConnection(currentStore);
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {

        //Menú
        int select;

        do {
            printMenu();

            select = scanner.nextInt();
            scanner.nextLine();

            switch (select) {

                //Introduir producte a botiga
                case 1 -> StoreManager.getInstance(connection).addProduct();

                //Actualitzar producte a botiga
                case 2 -> StoreManager.getInstance(connection).updateStock();

                //Esborrar producte de botiga
                case 3 -> StoreManager.getInstance(connection).deleteProduct();

                //Mostrar stock amb quantitats
                case 4 -> StoreManager.getInstance(connection).showStock();

                //Mostrar valor total
                case 5 -> StoreManager.getInstance(connection).getTotalValue();

                //Crear ticket
                case 6 -> StoreManager.getInstance(connection).generateTicket();

                //Mostrar vendes (historial)
                case 7 -> StoreManager.getInstance(connection).showHistory();

                //Mostrat total diners vendes
                case 8 -> StoreManager.getInstance(connection).showTotalSales();

                //Sortir del programa
                case 0 -> DBConnection.closeConnection();
            }

        } while (select != 0);

        System.out.println("Sortint del programa");
    }

    public static void printMenu() {
        System.out.println("""
                -------------------
                Escolliu una opció:
                1: Afegir un producte a una botiga
                2: Actualitzar un producte
                3: Esborrar un producte
                4: Mostrar stock total de la botiga
                5: Mostrar valor total del stock de la botiga
                6: Crear un nou tiquet de compra
                7: Mostrar historial de vendes
                8: Mostrar total guanyat amb les vendes
                0: Sortir
                ------------------
                """);
    }
}