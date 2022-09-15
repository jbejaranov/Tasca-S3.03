package org.nivell3.service;

import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.nivell3.products.Decoration;
import org.nivell3.products.Flower;
import org.nivell3.products.Tree;
import org.nivell3.utils.Ticket;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;
import static org.bson.codecs.pojo.Conventions.DEFAULT_CONVENTIONS;

//Utility class: classe final, mètodes estàtics, constructor privat
public final class DBConnection {

    private static MongoClient client;

    private DBConnection() {
        CodecRegistry pojoCodecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),
                fromProviders(PojoCodecProvider.builder()
                        .conventions(DEFAULT_CONVENTIONS)
                        .register(getSubClasses())
                        .automatic(true)
                        .build()));
        MongoClientSettings settings = MongoClientSettings.builder()
                .codecRegistry(pojoCodecRegistry)
                .build();

        client = MongoClients.create(settings);
    }

    public static MongoDatabase getConnection(String DBName) {
        if (client == null) {
            new DBConnection();
        }
        return client.getDatabase(DBName);
    }

    public static void closeConnection() {

        if (client != null) {
            client.close();
        }
    }

    private static Class<?>[] getSubClasses() {
        return new Class<?>[]{Decoration.class,
                Flower.class,
                Tree.class,
                Ticket.class};
    }
}
