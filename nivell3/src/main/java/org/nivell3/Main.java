package org.nivell3;

import com.mongodb.MongoClientSettings;
import com.mongodb.client.*;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.types.ObjectId;
import org.nivell3.products.Decoration;
import org.nivell3.products.Flower;
import org.nivell3.products.Product;
import org.nivell3.products.Tree;
import org.nivell3.service.DBConnection;

import java.sql.Connection;
import java.util.Objects;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;
import static org.bson.codecs.pojo.Conventions.DEFAULT_CONVENTIONS;


public class Main {

    static {
        Logger.getLogger("org.mongodb.driver").setLevel(Level.OFF);
        Logger.getLogger("org.bson").setLevel(Level.OFF);
    }
    private static final String currentStore = "floristeria";
    private static final MongoDatabase database = DBConnection.getConnection(currentStore);
    private static final Scanner scanner = new Scanner(System.in);

    protected static Class<?>[] getSubClasses() {
        return new Class<?>[]{Decoration.class, Flower.class, Tree.class};
    }

    public static void main(String[] args) {

        MongoCollection<Document> products = database.getCollection("products");

        products.deleteMany(new Document());

        MongoCollection<Product> collection = database.getCollection("products", Product.class);
        collection.insertOne(new Decoration(new ObjectId(), "nom", 1f, 1, "fusta"));
        collection.insertOne(new Flower(new ObjectId(), "nom", 1f, 1, "blanc"));
        collection.insertOne(new Tree(new ObjectId(), "nom", 1f, 1, 1d));

        FindIterable<Product> productsFound = collection.find();
        productsFound.forEach(product -> System.out.println(product.getProperty()));


//        //Menú
//        int select;
//
//        do {
//            printMenu();
//
//            select = scanner.nextInt();
//            scanner.nextLine();
//
//            switch (select) {
//
//                //Introduir producte a botiga
//                case 1 -> StoreManager.getInstance(connection).addProduct();
//
//                //Actualitzar producte a botiga
//                case 2 -> StoreManager.getInstance(connection).updateStock();
//
//                //Esborrar producte de botiga
//                case 3 -> StoreManager.getInstance(connection).deleteProduct();
//
//                //Mostrar stock amb quantitats
//                case 4 -> StoreManager.getInstance(connection).showStock();
//
//                //Mostrar valor total
//                case 5 -> StoreManager.getInstance(connection).getTotalValue();
//
//                //Crear ticket
//                case 6 -> StoreManager.getInstance(connection).generateTicket();
//
//                //Mostrar vendes (historial)
//                case 7 -> StoreManager.getInstance(connection).showHistory();
//
//                //Mostrat total diners vendes
//                case 8 -> StoreManager.getInstance(connection).showTotalSales();
//
//                //Sortir del programa
//                case 0 -> DBConnection.closeConnection();
//            }
//
//        } while (select != 0);
//
//        System.out.println("Sortint del programa");
    }

    public static void printMenu() {
        System.out.println("""
                -------------------
                Escolliu una opció:
                1: Afegir un producte a una botiga
                2: Actualitzar un producte
                3: Esborrar un producte
                4: Mostrar stock total de la botiga
                5: Mostrar valor total del stock de la botiga
                6: Crear un nou tiquet de compra
                7: Mostrar historial de vendes
                8: Mostrar total guanyat amb les vendes
                0: Sortir
                ------------------
                """);
    }
}