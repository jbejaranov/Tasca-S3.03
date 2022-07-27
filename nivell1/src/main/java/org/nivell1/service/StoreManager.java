package org.nivell1.service;

import org.nivell1.utils.ComparadorLlista;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

public class StoreManager {

    //Singleton de gestió de botigues

    private static StoreManager instance;
    private String storeName;

    //Private constructor
    private StoreManager() {

    }

    //Lazy initialiser
    public static StoreManager getInstance(String store) {
        if (instance == null) {
            instance = new StoreManager();
        }
        instance.setStoreName(store);
        return instance;
    }

    private void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public void addStore() {
        //TODO: crea arxchivo stock con nombre tienda
    }

    public void addProduct() {
        //TODO: añade producto a stock de tienda (busca archivo de tienda con este nombre)
        //TODO: si ya existe producto, añadir a la cantidad?
        //TODO: para implementar la parte de arriba, habria que hacer un equals de todos los campos del producto menos el ID
        //(p ej, del arbol -> si el nombre, precio y altura es igual)
    }

    public void deleteProduct() {
        //TODO: borra producto por número en pantalla
        deleteFromStock();
    }

    private List<List<String>> getOrderedProductList() {
        List<List<String>> list = readStockFromFile("stock");
        ComparadorLlista comparadorLlista = new ComparadorLlista();
        list.sort(comparadorLlista);
        return list;
    }

    public void showStock() {
        List<List<String>> list = getOrderedProductList();

//        List<List<String>> listDecoration;
//        List<List<String>> listFlower;
//        List<List<String>> listTree;
//
//        OptionalInt indexFlower = IntStream.range(0, list.size()).filter(i -> "flower".equals(list.get(i).get(0))).findFirst();
//        OptionalInt indexTree = IntStream.range(0, list.size()).filter(i -> "tree".equals(list.get(i).get(0))).findFirst();
//
//        //TODO: tractar Optional
//
//        listDecoration = list.subList(0, indexFlower.getAsInt());
//        listFlower = list.subList(indexFlower.getAsInt(), indexTree.getAsInt());
//        listTree = list.subList(indexTree.getAsInt(), list.size());
//
//        printTable(listDecoration, listFlower, listTree);

        Map<String, List<List<String>>> mapList = list.stream()
                .collect(Collectors.groupingBy(i -> i.get(0)));

        printTable(mapList);
    }

    public void getTotalValue() {
        List<List<String>> list = readStockFromFile("stock");
        double valor = list.stream()
                .map(element -> Double.parseDouble(element.get(2)) * Double.parseDouble(element.get(3)))
                .reduce(0d, Double::sum);

        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        System.out.println("El valor total dels productes de la botiga és: " + decimalFormat.format(valor) + " euros");
    }

    public void createTicket() {

        showStock();
        List<List<String>> stock = getOrderedProductList();

        //TODO: crear menú:
        /*
        0. Mostrar stock
        1. Menu cliente: 1) elegir producto (viendo stock), 2) ver todas compras, 0) salir
        2. cada vez que elige prod, eliminar de stock, add a ticket
        3. cuando sale, se crea timestamp y se guarda a historial
         */

        //List<Product> ->TIcket ticket = new Ticket(list)
        //ticket.getid -> crear arxiu ticket_id o ticket_timestamp

        //TODO: mirar tb que guarde el valor total. Tb se puede mostrar al cliente en finalizar la compra (cuando pulsa 0)
    }

    //TODO: mètode per canviar número de stock

    private void deleteFromStock() {
        showStock();
        List<List<String>> stock = getOrderedProductList();
        //TODO: scanner per demanar quin producte esborrar. imaginem que és el número 1
        //stock.remove(0);
        //TODO: canviar nom arxiu on escriu (currentStore)
        writeStockToFile(stock, "stock2");
    }

    private void addToHistory() {
        //TODO: recibe ticket, genera timestamp y lo guarda todo en el historial
    }

    public void showHistory() {
        //TODO: muestra el archivo Historial
    }

    public void showTotalSales() {
        //TODO: muestra suma del valor total de los tickets (ventas)
    }

    public List<List<String>> readStockFromFile(String fileName) {
        String inputFile = "nivell1/src/main/resources/" + fileName + ".txt";
        List<List<String>> listOfLists = new ArrayList<>();

        try {
            List<String> lines = Files.readAllLines(Paths.get(inputFile));
            lines.stream().filter(line -> !line.isEmpty()).toList().forEach(line -> {
                List<String> innerList = new ArrayList<>(Arrays.asList(line.split(",")));
                listOfLists.add(innerList);
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return listOfLists;
    }

    //Depende de como se estructure el fichero Historial puede que haya que hacer metodos a parte para él.

    public void writeStockToFile(List<List<String>> stock, String fileName) {
        //Convertir a llista de strings preparats pel csv
        List<String> stockToCSVList = stock.stream()
                .map(subList -> String.join(",", subList))
                .toList();

        //Escriure a l'arxiu
        String outputFile = "nivell1/src/main/resources/" + fileName + ".txt";
        try (PrintWriter pw = new PrintWriter(outputFile)) {
            stockToCSVList.forEach(pw::println);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private void printTable(Map<String, List<List<String>>> mapList) {
        int index = 1;

        if (mapList.containsKey("decoration")) {
            System.out.format("%-10s%-15s%-10s%-10s%-12s%-10s\n", "Numero", "Tipus", "Nom", "Preu(€)", "Quantitat", "Material");
            System.out.format("%-10s%-15s%-10s%-10s%-12s%-10s\n", "-------", "----------", "------", "---------", "-----------", "-----------");
            for (List<String> value : mapList.get("decoration")) {
                System.out.format("%-10s%-15s%-10s%-10s%-12s%-10s\n", index, value.get(0), value.get(1), value.get(2), value.get(3), value.get(4));
                index++;
            }
            System.out.println();
        }

        if (mapList.containsKey("flower")) {
            System.out.format("%-10s%-15s%-10s%-10s%-12s%-10s\n", "Numero", "Tipus", "Nom", "Preu (€)", "Quantitat", "Color");
            System.out.format("%-10s%-15s%-10s%-10s%-12s%-10s\n", "-------", "----------", "------", "---------", "-----------", "-----------");

            for (List<String> value : mapList.get("flower")) {
                System.out.format("%-10s%-15s%-10s%-10s%-12s%-10s\n", index, value.get(0), value.get(1), value.get(2), value.get(3), value.get(4));
                index++;
            }
            System.out.println();
        }

        if (mapList.containsKey("tree")) {
            System.out.format("%-10s%-15s%-10s%-10s%-12s%-10s\n", "Numero", "Tipus", "Nom", "Preu (€)", "Quantitat", "Altura (m)");
            System.out.format("%-10s%-15s%-10s%-10s%-12s%-10s\n", "-------", "----------", "------", "---------", "-----------", "-----------");

            for (List<String> value : mapList.get("tree")) {
                System.out.format("%-10s%-15s%-10s%-10s%-12s%-10s\n", index, value.get(0), value.get(1), value.get(2), value.get(3), value.get(4));
                index++;
            }
        }
    }
}
