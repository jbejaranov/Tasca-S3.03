package org.nivell1;

import org.nivell1.service.StoreManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Main {

    private static String currentStore;
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {

        //Escull botiga en ús (s'executa un cop)
        selectStore();

        //Menú
        int select;

        do {
            printMenu();

            select = scanner.nextInt();
            scanner.nextLine();

            switch (select) {
                //Introduir una botiga
                case 1 -> StoreManager.getInstance(currentStore).addStore();

                //Canviar botiga
                case 2 -> selectStore();

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
                case 8 -> StoreManager.getInstance(currentStore).createTicket();

                //Mostrar vendes (historial)
                case 9 -> StoreManager.getInstance(currentStore).showHistory();

                //Mostrat total diners vendes
                case 10 -> StoreManager.getInstance(currentStore).showTotalSales();

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

    public static void selectStore() {

        //Obté llistat d'arxius
        Map<Integer, String> filesMap = listFiles();
        filesMap.keySet().forEach(key -> System.out.println(key + " - " + filesMap.get(key)));

        System.out.println("\nEscolliu el número de la botiga que voleu gestionar:");
        Integer storeIndex = scanner.nextInt();
        scanner.nextLine();

        //Selecciona arxiu en ús
        if (!filesMap.containsKey(storeIndex)) {
            System.out.println("La botiga seleccionada no existeix, premeu 1 si la voleu crear.");
        } else {
            currentStore = filesMap.get(storeIndex);
            System.out.println("Botiga seleccionada correctament");
        }
    }

    private static Map<Integer, String> listFiles() {

        System.out.println("Botigues actuals:\n");

        //Predicats per filtrar historial de vendes i tickets
        Predicate<String> isHistory = name -> !name.endsWith("_History");
        Predicate<String> isTicket = name -> !name.startsWith("Ticket");

        List<String> filesList;

        //Elimina les extensions dels arxius i filtra
        try (Stream<Path> files = Files.walk(Paths.get("nivell1/src/main/resources/"))) {
            filesList = files.filter(Files::isRegularFile)
                    .map(Path::getFileName)
                    .map(name -> name.toString().replaceFirst("[.][^.]+$", ""))
                    .filter(isHistory)
                    .filter(isTicket)
                    .toList();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        //Posa els noms dels arxius en un mapa d'1 a n
        Map<Integer, String> map = new HashMap<>();
        IntStream.rangeClosed(1, filesList.size()).forEach(value -> map.put(value, filesList.get(value - 1)));

        return map;
    }
}