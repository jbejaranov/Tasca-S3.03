package org.nivell1;

import org.nivell1.service.StoreManager;

import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Main {
    /*
    TODO: documentar mètodes

     */
    private static String currentStore;
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {

        //Menu
        int select;

        do {
            printMenu();

            select = scanner.nextInt();
            scanner.nextLine();

            switch (select) {
                //Introduir una botiga
                case 1 -> {
                    currentStore = readName();
                    StoreManager.getInstance(currentStore).addStore();
                }

                //Canviar botiga
                case 2 -> currentStore = readName();

                //Introduir producte a botiga
                case 3 -> StoreManager.getInstance(currentStore).addProduct();

                //Actualitzar producte a botiga
                case 4 -> StoreManager.getInstance(currentStore).updateStock();

                //Esborrar producte de botiga
                case 5 -> StoreManager.getInstance(currentStore).deleteProduct();

                //Mostrar stock amb quantitats
                case 6 -> StoreManager.getInstance(currentStore).showStock();

                //Mostrar valor total
                case 7 -> StoreManager.getInstance(currentStore).getTotalValue();

                //Crear ticket
                case 8 -> StoreManager.getInstance(currentStore).createTicket(); //TODO: Ernest

                //Mostrar vendes (historial)
                case 9 -> StoreManager.getInstance(currentStore).showHistory(); //TODO: Teresa

                //Mostrat total diners vendes
                case 10 -> StoreManager.getInstance(currentStore).showTotalSales(); //TODO:Juan

                //Proves (TODO: ELIMINAR!!)
//                case 11 ->

                //Sortir del programa
                case 0 -> System.out.println("Sortint del programa");

            }

        } while (select != 0);
    }

    public static void printMenu() {
        System.out.println("""
                -------------------
                Escolliu una opció:
                1: Introduir una nova botiga
                2: Seleccionar la botiga en ús
                3: Afegir un producte a una botiga
                4: Actualitzar un producte
                5: Esborrar un producte
                6: Mostrar stock total de la botiga
                7: Mostrar valor total del stock de la botiga
                8: Crear un nou tiquet de compra
                9: Mostrar historial de vendes
                10: Mostrar total guanyat amb les vendes
                0: Sortir
                ------------------
                """);
    }

    public static String readName() {
        //Arreglar
        System.out.println("Introdueix el nom de la nova botiga: ");
        return scanner.nextLine();
    }
}