package org.nivell3.service;

import com.mongodb.client.*;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import org.bson.BsonNull;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.nivell3.products.Decoration;
import org.nivell3.products.Flower;
import org.nivell3.products.Product;
import org.nivell3.products.Tree;
import org.nivell3.utils.ComparadorProducte;
import org.nivell3.utils.Ticket;
import org.nivell3.utils.Tuple;

import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;

@SuppressWarnings("rawtypes")
public class StoreManager {

    //Singleton de gestió de botigues

    private static final Scanner scanner = new Scanner(System.in);
    private static StoreManager instance;
    private static MongoDatabase database;
    private static MongoCollection<Product> collection_products;
    private static MongoCollection<Ticket> collection_tickets;


    //Private constructor
    private StoreManager() {

    }

    //Lazy initialiser
    public static StoreManager getInstance(MongoDatabase database) {
        if (instance == null) {
            instance = new StoreManager();
            collection_products = database.getCollection("products", Product.class);
            collection_tickets = database.getCollection("tickets", Ticket.class);
        }
        setDatabase(database);
        return instance;
    }

    public static void setDatabase(MongoDatabase database) {
        StoreManager.database = database;
    }

    public void addProduct() {

        //Obtenim el nou producte
        Product newProduct = addProductQuestions();

        //Mirar si un producte ja existeix
        Bson query = and(eq("product", newProduct.getClass().getSimpleName().toLowerCase()),
                eq("name", newProduct.getName()),
                eq("price", newProduct.getPrice()),
                eq("property", newProduct.getProperty()));
        Product oldProduct = collection_products.find(query)
                .first();

        //Si ja existeix
        if (oldProduct != null) {

            //Suma la quantitat de productes antiga i la nova i actualitza
            collection_products.updateOne(query,
                    Updates.set("quantity", oldProduct.getQuantity() + newProduct.getQuantity()),
                    new UpdateOptions().upsert(true));
            System.out.println("Actualitzada quantitat de productes");
        } else {

            //Si no existeix:
            collection_products.insertOne(newProduct);
            System.out.println("Producte afegit");
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

        //Creem el nou objecte (amb id dummy)
        switch (productType.toLowerCase()) {
            case "decoration" ->
                    product = new Decoration(new ObjectId(), name, Float.parseFloat(price), Integer.parseInt(quantity), property);
            case "flower" ->
                    product = new Flower(new ObjectId(), name, Float.parseFloat(price), Integer.parseInt(quantity), property);
            case "tree" ->
                    product = new Tree(new ObjectId(), name, Float.parseFloat(price), Integer.parseInt(quantity), Double.parseDouble(property));
        }

        return product;
    }

    //TODO
//    public void deleteProduct() {
//        //Obtenim llistat de productes
//        showStock();
//
//        //Obtenim índex
//        System.out.println("Introduïu número del producte a eliminar:");
//        int index = scanner.nextInt();
//        scanner.nextLine();
//
//        //Eliminem el producte
//        String sqlDelete = "DELETE FROM products WHERE id_product = ?";
//        try (PreparedStatement preparedStatement = connection.prepareStatement(sqlDelete)) {
//
//            preparedStatement.setInt(1, index);
//            int i = preparedStatement.executeUpdate();
//
//            if (i == 1) {
//                System.out.println("Producte eliminat");
//            } else {
//                System.out.println("El producte seleccionat no existeix");
//            }
//        } catch (SQLException e) {
//            System.out.println("No s'ha pogut eliminar el producte");
//            e.printStackTrace();
//        }
//    }

    private List<Product> getOrderedProductList() {

        //Obtenim llista ordenada de productes, per tipus i nom (alfabètic)
        List<Product> list = readProductsFromStock();
        Comparator<Product> comparadorProducte = new ComparadorProducte().thenComparing(Product::getName);
        list.sort(comparadorProducte);
        return list;
    }

    private Map<String, List<Tuple>> getTupleMap(List<Product> productList) {

        //Primer convertim la llista de productes a llista de Tuples, creant els índexs
        List<Tuple> tupleList = getTupleList(productList);

        //Després classifiquem els productes segons la seva classe
        return tupleList.stream()
                .collect(Collectors.groupingBy(i -> i.getProduct().getClass().getSimpleName(),
                        TreeMap::new,
                        Collectors.toList()));
    }

    private List<Tuple> getTupleList(List<Product> productList) {
        //Crea una llista de Tuples amb l'índex corresponent
        return IntStream.rangeClosed(1, productList.size())
                .boxed()
                .toList()
                .stream()
                .map(index -> new Tuple(index, productList.get(index - 1)))
                .toList();
    }

    public void showStock() {
        //Obtenim llista de productes ordenada
        List<Product> list = getOrderedProductList();

        //Obtenim el mapa amb llistes de productes i índexs, classificats per tipus
        Map<String, List<Tuple>> collection = getTupleMap(list);

        //Mostrem els productes per pantalla
        printTable(collection);
    }

    //
//    public void showProducts(List<Product> products) {
//
//        //Creem un mapa amb llistes de productes classificades per tipus
//        Map<String, List<Product>> treeMapList = products.stream().sorted(Comparator.comparing(Product::getName))
//                .collect(Collectors.groupingBy(i -> i.getClass().getSimpleName(), TreeMap::new, Collectors.toList()));
//
//        //Mostrem els productes per pantalla
//        printTable(treeMapList);
//    }
//
    public void getTotalValue() {

        //Obtenim el valor total mitjançant una query
        List<Document> query = List.of(new Document("$group",
                new Document("_id",
                        new BsonNull())
                        .append("total",
                                new Document("$sum",
                                        new Document("$multiply", Arrays.asList("$price", "$quantity"))))));

        //Hem de crear un client nou sense el còdec de conversió als POJOs de product, que treballi amb Document
        MongoClient client = MongoClients.create();
        AggregateIterable<Document> aggregate = client.getDatabase("floristeria").getCollection("products").aggregate(query);
        MongoCursor<Document> iterator = aggregate.iterator();

        //Obtenim el valor del Document retornat
        double valor = 0;
        while (iterator.hasNext()) {
            Document next = iterator.next();
            valor = (double) next.get("total");
        }

        //Format amb 2 decimals
        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        System.out.println("El valor total dels productes de la botiga és: " + decimalFormat.format(valor) + " euros");
    }
//
//    //TODO
//    public void generateTicket() {
//
//        //Crea ticket amb timestamp a la BD
//        int ticketIndex = createTicket();
//
//        //Menú
//        int select;
//
//        do {
//            System.out.println("""
//                    -------------------
//                    Escolliu una opció:
//                    1: Escollir producte
//                    2: Veure tots els productes seleccionats
//                    0: Acabar amb la compra i generar tiquet
//                    ------------------
//                    """);
//
//            select = scanner.nextInt();
//            scanner.nextLine();
//
//            switch (select) {
//                //Escollir un producte
//                case 1 -> {
//                    showStock();
//
//                    System.out.println("Introduïu id del producte que desitgeu comprar:");
//                    int productIndex = scanner.nextInt();
//                    scanner.nextLine();
//
//                    System.out.println("Introduïu quantitat de productes a posar al carro:");
//                    int quantitatCompra = scanner.nextInt();
//                    scanner.nextLine();
//
//                    Product product = readProductById(productIndex);
//
//                    //Si es pot efectuar la compra
//                    if (product != null) {
//
//                        //Quant de stock hi ha disponible
//                        int quantitatStock = product.getQuantity();
//
//                        //Comprovem que la quantitat és correcta
//                        if (quantitatCompra <= quantitatStock) {
//
//                            //Preparem la query per actualitzar
//                            String sqlUpdate = "UPDATE products SET quantity = ? WHERE id_product = ?";
//
//                            //Executem l'actualització
//                            try (PreparedStatement preparedStatement = connection.prepareStatement(sqlUpdate)) {
//
//                                preparedStatement.setInt(1, quantitatStock - quantitatCompra);
//                                preparedStatement.setInt(2, productIndex);
//
//                                preparedStatement.executeUpdate();
//                            } catch (SQLException e) {
//                                e.printStackTrace();
//                            }
//
//                            //Preparem la query per inserir les dades del ticket
//                            String sqlInsert = "INSERT INTO products_tickets (product_id, ticket_id, quantity) VALUES (?, ?, ?)";
//
//                            //Executem la inserció
//                            try (PreparedStatement preparedStatement = connection.prepareStatement(sqlInsert)) {
//
//                                preparedStatement.setInt(1, productIndex);
//                                preparedStatement.setInt(2, ticketIndex);
//                                preparedStatement.setInt(3, quantitatCompra);
//
//                                int i = preparedStatement.executeUpdate();
//                                if (i == 1) {
//                                    System.out.println("Producte afegit al tiquet");
//                                }
//                            } catch (SQLException e) {
//                                e.printStackTrace();
//                            }
//                        } else {
//                            System.out.println("No podeu adquirir més articles dels que hi ha disponibles, torneu a provar");
//                        }
//                    } else {
//                        System.out.println("El producte seleccionat no existeix");
//                    }
//                }
//
////              //Veure tots els productes ja seleccionats
//                case 2 -> showTicket(ticketIndex);
//
//                //Acabar el tiquet
//                case 0 -> System.out.println("Processant la compra");
//            }
//        } while (select != 0);
//    }
//
//    //TODO
//    private int createTicket() {
//        int index = 0;
//
//        String sqlAdd = "INSERT INTO tickets (datetime) VALUES (?)";
//        try (PreparedStatement preparedStatement = connection.prepareStatement(sqlAdd, Statement.RETURN_GENERATED_KEYS)) {
//
//            preparedStatement.setTimestamp(1, new java.sql.Timestamp(new Date().getTime()));
//            preparedStatement.executeUpdate();
//
//            ResultSet resultSet = preparedStatement.getGeneratedKeys();
//            if (resultSet.next()) {
//                index = resultSet.getInt(1);
//            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//        return index;
//    }
//
//    //TODO
//    private void showTicket(int ticketIndex) {
//        List<Product> products = new ArrayList<>();
//        String sqlSelectAll = "SELECT p.*, pt.quantity FROM products_tickets pt JOIN products p ON p.id_product = pt.product_id WHERE pt.ticket_id = ?";
//
//        try (PreparedStatement preparedStatement = connection.prepareStatement(sqlSelectAll)) {
//
//            preparedStatement.setInt(1, ticketIndex);
//            ResultSet resultSet = preparedStatement.executeQuery();
//
//            while (resultSet.next()) {
//                String id = resultSet.getString("id_product");
//                String type = resultSet.getString("type");
//                String name = resultSet.getString("name");
//                float price = resultSet.getFloat("price");
//                int quantity = resultSet.getInt("pt.quantity");
//                String property = resultSet.getString("property");
//
//                products.add(buildProduct(id, type, name, price, quantity, property));
//            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//        showProducts(products);
//    }
//
//    //TODO
//    public void updateStock() {
//        List<Product> stock = getOrderedProductList();
//        showStock();
//
//        if (!stock.isEmpty()) {
//
//            System.out.println("Escolliu l'id del producte que voleu actualitzar:");
//
//            //Obtenim l'índex del producte (id)
//            int index = scanner.nextInt();
//            scanner.nextLine();
//
//            //Obtenim el producte
//            Product product = readProductById(index);
//
//            //Mostrem el producte per pantalla per facilitar-ne la visió
//            if (product != null) {
//
//                System.out.println("Producte seleccionat: ");
//                List<Product> productToShow = new ArrayList<>();
//                productToShow.add(product);
//                showProducts(productToShow);
//
//                System.out.println("Escolliu la propietat del producte que voleu canviar:");
//                System.out.println("""
//                        1 - Nom
//                        2 - Preu
//                        3 - Quantitat
//                        4 - Material/Color/Alçada
//                        """);
//
//                //Obtenim propietat a canviar
//                int propertyToChange = scanner.nextInt();
//                scanner.nextLine();
//
//                System.out.println("Introduïu el nou valor:");
//
//                //Obtenim nou valor de la propietat
//                String newValue = scanner.nextLine();
//
//                //Preparem la query
//                String sqlUpdate = "UPDATE products SET %s = ? WHERE id_product = ?";
//                switch (propertyToChange) {
//                    case 1 -> sqlUpdate = String.format(sqlUpdate, "name");
//                    case 2 -> sqlUpdate = String.format(sqlUpdate, "price");
//                    case 3 -> sqlUpdate = String.format(sqlUpdate, "quantity");
//                    case 4 -> sqlUpdate = String.format(sqlUpdate, "property");
//
//                    default -> System.out.println("El valor seleccionat no és vàlid");
//                }
//
//                //Executem l'actualització
//                try (PreparedStatement preparedStatement = connection.prepareStatement(sqlUpdate)) {
//
//                    preparedStatement.setString(1, newValue);
//                    preparedStatement.setInt(2, index);
//
//                    preparedStatement.executeUpdate();
//                    System.out.println("Producte actualitzat");
//                } catch (SQLException e) {
//                    System.out.println("El producte no s'ha pogut actualitzar correctament");
//                    e.printStackTrace();
//                }
//            } else {
//                System.out.println("El producte seleccionat no existeix");
//            }
//        }
//    }
//
//    //TODO
//    public void showHistory() {
//
//        //Inicialitzem mapa i query
//        Map<String, List<Product>> map = new TreeMap<>();
//        String sqlSelectAll = "SELECT p.*, pt.quantity, t.datetime FROM products_tickets pt JOIN products p ON p.id_product = pt.product_id JOIN tickets t ON t.id_ticket = pt.ticket_id";
//
//        //Executem la query
//        try (PreparedStatement preparedStatement = connection.prepareStatement(sqlSelectAll)) {
//
//            ResultSet resultSet = preparedStatement.executeQuery();
//
//            //Llista temporal dummy
//            List<Product> temp = new ArrayList<>();
//            while (resultSet.next()) {
//                String id = resultSet.getString("id_product");
//                String type = resultSet.getString("type");
//                String name = resultSet.getString("name");
//                float price = resultSet.getFloat("price");
//                int quantity = resultSet.getInt("pt.quantity");
//                String property = resultSet.getString("property");
//                String timestamp = resultSet.getTimestamp("t.datetime").toString();
//
//                //Desem cada compra en un mapa on la key és el timestamp; si ja n'hi ha un, hi afegim
//                if (map.containsKey(timestamp)) {
//                    temp = map.get(timestamp);
//                    temp.add(buildProduct(id, type, name, price, quantity, property));
//                    map.replace(timestamp, new ArrayList<>(temp));
//                } else {
//                    temp.add(buildProduct(id, type, name, price, quantity, property));
//                    map.put(timestamp, new ArrayList<>(temp));
//                }
//                //Esborrem la llista dummy
//                temp.clear();
//            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//
//        System.out.println("Mostrant historial de vendes complet:");
//
//        if (map.isEmpty()) {
//            System.out.println("Aquesta botiga encara no ha efectuat cap venda");
//        }
//
//        //Obtenim el preu total de venda d'un ticket concret (pertany a una data/hora concreta)
//        for (String dateTime : map.keySet()) {
//            Float ticketTotal = map.get(dateTime).stream()
//                    .map(product -> product.getQuantity() * product.getPrice())
//                    .reduce(0f, Float::sum);
//
//            //Imprimim historial d'aquell ticket
//            System.out.println(dateTime + " - Total: " + ticketTotal + " euros");
//            System.out.format("%-10s%-10s%-12s%-10s\n", "Nom", "Preu(€)", "Quantitat", "Material");
//            System.out.format("%-10s%-10s%-12s%-10s\n", "------", "---------", "-----------", "-----------");
//            for (Product product : map.get(dateTime)) {
//                switch (product.getClass().getSimpleName()) {
//                    case "Decoration" ->
//                            System.out.format("%-10s%-10s%-12s%-10s\n", product.getName(), product.getPrice(), product.getQuantity(), ((Decoration) product).getMaterial());
//                    case "Flower" ->
//                            System.out.format("%-10s%-10s%-12s%-10s\n", product.getName(), product.getPrice(), product.getQuantity(), ((Flower) product).getColor());
//                    case "Tree" ->
//                            System.out.format("%-10s%-10s%-12s%-10s\n", product.getName(), product.getPrice(), product.getQuantity(), ((Tree) product).getHeight());
//                }
//
//            }
//            System.out.println();
//        }
//    }
//
//    //TODO
//    public void showTotalSales() {
//        Float sales = 0f;
//
//        //Calculem el valor total de les vendes a l'historial
//        String sqlSelectAllSales = "SELECT SUM(pt.quantity * p.price) FROM products_tickets pt JOIN products p ON p.id_product = pt.product_id JOIN tickets t ON t.id_ticket = pt.ticket_id";
//
//        try (PreparedStatement preparedStatement = connection.prepareStatement(sqlSelectAllSales)) {
//
//            ResultSet resultSet = preparedStatement.executeQuery();
//            if (resultSet.next()) {
//                sales = resultSet.getFloat(1);
//            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//        //Format amb 2 decimals
//        DecimalFormat decimalFormat = new DecimalFormat("0.00");
//        System.out.println("El valor total de les vendes és de: " + decimalFormat.format(sales) + " euros");
//
//    }
//
    public List<Product> readProductsFromStock() {

        FindIterable<Product> productsMongo = collection_products.find();
        List<Product> products = new ArrayList<>();
        productsMongo.forEach(products::add);

        return products;
    }

    //
//    //TODO
//    public Product readProductById(int id_product) {
//
//        Product product = null;
//        String sqlSelectAll = "SELECT * FROM products WHERE id_product = ?";
//
//        try (PreparedStatement preparedStatement = connection.prepareStatement(sqlSelectAll)) {
//
//            preparedStatement.setInt(1, id_product);
//            ResultSet resultSet = preparedStatement.executeQuery();
//
//            while (resultSet.next()) {
//                String id = resultSet.getString("id_product");
//                String type = resultSet.getString("type");
//                String name = resultSet.getString("name");
//                float price = resultSet.getFloat("price");
//                int quantity = resultSet.getInt("quantity");
//                String property = resultSet.getString("property");
//
//                product = buildProduct(id, type, name, price, quantity, property);
//            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//
//        return product;
//    }
//
//    //TODO (eliminar?)
//    private Product buildProduct(String id, String type, String name, float price, int quantity, String property) {
//        Product product = null;
//
//        //Convertim a Product, segons el tipus
//        switch (type.toLowerCase()) {
//            case "decoration" -> product = new Decoration(id, name, price, quantity, property);
//            case "flower" -> product = new Flower(id, name, price, quantity, property);
//            case "tree" -> product = new Tree(id, name, price, quantity, Double.parseDouble(property));
//        }
//        return product;
//    }
//
//    //TODO
//    private Object getProperty(Product product) {
//
//        //Obtenim la propietat concreta depenent del tipus
//        Object property = null;
//        switch (product.getClass().getSimpleName()) {
//            case "Decoration" -> property = ((Decoration) product).getMaterial();
//            case "Flower" -> property = ((Flower) product).getColor();
//            case "Tree" -> property = ((Tree) product).getHeight();
//        }
//        return property;
//    }
//
    private void printTable(Map<String, List<Tuple>> mapList) {

        if (mapList.isEmpty()) {
            System.out.println("No hi ha productes a mostrar");
        }

        //Imprimeix només els tipus de productes que existeixen

        if (mapList.containsKey("Decoration")) {
            System.out.println("Decoration:");
            System.out.format("%-14s%-10s%-10s%-12s%-10s\n", "Id Producte", "Nom", "Preu (€)", "Quantitat", "Material");
            System.out.format("%-14s%-10s%-10s%-12s%-10s\n", "---------", "------", "---------", "-----------", "-----------");
            for (Tuple value : mapList.get("Decoration")) {
                System.out.format("%-14s%-10s%-10s%-12s%-10s\n", value.getIndex(),
                        value.getProduct().getName(),
                        value.getProduct().getPrice(),
                        value.getProduct().getQuantity(),
                        value.getProduct().getProperty());
            }
            System.out.println();
        }

        if (mapList.containsKey("Flower")) {
            System.out.println("Flower:");
            System.out.format("%-14s%-10s%-10s%-12s%-10s\n", "Id Producte", "Nom", "Preu (€)", "Quantitat", "Material");
            System.out.format("%-14s%-10s%-10s%-12s%-10s\n", "---------", "------", "---------", "-----------", "-----------");

            for (Tuple value : mapList.get("Flower")) {
                System.out.format("%-14s%-10s%-10s%-12s%-10s\n", value.getIndex(),
                        value.getProduct().getName(),
                        value.getProduct().getPrice(),
                        value.getProduct().getQuantity(),
                        value.getProduct().getProperty());
            }
            System.out.println();
        }

        if (mapList.containsKey("Tree")) {
            System.out.println("Tree:");
            System.out.format("%-14s%-10s%-10s%-12s%-10s\n", "Id Producte", "Nom", "Preu (€)", "Quantitat", "Material");
            System.out.format("%-14s%-10s%-10s%-12s%-10s\n", "---------", "------", "---------", "-----------", "-----------");

            for (Tuple value : mapList.get("Tree")) {
                System.out.format("%-14s%-10s%-10s%-12s%-10s\n", value.getIndex(),
                        value.getProduct().getName(),
                        value.getProduct().getPrice(),
                        value.getProduct().getQuantity(),
                        value.getProduct().getProperty());
            }
        }
    }
}
