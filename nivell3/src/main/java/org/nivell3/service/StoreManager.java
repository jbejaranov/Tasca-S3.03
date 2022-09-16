package org.nivell3.service;

import com.google.gson.Gson;
import com.mongodb.client.*;
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
import java.time.LocalDateTime;
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
        return instance;
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

    public void deleteProduct() {

        //Obtenim llistat de productes
        showStock();
        List<Tuple> tupleList = getTupleList(getOrderedProductList());

        //Obtenim índex
        System.out.println("Introduïu número del producte a eliminar:");
        int deleteIndex = scanner.nextInt();
        scanner.nextLine();

        //Eliminem el producte
        Optional<Tuple> optionalTuple = getTupleByTupleId(tupleList, deleteIndex);

        if (optionalTuple.isPresent()) {
            //Si existeix
            collection_products.deleteOne(eq("_id", tupleList.get(deleteIndex - 1).getProduct().getId()));
            System.out.println("Producte eliminat");
        } else {
            //Si no existeix:
            System.out.println("El producte seleccionat no existeix");
        }
    }

    private Optional<Tuple> getTupleByTupleId(List<Tuple> tupleList, int deleteIndex) {
        return tupleList.stream().filter(tuple -> tuple.getIndex() == deleteIndex).findFirst();
    }


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

    private void showProducts(List<Product> products) {

        //Creem un mapa amb llistes de productes classificades per tipus
        Map<String, List<Tuple>> collection = getTupleMap(products);

        //Mostrem els productes per pantalla
        printTable(collection);
    }

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

    public void generateTicket() {

        //Crea ticket amb timestamp
        Ticket ticket = new Ticket(new ObjectId(), LocalDateTime.now());

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

                    //Obtenim els productes
                    List<Tuple> productTuples = getTupleList(getOrderedProductList());

                    //Si es pot efectuar la compra
                    if (productIndex <= productTuples.size()) {

                        //Obtenim el producte concret
                        Product product = productTuples.get(productIndex - 1).getProduct();

                        //Quant de stock hi ha disponible
                        int quantitatStock = product.getQuantity();

                        //Comprovem que la quantitat és correcta
                        if (quantitatCompra <= quantitatStock) {

                            //Preparem la query per actualitzar el valor de la quantitat a la BD
                            collection_products.updateOne(eq(product.getId()),
                                    Updates.set("quantity", quantitatStock - quantitatCompra));

                            //Preparem la query per inserir les dades del ticket
                            addProductToTicket(ticket, product, quantitatCompra);

                        } else {
                            System.out.println("No podeu adquirir més articles dels que hi ha disponibles, torneu a provar");
                        }
                    } else {
                        System.out.println("El producte seleccionat no existeix");
                    }
                }

                //Veure tots els productes ja seleccionats
                case 2 -> showTicket(ticket);

                //Acabar el tiquet
                case 0 -> System.out.println("Processant la compra");
            }
        } while (select != 0);

        collection_tickets.insertOne(ticket);
    }

    private void addProductToTicket(Ticket ticket, Product product, int quantitatCompra) {
        Product newProduct = buildProduct(product.getId(),
                product.getClass().getSimpleName(),
                product.getName(),
                product.getPrice(),
                quantitatCompra,
                product.getProperty().toString());

        switch (product.getClass().getSimpleName().toLowerCase()) {
            case "decoration" -> ticket.addDecoration((Decoration) newProduct);
            case "flower" -> ticket.addFlower((Flower) newProduct);
            case "tree" -> ticket.addTree((Tree) newProduct);
        }
    }

    private void showTicket(Ticket ticket) {
        List<Product> list = new ArrayList<>();
        list.addAll(ticket.getDecorationList());
        list.addAll(ticket.getFlowerList());
        list.addAll(ticket.getTreeList());

        showProducts(list);
    }

    public void updateStock() {
        List<Tuple> stock = getTupleList(getOrderedProductList());
        showStock();

        if (!stock.isEmpty()) {

            System.out.println("Escolliu l'id del producte que voleu actualitzar:");

            //Obtenim l'índex del producte (id)
            int index = scanner.nextInt();
            scanner.nextLine();

            //Obtenim el producte
            Tuple tuple = getTupleByTupleId(stock, index).orElse(null);

            //Mostrem el producte per pantalla per facilitar-ne la visió
            if (tuple != null) {

                Product product = tuple.getProduct();

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
                Bson update = null;

                switch (propertyToChange) {
                    case 1 -> update = Updates.set("name", newValue);
                    case 2 -> update = Updates.set("price", Float.parseFloat(newValue));
                    case 3 -> update = Updates.set("quantity", Integer.parseInt(newValue));
                    case 4 -> {
                        if ("tree".equalsIgnoreCase(product.getClass().getSimpleName())) {
                            update = Updates.set("property", Double.parseDouble(newValue));
                        } else {
                            update = Updates.set("property", newValue);
                        }
                    }

                    default -> System.out.println("El valor seleccionat no és vàlid");
                }

                //Executem l'actualització
                if (update != null) {
                    collection_products.updateOne(eq(product.getId()), update);
                }
                System.out.println("Producte actualitzat");

            } else {
                System.out.println("El producte seleccionat no existeix");
            }
        } else {
            System.out.println("No hi ha productes a mostrar");
        }
    }

    public void showHistory() {

        //Hem de crear un client nou sense el còdec de conversió als POJOs de product,
        //que treballi amb Document, i obtenir els Documents de tickets
        MongoCollection<Document> ticketCollection = MongoClients.create().getDatabase("floristeria").getCollection("tickets");
        FindIterable<Document> tickets = ticketCollection.find();

        //Instanciem un nou Gson per deserialitzar els Documents
        Gson gson = new Gson();
        List<Ticket> ticketList = new ArrayList<>();
        tickets.forEach(ticket -> ticketList.add(gson.fromJson(ticket.toJson(), Ticket.class)));

        System.out.println("Mostrant historial de vendes complet:");

        if (ticketList.isEmpty()) {
            System.out.println("Aquesta botiga encara no ha efectuat cap venda");
        }

        //Obtenim el preu total de venda d'un ticket concret
        for (Ticket ticket : ticketList) {
            float ticketTotal = ticket.getDecorationList().stream()
                    .map(product -> product.getQuantity() * product.getPrice())
                    .reduce(0f, Float::sum) +
                    ticket.getFlowerList().stream()
                            .map(product -> product.getQuantity() * product.getPrice())
                            .reduce(0f, Float::sum) +
                    ticket.getTreeList().stream()
                            .map(product -> product.getQuantity() * product.getPrice())
                            .reduce(0f, Float::sum);

            //Imprimim historial d'aquell ticket
            System.out.println(ticket.getDateTime() + " - Total: " + ticketTotal + " euros");
            System.out.format("%-10s%-10s%-12s%-10s\n", "Nom", "Preu(€)", "Quantitat", "Material");
            System.out.format("%-10s%-10s%-12s%-10s\n", "------", "---------", "-----------", "-----------");
            for (Decoration product : ticket.getDecorationList()) {
                System.out.format("%-10s%-10s%-12s%-10s\n", product.getName(), product.getPrice(), product.getQuantity(), product.getProperty());
            }
            for (Flower product : ticket.getFlowerList()) {
                System.out.format("%-10s%-10s%-12s%-10s\n", product.getName(), product.getPrice(), product.getQuantity(), product.getProperty());
            }
            for (Tree product : ticket.getTreeList()) {
                System.out.format("%-10s%-10s%-12s%-10s\n", product.getName(), product.getPrice(), product.getQuantity(), product.getProperty());
            }
            System.out.println();
        }
    }

    public void showTotalSales() {

        //Calculem el valor total de les vendes a l'historial amb una query
        List<Document> query = Arrays.asList(new Document("$project",
                        new Document("all_products",
                                new Document("$concatArrays", Arrays.asList("$decorationList", "$flowerList", "$treeList")))),
                new Document("$unwind",
                        new Document("path", "$all_products")),
                new Document("$group",
                        new Document("_id",
                                new BsonNull())
                                .append("total",
                                        new Document("$sum",
                                                new Document("$multiply", Arrays.asList("$all_products.price", "$all_products.quantity"))))));

        //Hem de crear un client nou sense el còdec de conversió als POJOs de product, que treballi amb Document
        MongoClient client = MongoClients.create();
        AggregateIterable<Document> aggregate = client.getDatabase("floristeria").getCollection("tickets").aggregate(query);
        MongoCursor<Document> iterator = aggregate.iterator();

        //Obtenim el valor del Document retornat
        double valor = 0;
        while (iterator.hasNext()) {
            Document next = iterator.next();
            valor = (double) next.get("total");
        }

        //Format amb 2 decimals
        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        System.out.println("El valor total de les vendes és de: " + decimalFormat.format(valor) + " euros");
    }

    public List<Product> readProductsFromStock() {

        FindIterable<Product> productsMongo = collection_products.find();
        List<Product> products = new ArrayList<>();
        productsMongo.forEach(products::add);

        return products;
    }

    private Product buildProduct(ObjectId id, String type, String name, float price, int quantity, String property) {
        Product product = null;

        //Convertim a Product, segons el tipus
        switch (type.toLowerCase()) {
            case "decoration" -> product = new Decoration(id, name, price, quantity, property);
            case "flower" -> product = new Flower(id, name, price, quantity, property);
            case "tree" -> product = new Tree(id, name, price, quantity, Double.parseDouble(property));
        }
        return product;
    }

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