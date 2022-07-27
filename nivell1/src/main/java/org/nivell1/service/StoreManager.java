package org.nivell1.service;

import org.nivell1.utils.ComparadorLlista;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
        //TODO: borra producto por ID del stock
    }

    public void showStock() {
        List<List<String>> list = readFromFile("stock");
        ComparadorLlista comparadorLlista = new ComparadorLlista();
        list.sort(comparadorLlista);

        List<List<String>> listDecoration;
        List<List<String>> listFlower;
        List<List<String>> listTree;

        OptionalInt indexFlower = IntStream.range(0, list.size()).filter(i -> "flower".equals(list.get(i).get(0))).findFirst();
        OptionalInt indexTree = IntStream.range(0, list.size()).filter(i -> "tree".equals(list.get(i).get(0))).findFirst();

        //TODO: tractar Optional

        listDecoration = list.subList(0, indexFlower.getAsInt());
        listFlower = list.subList(indexFlower.getAsInt(), indexTree.getAsInt());
        listTree = list.subList(indexTree.getAsInt(), list.size());

        int index = 1;
        System.out.format("%-10s%-15s%-10s%-10s%-12s%-10s\n", "Numero", "Tipus", "Nom", "Preu(€)", "Quantitat", "Material");
        System.out.format("%-10s%-15s%-10s%-10s%-12s%-10s\n", "-------", "----------", "------", "---------", "-----------", "-----------");
        for (List<String> value : listDecoration) {
            System.out.format("%-10s%-15s%-10s%-10s%-12s%-10s\n", index, value.get(0), value.get(1), value.get(2), value.get(3), value.get(4));
            index++;
        }
        System.out.println();
        System.out.format("%-10s%-15s%-10s%-10s%-12s%-10s\n", "Numero", "Tipus", "Nom", "Preu (€)", "Quantitat", "Color");
        System.out.format("%-10s%-15s%-10s%-10s%-12s%-10s\n", "-------", "----------", "------", "---------", "-----------", "-----------");

        for (List<String> value : listFlower) {
            System.out.format("%-10s%-15s%-10s%-10s%-12s%-10s\n", index, value.get(0), value.get(1), value.get(2), value.get(3), value.get(4));
            index++;
        }
        System.out.println();
        System.out.format("%-10s%-15s%-10s%-10s%-12s%-10s\n", "Numero", "Tipus", "Nom", "Preu (€)", "Quantitat", "Altura (m)");
        System.out.format("%-10s%-15s%-10s%-10s%-12s%-10s\n", "-------", "----------", "------", "---------", "-----------", "-----------");

        for (List<String> value : listTree) {
            System.out.format("%-10s%-15s%-10s%-10s%-12s%-10s\n", index, value.get(0), value.get(1), value.get(2), value.get(3), value.get(4));
            index++;
        }
    }

    public void getTotalValue() {
        //TODO: muestra valor total de tienda
    }

    public void createTicket() {

        showStock();

        List<List<String>> list = readFromFile("stock");
        ComparadorLlista comparadorLlista = new ComparadorLlista();
        list.sort(comparadorLlista);

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

    private void deleteFromStock() {
        //TODO: recibe ID del producto a borrar, lee del archivo, lo busca, y lo elimina
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

    public List<List<String>> readFromFile(String fileName) {
        //TODO: eliminar ultima linea
        List<List<String>> list = new ArrayList<>();
        String inputFile = "nivell1/src/main/resources/" + fileName + ".txt";
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                List<String> listContent = new ArrayList<>();
                Collections.addAll(listContent, line.split(",")[0],
                        line.split(",")[1],
                        line.split(",")[2],
                        line.split(",")[3],
                        line.split(",")[4]);
                list.add(listContent);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    //Depende de como se estructure el fichero Historial puede que haya que hacer metodos a parte para él.

    public void writeToFile() {
        //TODO: para escribir a un fichero
    }
}
