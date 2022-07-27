package org.nivell1.service;

import org.nivell1.products.Product;
import org.nivell1.products.Tree;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

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
        Map<String, List<String>> map = readFromFile("stock");
        map.values().forEach(System.out::println);
    }

    public void getTotalValue() {
        //TODO: muestra valor total de tienda
    }

    public void createTicket() {

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

    public Map<String, List<String>> readFromFile(String fileName) {
        Map<String, List<String>> map = new HashMap<>();
        String inputFile = "nivell1/src/main/resources/" + fileName + ".txt";
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                List<String> listContent = new ArrayList<>();
                Collections.addAll(listContent, line.split(",")[0],
                        line.split(",")[2],
                        line.split(",")[3],
                        line.split(",")[4],
                        line.split(",")[5]);
                map.put(line.split(",")[1], listContent);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return map;
    }

    //Depende de como se estructure el fichero Historial puede que haya que hacer metodos a parte para él.

    public void writeToFile() {
        //TODO: para escribir a un fichero
    }
}
