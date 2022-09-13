package org.nivell2.service;

import org.nivell2.products.Decoration;
import org.nivell2.products.Flower;
import org.nivell2.products.Product;
import org.nivell2.products.Tree;
import org.nivell2.utils.ComparadorProducte;

import java.sql.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.Date;
import java.util.stream.Collectors;

public class StoreManager {

    //Singleton de gestió de botigues

    private static final Scanner scanner = new Scanner(System.in);

    private static StoreManager instance;
    private static Connection connection;


    //Private constructor
    private StoreManager() {

    }

    //Lazy initialiser
    public static StoreManager getInstance(Connection connection) {
        if (instance == null) {
            instance = new StoreManager();
        }
        instance.setConnection(connection);
        return instance;
    }

    public void setConnection(Connection connection) {
        StoreManager.connection = connection;
    }

    public void addProduct() {

        //Obtenim el nou producte
        Product newProduct = addProductQuestions();

        //Mirar si un producte ja existeix
        boolean exists = false;
        int index = 0;
        int oldQuantity = 0;

        String sqlFind = "SELECT id_product, quantity FROM products WHERE type = ? AND name = ? AND price = ? AND property = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sqlFind)) {

            preparedStatement.setString(1, newProduct.getClass().getSimpleName());
            preparedStatement.setString(2, newProduct.getName());
            preparedStatement.setFloat(3, newProduct.getPrice());
            preparedStatement.setString(4, getProperty(newProduct).toString());

            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                exists = true;

                //Índex del producte i quantitat
                index = resultSet.getInt("id_product");
                oldQuantity = resultSet.getInt("quantity");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        //Si ja existeix
        if (exists) {

            //Suma la quantitat de productes antiga i la nova i actualitza
            String sqlUpdate = "UPDATE products SET quantity = ? WHERE id_product = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sqlUpdate)) {

                preparedStatement.setInt(1, oldQuantity + newProduct.getQuantity());
                preparedStatement.setInt(2, index);

                preparedStatement.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            System.out.println("Actualitzada quantitat de productes");

        } else {
            //Si no existeix:

            String sqlAdd = "INSERT INTO products (type, name, price, quantity, property) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sqlAdd)) {

                preparedStatement.setString(1, newProduct.getClass().getSimpleName());
                preparedStatement.setString(2, newProduct.getName());
                preparedStatement.setFloat(3, newProduct.getPrice());
                preparedStatement.setInt(4, newProduct.getQuantity());
                preparedStatement.setString(5, getProperty(newProduct).toString());

                int i = preparedStatement.executeUpdate();

                if (i == 1) {
                    System.out.println("Producte afegit");
                }
            } catch (SQLException e) {
                System.out.println("No s'ha pogut afegir el producte");
                e.printStackTrace();
            }
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

        //Creem el nou objecte (amb id dummy = 0)
        switch (productType.toLowerCase()) {
            case "decoration" ->
                    product = new Decoration(0, name, Float.parseFloat(price), Integer.parseInt(quantity), property);
            case "flower" ->
                    product = new Flower(0, name, Float.parseFloat(price), Integer.parseInt(quantity), property);
            case "tree" ->
                    product = new Tree(0, name, Float.parseFloat(price), Integer.parseInt(quantity), Double.parseDouble(property));
        }

        return product;
    }

    public void deleteProduct() {
        //Obtenim llistat de productes
        showStock();

        //Obtenim índex
        System.out.println("Introduïu número del producte a eliminar:");
        int index = scanner.nextInt();
        scanner.nextLine();

        //Eliminem el producte
        String sqlDelete = "DELETE FROM products WHERE id_product = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sqlDelete)) {

            preparedStatement.setInt(1, index);
            int i = preparedStatement.executeUpdate();

            if (i == 1) {
                System.out.println("Producte eliminat");
            } else {
                System.out.println("El producte seleccionat no existeix");
            }
        } catch (SQLException e) {
            System.out.println("No s'ha pogut eliminar el producte");
            e.printStackTrace();
        }
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
        Map<String, List<Product>> treeMapList = list.stream()
                .sorted(Comparator.comparing(Product::getName))
                .collect(Collectors.groupingBy(i -> i.getClass().getSimpleName(),
                        TreeMap::new,
                        Collectors.toList()));

