package org.nivell3.products;

import org.bson.codecs.pojo.annotations.BsonDiscriminator;
import org.bson.types.ObjectId;

@BsonDiscriminator(key = "product", value = "decoration")
public class Decoration extends Product<String>{

    public Decoration() {
    }

    public Decoration(ObjectId _id, String name, float price, int quantity, String material) {
        super(_id, name, price, quantity, material);
    }
}