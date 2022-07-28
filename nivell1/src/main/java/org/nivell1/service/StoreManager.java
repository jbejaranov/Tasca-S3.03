package org.nivell1.service;

import org.nivell1.stores.Ticket;
import org.nivell1.utils.ComparadorLlista;

import javax.sound.midi.Soundbank;
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

    private static final Scanner scanner = new Scanner(System.in);

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
        // TODO: mirar de fer la capçalera de l'arxiu

        String fileName = "nivell1/src/main/resources/" + storeName + ".txt";
        File florist = new File(fileName);

        if (florist.exists()) {
            System.out.println("La floristeria que vol introduir ja es troba registrada. ");
        } else {
            try (FileOutputStream file = new FileOutputStream(fileName, true);
                 OutputStreamWriter out = new OutputStreamWriter(file);
                 BufferedWriter bw = new BufferedWriter(out);
                 PrintWriter writer = new PrintWriter(bw)) {
                System.out.println("Botiga creada");
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }

    public void addProduct() {
        //TODO: canviar a storeName
        //String fileName = "nivell1/src/main/resources/" + storeName + ".txt";
        String fileName = "nivell1/src/main/resources/" + "prova" + ".txt";
        File florist = new File(fileName);

        List<String> newProduct = addProductQuestions();

        //Mirar si un producte ja existeix
        List<List<String>> stock = getOrderedProductList();
        List<List<String>> productAlreadyExists = stock.stream()
                .filter(subList -> newProduct.get(0).equals(subList.get(0)) &&
                        newProduct.get(1).equals(subList.get(1)) &&
                        newProduct.get(1).equals(subList.get(2)) &&
                        newProduct.get(4).equals(subList.get(4))).toList();

        //Si ja existeix
        if (!productAlreadyExists.isEmpty()) {
            //Fes flat la llista
            List<String> productAlreadyExistsFlat = productAlreadyExists.stream().flatMap(List::stream).toList();
            //Troba l'índex
            int indexOf = stock.indexOf(productAlreadyExistsFlat);
            //Suma la quantitat de productes antiga i la nova
            newProduct.set(3, String.valueOf(Integer.parseInt(productAlreadyExistsFlat.get(3)) +
                    Integer.parseInt(newProduct.get(3))));
            //Actualitza les quantitats
            stock.set(indexOf, newProduct);
            //Desa tot el stock de nou
            writeProductsToFile(stock, "stock");
            System.out.println("Actualitzada quantitat de productes");
        } else {
            //Si no existeix:
            //Converteix a format csv
            String toCSV = String.join(",", newProduct);
            //Escriu l'arxiu
            try (PrintWriter pw = new PrintWriter(fileName)) {
                pw.println(toCSV);
                System.out.println("Producte afegit");

                //Desa-ho de manera ordenada
                List<List<String>> orderedList = getOrderedProductList();
                writeProductsToFile(orderedList, "stock");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public List<String> addProductQuestions() {
        List<String> questionsList = new ArrayList<>();

        System.out.println("Quin producte voleu introduïr? (Escriviu el nom en anglès) ");
        String product = scanner.nextLine();

        System.out.println("Introdueix el nom de " + product);
        String name = scanner.nextLine();

        System.out.println("Introdueix el preu de " + product);
        String price = scanner.nextLine();

        System.out.println("Introdueix la quantitat de " + product);
        String quantity = scanner.nextLine();

        if (product.equalsIgnoreCase("tree")) {
            System.out.println("Introdueix l'altura de l'arbre: ");
        } else if (product.equalsIgnoreCase("flower")) {
            System.out.println("Introdueix el color de les flors: ");
        } else if (product.equalsIgnoreCase("decoration")) {
            System.out.println("Introdueix el tipus de material: ");
        }

        String property = scanner.nextLine();

        Collections.addAll(questionsList, product, name, price, quantity, property);

        return questionsList;
    }

    public void deleteProduct() {
        showStock();
        List<List<String>> stock = getOrderedProductList();

        System.out.println("Introduïu número del producte a eliminar:");
        int removeProduct = scanner.nextInt();
        scanner.nextLine();

        stock.remove(removeProduct - 1);

        //TODO: canviar nom arxiu on escriu (currentStore)
        writeProductsToFile(stock, "stock2");
    }

    private List<List<String>> getOrderedProductList() {
        //TODO: canviar nom arxiu
        List<List<String>> list = readProductsFromFile("stock");
        ComparadorLlista comparadorLlista = new ComparadorLlista();
        list.sort(comparadorLlista);
        return list;
    }

    public void showStock() {
        //Obtenim llista de productes ordenada
        List<List<String>> list = getOrderedProductList();
        //Creem un mapa amb llistes de productes classificades per tipus
        Map<String, List<List<String>>> mapList = list.stream()
                .collect(Collectors.groupingBy(i -> i.get(0)));
        //Mostrem els productes per pantalla
        printTable(mapList);
    }

    public void getTotalValue() {
        //TODO: canviar nom arxiu input
        //Obtenim la llista de productes al stock
        List<List<String>> list = readProductsFromFile("stock");
        //Iterem sobre la llisa de productes i multipliquem preu (índex 2) per quantitat (índex 3)
        double valor = list.stream()
                .map(element -> Double.parseDouble(element.get(2)) * Double.parseDouble(element.get(3)))
                .reduce(0d, Double::sum);
        //Format amb 2 decimals
        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        System.out.println("El valor total dels productes de la botiga és: " + decimalFormat.format(valor) + " euros");
    }

    public void createTicket() {

        List<List<String>> stock = getOrderedProductList();

        List<List<String>> productesTiquet = new ArrayList<>();

        int select;

        do {
            System.out.println("""
                    -------------------
                    Escolliu una opció:
                    1: Escollir producte
                    2: Veure tots els productes seleccionats
                    0: Acabar amb la compra i generar tiquet
                    ------------------
                    """);

            select = scanner.nextInt();
            scanner.nextLine();

            switch (select) {
                //Escollir un producte
                case 1 -> {
                    showStock();

                    System.out.println("Introduïu número del producte que desitgeu comprar:");
                    int numProducte = scanner.nextInt() - 1;
                    scanner.nextLine();

                    System.out.println("Introduïu quantitat de productes a posar al carro:");
                    int quantitatProducte = scanner.nextInt();
                    scanner.nextLine();

                    int quantitatStock = Integer.parseInt(stock.get(numProducte).get(3));

                    if (quantitatProducte == quantitatStock) {
                        stock.remove(numProducte);
                        productesTiquet.add(stock.get(numProducte));
                    } else if (quantitatProducte < quantitatStock) {
                        List<String> addToTiquet = new ArrayList<>(stock.get(numProducte));
                        addToTiquet.set(3, String.valueOf(quantitatProducte));
                        productesTiquet.add(addToTiquet);

                        stock.get(numProducte).set(3, String.valueOf(quantitatStock - quantitatProducte));
                        System.out.println("Producte afegit al tiquet");
                        //TODO: afegir a historial
                    } else {
                        System.out.println("No podeu adquirir més articles dels que hi ha disponibles, torneu a provar");
                    }
                }

//              //Veure tots els productes ja seleccionats
                case 2 -> productesTiquet.forEach(System.out::println);

                //Acabar el tiquet
                case 0 -> System.out.println("Processant la compra");
            }
        } while (select != 0);

        Ticket ticket = new Ticket(productesTiquet);

        writeProductsToFile(stock, "stock");
        writeProductsToFile(productesTiquet, "Ticket_" + storeName + "_" + ticket.getId());
    }

    //TODO: mètode per canviar número de stock

    public void updateStock() {
        List<List<String>> stock = getOrderedProductList();
        showStock();

        System.out.println("Escolliu quin producte voleu actualitzar:");

        int productNum = scanner.nextInt() - 1;
        scanner.nextLine();

        List<String> product = new ArrayList<>(stock.get(productNum ));

        System.out.println("Escolliu la propietat del producte que voleu canviar:");
        System.out.println("""
                1 - Nom
                2 - Preu
                3 - Quantitat
                4 - Material/Color/Alçada
                """);

        int property = scanner.nextInt();
        scanner.nextLine();

        System.out.println("Introduïu el nou valor:");

        String newValue = scanner.nextLine();
        product.set(property, newValue);

        stock.set(productNum, product);
        System.out.println("Producte actualizat");

        writeProductsToFile(stock, "stock");
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
            lines.stream()
                    .filter(line -> !line.isEmpty())
                    .toList()
                    .forEach(line -> listOfLists.add(convertCSVToStringList(line)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return listOfLists;
    }

    //Depende de como se estructure el fichero Historial puede que haya que hacer metodos a parte para él.

    private List<String> convertToCSVList(List<List<String>> list) {
        return list.stream()
                .map(subList -> String.join(",", subList))
                .toList();
    }

    private List<String> convertCSVToStringList(String csvString) {
        return new ArrayList<>(Arrays.asList(csvString.split(",")));
    }

    public void writeProductsToFile(List<List<String>> productList, String fileName) {
        //Convertir a llista de strings preparats pel csv
        List<String> stockToCSVList = convertToCSVList(productList);

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
