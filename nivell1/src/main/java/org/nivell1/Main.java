package org.nivell1;

import org.nivell1.service.StoreManager;

import java.util.Scanner;

public class Main {
    /*
    TODO: toString a products

     */

    private static String currentStore;
    private static final Scanner scanner = new Scanner(System.in);

    //Idea: selecciona botiga i fes totes operacions a aquesta botiga. mètode al menu per canviar de botiga
    //En comptes de demanar la botiga cada vegada

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

                //
                case 2 -> {}

                //...

                //Sortir del programa
                case 0 -> System.out.println("Sortint del programa");

            }

        } while (select != 0);
    }

    public static void printMenu() {

        System.out.println("""
                -------------------
                Escolliu una opció:
                1: Introduir una botiga
                2: Select store...
                3: 
                4: 
                5: 
                6: 
                7: 
                8:
                0: Sortir
                ------------------
                """);

    }

    public static String readName() {
        //Arreglar
        System.out.println("Intro name store: ");
        return scanner.nextLine();
    }
}