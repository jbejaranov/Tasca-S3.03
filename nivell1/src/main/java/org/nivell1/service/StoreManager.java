package org.nivell1.service;

import org.nivell1.products.Decoration;
import org.nivell1.products.Flower;
import org.nivell1.products.Product;
import org.nivell1.products.Tree;
import org.nivell1.utils.Ticket;
import org.nivell1.utils.ComparadorProducte;
import org.nivell1.utils.PropertyFilter;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class StoreManager {

    //Singleton de gestió de botigues

    private static final Scanner scanner = new Scanner(System.in);

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

        //Obtenim el nom nou
        System.out.println("\nEscolliu el nom de la nova botiga:");
        String newStore = scanner.nextLine();

        String fileName = "nivell1/src/main/resources/" + newStore + ".txt";
        File florist = new File(fileName);

        boolean created;

        //Intentem crear el fitxer
        try {
            created = florist.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        //Mirem si s'ha creat
        if (created) {
            System.out.println("Botiga creada");
        } else {
            System.out.println("La floristeria que voleu introduir ja es troba registrada.");
        }
    }

    public void addProduct() {
        String fileName = "nivell1/src/main/resources/" + storeName + ".txt";

        //Obtenim el nou producte
        Product newProduct = addProductQuestions();

        //Mirar si un producte ja existeix
        List<Product> stock = getOrderedProductList();

        PropertyFilter propertyFilter = new PropertyFilter();

        Optional<Product> productAlreadyExists = stock.stream()
                .filter(product -> product.getClass().getSimpleName().equals(newProduct.getClass().getSimpleName()) &&
                        product.getName().equals(newProduct.getName()) &&
                        product.getPrice() == newProduct.getPrice())
                .filter(propertyFilter.filterProperty(newProduct))
                .findAny();

        //Si ja existeix
        if (productAlreadyExists.isPresent()) {

            //Troba l'índex
            int indexOf = stock.indexOf(productAlreadyExists.get());

            //Suma la quantitat de productes antiga i la nova
            stock.get(indexOf).setQuantity(productAlreadyExists.get().getQuantity() + newProduct.getQuantity());

            //Desa tot el stock de nou
            writeProductsToFile(stock, storeName);
            System.out.println("Actualitzada quantitat de productes");

        } else {
            //Si no existeix:

            //Converteix a format csv
            String toCSV = convertProductToCSV(newProduct);

            //Escriu a l'arxiu
            try (PrintWriter pw = new PrintWriter(new FileWriter(fileName, true))) {
                //Si és el primer producte, escriu la capçalera també
                if (stock.isEmpty()) {
                    pw.println("type,name,price,quantity,property");
                }

                pw.println(toCSV);
                System.out.println("Producte afegit");

            } catch (IOException e) {
                e.printStackTrace();
            }

            //Desa-ho de manera ordenada
            List<Product> orderedList = getOrderedProductList();
            writeProductsToFile(orderedList, storeName);
        }
    }

    public Product addProductQuestions() {
        Product product = null;

        System.out.println("Quin tipus de producte voleu introduïr? (Escolliu entre: decoration - flower - tree) ");
        String productType = scanner.nextLine();

        //Validació del tipus de producte
        while (!(productType.equalsIgnoreCase("tree") ||
                productType.equalsIgnoreCase("flower") ||
                productType.equalsIgnoreCase("decoration"))) {

            System.out.println("Tipus no vàlid, torneu a provar:");
            productType = scanner.nextLine();
        }

        System.out.println("Introdueix el nom de " + productType);
        String name = scanner.nextLine();

        System.out.println("Introdueix el preu de " + productType);
        String price = scanner.nextLine();

        System.out.println("Introdueix la quantitat de " + productType);
        String quantity = scanner.nextLine();

        switch (productType.toLowerCase()) {
            case "decoration" -> System.out.println("Introdueix el tipus de material: ");
            case "flower" -> System.out.println("Introdueix el color de les flors: ");
            case "tree" -> System.out.println("Introdueix l'altura de l'arbre (m): ");
        }

        String property = scanner.nextLine();

        //Creem el nou objecte
        switch (productType.toLowerCase()) {
            case "decoration" ->
                    product = new Decoration(name, Float.parseFloat(price), Integer.parseInt(quantity), property);
            case "flower" -> product = new Flower(name, Float.parseFloat(price), Integer.parseInt(quantity), property);
            case "tree" ->
                    product = new Tree(name, Float.parseFloat(price), Integer.parseInt(quantity), Double.parseDouble(property));
        }

        return product;
    }

    public void deleteProduct() {
        showStock();
        List<Product> stock = getOrderedProductList();

        System.out.println("Introduïu número del producte a eliminar:");
        int removeProduct = scanner.nextInt();
        scanner.nextLine();

        if (removeProduct > 0 && removeProduct < stock.size()) {
            stock.remove(removeProduct - 1);
        } else {
            System.out.println("El producte seleccionat no existeix");
        }

        writeProductsToFile(stock, storeName);
    }

    private List<Product> getOrderedProductList() {

        //Obtenim llista ordenada de productes, per tipus i nom (alfabètic)
        List<Product> list = readProductsFromStock();
        Comparator<Product> comparadorProducte = new ComparadorProducte().thenComparing(Product::getName);
        list.sort(comparadorProducte);
        return list;
    }

    public void showStock() {
        //Obtenim llista de productes ordenada
        List<Product> list = getOrderedProductList();

        //Creem un mapa amb llistes de productes classificades per tipus i ordenades alfabèticament
        Map<String, List<Product>> treeMapList = list.stream().sorted(Comparator.comparing(Product::getName))
                .collect(Collectors.groupingBy(i -> i.getClass().getSimpleName(), TreeMap::new, Collectors.toList()));

        //Mostrem els productes per pantalla
        printTable(treeMapList);
    }

    public void showTicket(List<Product> products) {

        //Creem un mapa amb llistes de productes classificades per tipus
        Map<String, List<Product>> treeMapList = products.stream().sorted(Comparator.comparing(Product::getName))
                .collect(Collectors.groupingBy(i -> i.getClass().getSimpleName(), TreeMap::new, Collectors.toList()));

        //Mostrem els productes per pantalla
        printTable(treeMapList);
    }

    public void getTotalValue() {
        //Obtenim la llista de productes al stock
        List<Product> list = readProductsFromStock();

        //Iterem sobre la llisa de productes i multipliquem preu per quantitat
        float valor = list.stream()
                .map(product -> product.getPrice() * product.getQuantity())
                .reduce(0f, Float::sum);

        //Format amb 2 decimals
        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        System.out.println("El valor total dels productes de la botiga és: " + decimalFormat.format(valor) + " euros");
    }

    public void createTicket() {

        List<Product> stock;
        List<Product> ticketProducts = new ArrayList<>();

        //Menú
        int select;

        do {
            stock = getOrderedProductList();

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

                    //Si es pot efectuar la compra
                    if (numProducte <= stock.size()) {

                        //Quant de stock hi ha disponible
                        int quantitatStock = stock.get(numProducte).getQuantity();

                        //Si volem comprar-ho tot, s'esborra del magatzem
                        if (quantitatProducte == quantitatStock) {
                            stock.remove(numProducte);
                            writeProductsToFile(stock, storeName);
                            ticketProducts.add(stock.get(numProducte));
                            System.out.println("Producte afegit al tiquet");

                        //Si no, actualitzem el producte
                        } else if (quantitatProducte < quantitatStock) {
                            Product addToTiquet = stock.get(numProducte);

                            //Hem de clonar l'objecte amb la nova quantitat canviada
                            Product newProduct = null;
                            switch (addToTiquet.getClass().getSimpleName()) {
                                case "Decoration" ->
                                        newProduct = new Decoration(addToTiquet.getName(), addToTiquet.getPrice(), quantitatProducte, ((Decoration) addToTiquet).getMaterial());
                                case "Flower" ->
                                        newProduct = new Flower(addToTiquet.getName(), addToTiquet.getPrice(), quantitatProducte, ((Flower) addToTiquet).getColor());
                                case "Tree" ->
                                        newProduct = new Tree(addToTiquet.getName(), addToTiquet.getPrice(), quantitatProducte, ((Tree) addToTiquet).getHeight());
                            }

                            //Afegim a la llista de tickets
                            ticketProducts.add(newProduct);

                            //Actualitzem la quantitat i escrivim a arxiu
                            stock.get(numProducte).setQuantity(quantitatStock - quantitatProducte);
                            writeProductsToFile(stock, storeName);
                            System.out.println("Producte afegit al tiquet");
                        } else {
                            System.out.println("No podeu adquirir més articles dels que hi ha disponibles, torneu a provar");
                        }
                    } else {
                        System.out.println("El producte seleccionat no existeix");
                    }
                }

//              //Veure tots els productes ja seleccionats
                case 2 -> showTicket(ticketProducts);

                //Acabar el tiquet
                case 0 -> System.out.println("Processant la compra");
            }
        } while (select != 0);

        //Desem el ticket i actualizem l'historial
        Ticket ticket = new Ticket(ticketProducts);
        writeTicket(ticket);
        addToHistory(ticketProducts);
    }

    private void writeTicket(Ticket ticket) {
        //Convertir a llista de strings preparats pel csv i obtenir la data i l'hora del moment d'escriptura
        List<String> productsToCSVList = convertToCSVList(ticket.getList());
        LocalDateTime dateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String dateTimeFormatted = dateTime.format(formatter);

        //Escriure a l'arxiu
        String fileName = "Ticket_" + storeName + "_" + ticket.getId();
        String outputFile = "nivell1/src/main/resources/" + fileName + ".txt";
        try (PrintWriter pw = new PrintWriter(outputFile)) {
            //Capçalera
            pw.println("type,name,price,quantity,property");
            //Dades
            productsToCSVList.forEach(pw::println);
            //Data i hora
            pw.println(dateTimeFormatted);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateStock() {
        List<Product> stock = getOrderedProductList();
        showStock();

        if (!stock.isEmpty()) {

            System.out.println("Escolliu quin producte voleu actualitzar:");

            int productNum = scanner.nextInt() - 1;
            scanner.nextLine();

            if (productNum <= stock.size()) {

                //Obtenim el producte
                Product product = stock.get(productNum);

                //Mostrem el producte per pantalla per facilitar-ne la visió
                List<Product> showProduct = new ArrayList<>();
                showProduct.add(product);
                showTicket(showProduct);

                System.out.println("Escolliu la propietat del producte que voleu canviar:");
                System.out.println("""
                        1 - Nom
                        2 - Preu
                        3 - Quantitat
                        4 - Material/Color/Alçada
                        """);

                //Obtenim propietat a canviar
                int propertyToChange = scanner.nextInt();
                scanner.nextLine();

                System.out.println("Introduïu el nou valor:");

                //Obtenim nou valor de la propietat
                String newValue = scanner.nextLine();

                //Fem el canvi
                switch (propertyToChange) {
                    case 1 -> product.setName(newValue);
                    case 2 -> product.setPrice(Float.parseFloat(newValue));
                    case 3 -> product.setQuantity(Integer.parseInt(newValue));
                    case 4 -> {
                        switch (product.getClass().getSimpleName()) {
                            case "Decoration" -> ((Decoration) product).setMaterial(newValue);
                            case "Flower" -> ((Flower) product).setColor(newValue);
                            case "Tree" -> ((Tree) product).setHeight(Double.parseDouble(newValue));
                        }
                    }
                    default -> System.out.println("El valor seleccionat no és vàlid");
                }

                //Actualitzem valors
                stock.set(productNum, product);
                System.out.println("Producte actualitzat");

            } else {
                System.out.println("El producte seleccionat no existeix");
            }

            //Desem els canvis
            writeProductsToFile(stock, storeName);
        }
    }

    private void addToHistory(List<Product> products) {
        //Mirar si ja existeix l'historial; si no, crear-lo
        String fileName = "nivell1/src/main/resources/" + storeName + "_History" + ".txt";
        File history = new File(fileName);
        boolean exists = history.exists();

        //Convertir a llista de strings preparats pel csv i obtenir data i hora d'escriptura
        List<String> productsToCSVList = convertToCSVList(products);
        LocalDateTime dateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String dateTimeFormatted = dateTime.format(formatter);

        //Escriure a l'arxiu
        try (PrintWriter pw = new PrintWriter(new FileWriter(fileName, true))) {
            if (!exists) {
                //Si és la primera vegada que hi escrivim, posem la capçalera
                pw.println("type,name,price,quantity,property");
            }
            productsToCSVList.forEach(pw::println);
            pw.println(dateTimeFormatted);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void showHistory() {
        //Mirar si ja existeix l'historial
        String fileName = "nivell1/src/main/resources/" + storeName + "_History" + ".txt";
        File history = new File(fileName);
        boolean exists = history.exists();

        if (exists) {
            try {
                //Obtenim dades i eliminem les línies buides
                List<String> lines = Files.readAllLines(Paths.get(fileName));
                lines = lines.stream().skip(1).filter(line -> !line.isEmpty()).toList();

                Map<String, List<Product>> map = new TreeMap<>();
                List<String> temp = new ArrayList<>();

                //Agrupem en un mapa totes les vendes d'una mateixa data i hora, emprant una llista temporal
                for (String line : lines) {
                    if (!Character.isDigit(line.charAt(0))) {
                        temp.add(line);
                    } else {
                        //Convertim a List<Product> abans de desar
                        map.put(line, temp.stream().map(this::convertCSVToProduct).toList());
                        temp.clear();
                    }
                }

                System.out.println("Mostrant historial de vendes complet:");

                //Obtenim el preu total de venda d'un ticket concret (pertany a una data/hora concreta)
                for (String dateTime : map.keySet()) {
                    Float ticketTotal = map.get(dateTime).stream()
                            .map(product -> product.getQuantity() * product.getPrice())
                            .reduce(0f, Float::sum);

                    //Imprimim historial d'aquell ticket
                    System.out.println(dateTime + " - Total: " + ticketTotal + " euros");
                    System.out.format("%-10s%-10s%-12s%-10s\n", "Nom", "Preu(€)", "Quantitat", "Material");
                    System.out.format("%-10s%-10s%-12s%-10s\n", "------", "---------", "-----------", "-----------");
                    for (Product product : map.get(dateTime)) {
                        switch (product.getClass().getSimpleName()) {
                            case "Decoration" ->
                                    System.out.format("%-10s%-10s%-12s%-10s\n", product.getName(), product.getPrice(), product.getQuantity(), ((Decoration) product).getMaterial());
                            case "Flower" ->
                                    System.out.format("%-10s%-10s%-12s%-10s\n", product.getName(), product.getPrice(), product.getQuantity(), ((Flower) product).getColor());
                            case "Tree" ->
                                    System.out.format("%-10s%-10s%-12s%-10s\n", product.getName(), product.getPrice(), product.getQuantity(), ((Tree) product).getHeight());
                        }

                    }
                    System.out.println();
                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            System.out.println("Aquesta botiga encara no ha efectuat cap venda");
        }
    }

    public void showTotalSales() {
        String inputFile = "nivell1/src/main/resources/" + storeName + "_History" + ".txt";
        Float sales;

        //Calculem el valor total de les vendes a l'historial
        try {
            List<String> lines = Files.readAllLines(Paths.get(inputFile));

            //Filtrem línies buides, línies que comencin per dígit, i la capçalera. Convertim a producte i obtenim el preu
            sales = lines.stream()
                    .filter(line -> !line.isEmpty())
                    .filter(line -> !Character.isDigit(line.charAt(0)))
                    .skip(1)
                    .map(this::convertCSVToProduct)
                    .map(product -> product.getQuantity() * product.getPrice())
                    .reduce(0f, Float::sum);

            //Format amb 2 decimals
            DecimalFormat decimalFormat = new DecimalFormat("0.00");
            System.out.println("El valor total de les vendes és de: " + decimalFormat.format(sales) + " euros");

        } catch (NoSuchFileException noSuchFileException) {
            System.out.println("La botiga encara no ha efectuat cap venda");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<Product> readProductsFromStock() {
        String inputFile = "nivell1/src/main/resources/" + storeName + ".txt";
        List<Product> products = new ArrayList<>();

        //Filtrem línies buides i capçalera. Convertim a producte.
        try {
            List<String> lines = Files.readAllLines(Paths.get(inputFile));
            lines.stream()
                    .filter(line -> !line.isEmpty())
                    .skip(1)
                    .toList()
                    .forEach(line -> products.add(convertCSVToProduct(line)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return products;
    }

    private Product convertCSVToProduct(String productCSV) {
        List<String> list = convertCSVToStringList(productCSV);
        Product product = null;

        //Convertim llistat de Strings a Product, segons el tipus
        switch (list.get(0).toLowerCase()) {
            case "decoration" ->
                    product = new Decoration(list.get(1), Float.parseFloat(list.get(2)), Integer.parseInt(list.get(3)), list.get(4));
            case "flower" ->
                    product = new Flower(list.get(1), Float.parseFloat(list.get(2)), Integer.parseInt(list.get(3)), list.get(4));
            case "tree" ->
                    product = new Tree(list.get(1), Float.parseFloat(list.get(2)), Integer.parseInt(list.get(3)), Double.parseDouble(list.get(4)));
        }
        return product;
    }

    private List<String> convertToCSVList(List<Product> list) {
        //Converteix llista de productes a llista de CSVs
        return list.stream().map(this::convertProductToCSV).toList();
    }

    private String convertProductToCSV(Product product) {
        String CSV_SEPARATOR = ",";

        //Obtenim la propietat
        Object property = null;
        switch (product.getClass().getSimpleName()) {
            case "Decoration" -> property = ((Decoration) product).getMaterial();
            case "Flower" -> property = ((Flower) product).getColor();
            case "Tree" -> property = ((Tree) product).getHeight();
        }

        //Retorna serialització a CSV
        return product.getClass().getSimpleName() + CSV_SEPARATOR +
                product.getName() + CSV_SEPARATOR +
                product.getPrice() + CSV_SEPARATOR +
                product.getQuantity() + CSV_SEPARATOR +
                property;
    }

    private List<String> convertCSVToStringList(String csvString) {
        //Converteix CSV a llista de Strings
        return new ArrayList<>(Arrays.asList(csvString.split(",")));
    }

    public void writeProductsToFile(List<Product> productList, String fileName) {
        //Convertir a llista de strings preparats pel csv
        List<String> productsToCSVList = convertToCSVList(productList);

        //Escriure a l'arxiu
        String outputFile = "nivell1/src/main/resources/" + fileName + ".txt";
        try (PrintWriter pw = new PrintWriter(outputFile)) {
            pw.println("type,name,price,quantity,property");
            productsToCSVList.forEach(pw::println);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private void printTable(Map<String, List<Product>> mapList) {
        int index = 1;

        if (mapList.isEmpty()) {
            System.out.println("No hi ha productes a mostrar");
        }

        //Imprimeix només els tipus de productes que existeixen

        if (mapList.containsKey("Decoration")) {
            System.out.println("Decoration:");
            System.out.format("%-10s%-10s%-10s%-12s%-10s\n", "Numero", "Nom", "Preu(€)", "Quantitat", "Material");
            System.out.format("%-10s%-10s%-10s%-12s%-10s\n", "-------", "------", "---------", "-----------", "-----------");
            for (Product value : mapList.get("Decoration")) {
                System.out.format("%-10s%-10s%-10s%-12s%-10s\n", index, value.getName(), value.getPrice(), value.getQuantity(), ((Decoration) value).getMaterial());
                index++;
            }
            System.out.println();
        }

        if (mapList.containsKey("Flower")) {
            System.out.println("Flower:");
            System.out.format("%-10s%-10s%-10s%-12s%-10s\n", "Numero", "Nom", "Preu (€)", "Quantitat", "Color");
            System.out.format("%-10s%-10s%-10s%-12s%-10s\n", "-------", "------", "---------", "-----------", "-----------");

            for (Product value : mapList.get("Flower")) {
                System.out.format("%-10s%-10s%-10s%-12s%-10s\n", index, value.getName(), value.getPrice(), value.getQuantity(), ((Flower) value).getColor());
                index++;
            }
            System.out.println();
        }

        if (mapList.containsKey("Tree")) {
            System.out.println("Tree:");
            System.out.format("%-10s%-10s%-10s%-12s%-10s\n", "Numero", "Nom", "Preu (€)", "Quantitat", "Altura (m)");
            System.out.format("%-10s%-10s%-10s%-12s%-10s\n", "-------", "------", "---------", "-----------", "-----------");

            for (Product value : mapList.get("Tree")) {
                System.out.format("%-10s%-10s%-10s%-12s%-10s\n", index, value.getName(), value.getPrice(), value.getQuantity(), ((Tree) value).getHeight());
                index++;
            }
        }
    }
}