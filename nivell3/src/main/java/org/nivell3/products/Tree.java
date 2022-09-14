package org.nivell3.products;

import org.bson.codecs.pojo.annotations.BsonDiscriminator;
import org.bson.types.ObjectId;

@BsonDiscriminator(key = "product", value = "tree")
public class Tree extends Product<Double> {

    public Tree() {
    }

    public Tree(ObjectId _id, String name, float price, int quantity, double height) {
        super(_id, name, price, quantity, height);
    }
}
