package org.nivell1.service;

import org.nivell1.stores.Ticket;
import org.nivell1.utils.ComparadorLlista;

import java.io.*;
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
        // TODO: crea arxchivo stock con nombre tienda

        String fileName = "nivell1/src/main/resources/" + storeName + ".txt";
        File florist = new File(fileName);

        try (FileOutputStream file = new FileOutputStream(fileName, true);
             OutputStreamWriter out = new OutputStreamWriter(file);
             BufferedWriter bw = new BufferedWriter(out);
             PrintWriter writer = new PrintWriter(bw)) {

        } catch (IOException ioe) {

            ioe.printStackTrace();
        }

        if (florist.exists()) {
            System.out.println("La floristeria que vol introduir ja es troba registrada. ");
        } else {
            //TODO: crear arxiu
        }

    }

    public void addProduct() {
        // TODO: añade producto a stock de tienda (busca archivo de tienda con este
        // nombre)
        // TODO: si ya existe producto, añadir a la cantidad?
        // TODO: para implementar la parte de arriba, habria que hacer un equals de
        // todos los campos del producto menos el ID
        // (p ej, del arbol -> si el nombre, precio y altura es igual)

        //String fileName = "nivell1/src/main/resources/" + storeName + ".txt";
        String fileName = "nivell1/src/main/resources/" + "prova" + ".txt";
        File florist = new File(fileName);

        List<String> questionsList = questions();

        //TODO: mirar si ja existeix
        String toCSV = String.join(",", questionsList);

        try (PrintWriter pw = new PrintWriter(fileName)) {
            pw.println(toCSV);
            System.out.println("Producte afegit");
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public List<String> questions() {
        Scanner scan = new Scanner(System.in);

        List<String> questionsList = new ArrayList<>();

        System.out.println("Quin producte vol introduïr? (Escriu el nom en anglès) ");
        String product = scan.nextLine();

        System.out.println("Introdueix el nom de " + product);
        String name = scan.nextLine();

        System.out.println("Introdueix el preu de " + product);
        String price = scan.nextLine();

        System.out.println("Introdueix la quantitat de " + product);
        String quantity = scan.nextLine();

        String property = "";

        if (product.equalsIgnoreCase("tree")) {

            System.out.println("Introdueix l'altura de l'arbre: ");
            property = scan.nextLine();

        } else if (product.equalsIgnoreCase("flower")) {

            System.out.println("Introdueix el color de les flors: ");
            property = scan.nextLine();

        } else if (product.equalsIgnoreCase("decoration")) {

            System.out.println("Introdueix el tipus de material: ");
            property = scan.nextLine();
        }

        questionsList.add(product);
        questionsList.add(name);
        questionsList.add(price);
        questionsList.add(quantity);
        questionsList.add(property);

        return questionsList;
    }

    public void deleteProduct() {
        //TODO: borra producto por número en pantalla
        deleteFromStock();
    }

    private List<List<String>> getOrderedProductList() {
        List<List<String>> list = readProductsFromFile("stock");
        ComparadorLlista comparadorLlista = new ComparadorLlista();
        list.sort(comparadorLlista);
        return list;
    }

    public void showStock() {
        List<List<String>> list = getOrderedProductList();
        Map<String, List<List<String>>> mapList = list.stream()
                .collect(Collectors.groupingBy(i -> i.get(0)));

        printTable(mapList);
    }

    public void getTotalValue() {
        List<List<String>> list = readProductsFromFile("stock");
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
        2. cada vez que elige prod, eliminar o actualizar de stock, add a ticket
        3. cuando sale, se crea timestamp y se guarda a historial
         */

        //Assumim llista de productes:


        Ticket ticket = new Ticket(stock); //TODO: CANVIAR!!
        writeProductsToFile(stock, "Ticket_" + storeName + "_" + ticket.getId());

        //List<Product> ->TIcket ticket = new Ticket(list)
        //ticket.getid -> crear arxiu ticket_id o ticket_timestamp

        //TODO: mirar tb que guarde el valor total. Tb se puede mostrar al cliente en finalizar la compra (cuando pulsa 0)
    }

    //TODO: mètode per canviar número de stock

    public void updateStock() {
        showStock();
        List<List<String>> stock = getOrderedProductList();

        //TODO: menú per triar quin stock actualitzar. Suposem que l'1:
        int lineToUpdate = 0;
        int newStock = 9; //Nova quantitat
        stock.get(lineToUpdate).set(3, String.valueOf(newStock));
        //Seguim demanant... Quan 0:
        writeProductsToFile(stock, "stock2");
    }

    private void deleteFromStock() {
        showStock();
        List<List<String>> stock = getOrderedProductList();
        //TODO: scanner per demanar quin producte esborrar. imaginem que és el número 1
        //stock.remove(0);
        //TODO: canviar nom arxiu on escriu (currentStore)
        writeProductsToFile(stock, "stock2");
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

    public List<List<String>> readProductsFromFile(String fileName) {
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

    private List<String> convertToCSVStringList(List<List<String>> list) {
        return list.stream()
                .map(subList -> String.join(",", subList))
                .toList();
    }

    public void writeProductsToFile(List<List<String>> productList, String fileName) {
        //Convertir a llista de strings preparats pel csv
        List<String> stockToCSVList = convertToCSVStringList(productList);

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
