package org.nivell3.products;

import org.bson.codecs.pojo.annotations.BsonDiscriminator;
import org.bson.types.ObjectId;

@BsonDiscriminator(key = "product", value = "flower")
public class Flower extends Product<String> {

    public Flower() {
    }
    public Flower(ObjectId _id, String name, float price, int quantity, String color) {
        super(_id, name, price, quantity, color);
    }
}