        //Mostrem els productes per pantalla
        printTable(treeMapList);
    }

    public void showProducts(List<Product> products) {

        //Creem un mapa amb llistes de productes classificades per tipus
        Map<String, List<Product>> treeMapList = products.stream().sorted(Comparator.comparing(Product::getName))
                .collect(Collectors.groupingBy(i -> i.getClass().getSimpleName(), TreeMap::new, Collectors.toList()));

        //Mostrem els productes per pantalla
        printTable(treeMapList);
    }

    public void getTotalValue() {

        //Obtenim el valor total mitjançant una query SQL
        float valor = 0;
        String sqlTotal = "SELECT SUM(price * quantity) FROM products";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sqlTotal)) {

            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                valor = resultSet.getFloat(1);
            }
        } catch (SQLException e) {
            System.out.println("No s'ha pogut obtenir el valor total dels productes");
            e.printStackTrace();
        }

        //Format amb 2 decimals
        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        System.out.println("El valor total dels productes de la botiga és: " + decimalFormat.format(valor) + " euros");
    }

    public void generateTicket() {

        //Crea ticket amb timestamp a la BD
        int ticketIndex = createTicket();

        //Menú
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

                    System.out.println("Introduïu id del producte que desitgeu comprar:");
                    int productIndex = scanner.nextInt();
                    scanner.nextLine();

                    System.out.println("Introduïu quantitat de productes a posar al carro:");
                    int quantitatCompra = scanner.nextInt();
                    scanner.nextLine();

                    Product product = readProductById(productIndex);

                    //Si es pot efectuar la compra
                    if (product != null) {

                        //Quant de stock hi ha disponible
                        int quantitatStock = product.getQuantity();

                        //Comprovem que la quantitat és correcta
                        if (quantitatCompra <= quantitatStock) {

                            //Preparem la query per actualitzar
                            String sqlUpdate = "UPDATE products SET quantity = ? WHERE id_product = ?";

                            //Executem l'actualització
                            try (PreparedStatement preparedStatement = connection.prepareStatement(sqlUpdate)) {

                                preparedStatement.setInt(1, quantitatStock - quantitatCompra);
                                preparedStatement.setInt(2, productIndex);

                                preparedStatement.executeUpdate();
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }

                            //Preparem la query per inserir les dades del ticket
                            String sqlInsert = "INSERT INTO products_tickets (product_id, ticket_id, quantity) VALUES (?, ?, ?)";

                            //Executem la inserció
                            try (PreparedStatement preparedStatement = connection.prepareStatement(sqlInsert)) {

                                preparedStatement.setInt(1, productIndex);
                                preparedStatement.setInt(2, ticketIndex);
                                preparedStatement.setInt(3, quantitatCompra);

                                int i = preparedStatement.executeUpdate();
                                if (i == 1) {
                                    System.out.println("Producte afegit al tiquet");
                                }
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                        } else {
                            System.out.println("No podeu adquirir més articles dels que hi ha disponibles, torneu a provar");
                        }
                    } else {
                        System.out.println("El producte seleccionat no existeix");
                    }
                }

//              //Veure tots els productes ja seleccionats
                case 2 -> showTicket(ticketIndex);

                //Acabar el tiquet
                case 0 -> System.out.println("Processant la compra");
            }
        } while (select != 0);
    }

    private int createTicket() {
        int index = 0;

        String sqlAdd = "INSERT INTO tickets (datetime) VALUES (?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sqlAdd, Statement.RETURN_GENERATED_KEYS)) {

            preparedStatement.setTimestamp(1, new java.sql.Timestamp(new Date().getTime()));
            preparedStatement.executeUpdate();

            ResultSet resultSet = preparedStatement.getGeneratedKeys();
            if (resultSet.next()) {
                index = resultSet.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return index;
    }

    private void showTicket(int ticketIndex) {
        List<Product> products = new ArrayList<>();
        String sqlSelectAll = "SELECT p.*, pt.quantity FROM products_tickets pt JOIN products p ON p.id_product = pt.product_id WHERE pt.ticket_id = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sqlSelectAll)) {

            preparedStatement.setInt(1, ticketIndex);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                int id = resultSet.getInt("id_product");
                String type = resultSet.getString("type");
                String name = resultSet.getString("name");
                float price = resultSet.getFloat("price");
                int quantity = resultSet.getInt("pt.quantity");
                String property = resultSet.getString("property");

                products.add(buildProduct(id, type, name, price, quantity, property));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        showProducts(products);
    }

    public void updateStock() {
        List<Product> stock = getOrderedProductList();
        showStock();

        if (!stock.isEmpty()) {

            System.out.println("Escolliu l'id del producte que voleu actualitzar:");

            //Obtenim l'índex del producte (id)
            int index = scanner.nextInt();
            scanner.nextLine();

            //Obtenim el producte
            Product product = readProductById(index);

            //Mostrem el producte per pantalla per facilitar-ne la visió
            if (product != null) {

                System.out.println("Producte seleccionat: ");
                List<Product> productToShow = new ArrayList<>();
                productToShow.add(product);
                showProducts(productToShow);

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

                //Preparem la query
                String sqlUpdate = "UPDATE products SET %s = ? WHERE id_product = ?";
                switch (propertyToChange) {
                    case 1 -> sqlUpdate = String.format(sqlUpdate, "name");
                    case 2 -> sqlUpdate = String.format(sqlUpdate, "price");
                    case 3 -> sqlUpdate = String.format(sqlUpdate, "quantity");
                    case 4 -> sqlUpdate = String.format(sqlUpdate, "property");

                    default -> System.out.println("El valor seleccionat no és vàlid");
                }

                //Executem l'actualització
                try (PreparedStatement preparedStatement = connection.prepareStatement(sqlUpdate)) {

                    preparedStatement.setString(1, newValue);
                    preparedStatement.setInt(2, index);

                    preparedStatement.executeUpdate();
                    System.out.println("Producte actualitzat");
                } catch (SQLException e) {
                    System.out.println("El producte no s'ha pogut actualitzar correctament");
                    e.printStackTrace();
                }
            } else {
                System.out.println("El producte seleccionat no existeix");
            }
        }
    }

    public void showHistory() {

        //Inicialitzem mapa i query
        Map<String, List<Product>> map = new TreeMap<>();
        String sqlSelectAll = "SELECT p.*, pt.quantity, t.datetime FROM products_tickets pt JOIN products p ON p.id_product = pt.product_id JOIN tickets t ON t.id_ticket = pt.ticket_id";

        //Executem la query
        try (PreparedStatement preparedStatement = connection.prepareStatement(sqlSelectAll)) {

            ResultSet resultSet = preparedStatement.executeQuery();

            //Llista temporal dummy
            List<Product> temp = new ArrayList<>();
            while (resultSet.next()) {
                int id = resultSet.getInt("id_product");
                String type = resultSet.getString("type");
                String name = resultSet.getString("name");
                float price = resultSet.getFloat("price");
                int quantity = resultSet.getInt("pt.quantity");
                String property = resultSet.getString("property");
                String timestamp = resultSet.getTimestamp("t.datetime").toString();

                //Desem cada compra en un mapa on la key és el timestamp; si ja n'hi ha un, hi afegim
                if (map.containsKey(timestamp)) {
                    temp = map.get(timestamp);
                    temp.add(buildProduct(id, type, name, price, quantity, property));
                    map.replace(timestamp, new ArrayList<>(temp));
                } else {
                    temp.add(buildProduct(id, type, name, price, quantity, property));
                    map.put(timestamp, new ArrayList<>(temp));
                }
                //Esborrem la llista dummy
                temp.clear();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        System.out.println("Mostrant historial de vendes complet:");

        if (map.isEmpty()) {
            System.out.println("Aquesta botiga encara no ha efectuat cap venda");
        }

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
    }

    public void showTotalSales() {
        Float sales = 0f;

        //Calculem el valor total de les vendes a l'historial
        String sqlSelectAllSales = "SELECT SUM(pt.quantity * p.price) FROM products_tickets pt JOIN products p ON p.id_product = pt.product_id JOIN tickets t ON t.id_ticket = pt.ticket_id";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sqlSelectAllSales)) {

            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                sales = resultSet.getFloat(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        //Format amb 2 decimals
        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        System.out.println("El valor total de les vendes és de: " + decimalFormat.format(sales) + " euros");

    }

    public List<Product> readProductsFromStock() {

        List<Product> products = new ArrayList<>();
        String sqlSelectAll = "SELECT * FROM products";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sqlSelectAll);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                int id = resultSet.getInt("id_product");
                String type = resultSet.getString("type");
                String name = resultSet.getString("name");
                float price = resultSet.getFloat("price");
                int quantity = resultSet.getInt("quantity");
                String property = resultSet.getString("property");

                products.add(buildProduct(id, type, name, price, quantity, property));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return products;
    }

    public Product readProductById(int id_product) {

        Product product = null;
        String sqlSelectAll = "SELECT * FROM products WHERE id_product = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sqlSelectAll)) {

            preparedStatement.setInt(1, id_product);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                int id = resultSet.getInt("id_product");
                String type = resultSet.getString("type");
                String name = resultSet.getString("name");
                float price = resultSet.getFloat("price");
                int quantity = resultSet.getInt("quantity");
                String property = resultSet.getString("property");

                product = buildProduct(id, type, name, price, quantity, property);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return product;
    }

    private Product buildProduct(int id, String type, String name, float price, int quantity, String property) {
        Product product = null;

        //Convertim a Product, segons el tipus
        switch (type.toLowerCase()) {
            case "decoration" -> product = new Decoration(id, name, price, quantity, property);
            case "flower" -> product = new Flower(id, name, price, quantity, property);
            case "tree" -> product = new Tree(id, name, price, quantity, Double.parseDouble(property));
        }
        return product;
    }

    private Object getProperty(Product product) {

        //Obtenim la propietat concreta depenent del tipus
        Object property = null;
        switch (product.getClass().getSimpleName()) {
            case "Decoration" -> property = ((Decoration) product).getMaterial();
            case "Flower" -> property = ((Flower) product).getColor();
            case "Tree" -> property = ((Tree) product).getHeight();
        }
        return property;
    }

    private void printTable(Map<String, List<Product>> mapList) {

        if (mapList.isEmpty()) {
            System.out.println("No hi ha productes a mostrar");
        }

        //Imprimeix només els tipus de productes que existeixen

        if (mapList.containsKey("Decoration")) {
            System.out.println("Decoration:");
            System.out.format("%-14s%-10s%-10s%-12s%-10s\n", "Id Producte", "Nom", "Preu(€)", "Quantitat", "Material");
            System.out.format("%-14s%-10s%-10s%-12s%-10s\n", "---------", "------", "---------", "-----------", "-----------");
            for (Product value : mapList.get("Decoration")) {
                System.out.format("%-14s%-10s%-10s%-12s%-10s\n", value.getId(), value.getName(), value.getPrice(), value.getQuantity(), ((Decoration) value).getMaterial());
            }
            System.out.println();
        }

        if (mapList.containsKey("Flower")) {
            System.out.println("Flower:");
            System.out.format("%-14s%-10s%-10s%-12s%-10s\n", "Id Producte", "Nom", "Preu(€)", "Quantitat", "Material");
            System.out.format("%-14s%-10s%-10s%-12s%-10s\n", "---------", "------", "---------", "-----------", "-----------");

            for (Product value : mapList.get("Flower")) {
                System.out.format("%-14s%-10s%-10s%-12s%-10s\n", value.getId(), value.getName(), value.getPrice(), value.getQuantity(), ((Flower) value).getColor());
            }
            System.out.println();
        }

        if (mapList.containsKey("Tree")) {
            System.out.println("Tree:");
            System.out.format("%-14s%-10s%-10s%-12s%-10s\n", "Id Producte", "Nom", "Preu(€)", "Quantitat", "Material");
            System.out.format("%-14s%-10s%-10s%-12s%-10s\n", "---------", "------", "---------", "-----------", "-----------");

            for (Product value : mapList.get("Tree")) {
                System.out.format("%-14s%-10s%-10s%-12s%-10s\n", value.getId(), value.getName(), value.getPrice(), value.getQuantity(), ((Tree) value).getHeight());
            }
        }
    }
}