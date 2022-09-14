package org.nivell3.products;

import org.bson.codecs.pojo.annotations.BsonDiscriminator;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.ObjectId;

@BsonDiscriminator(key = "product")
public abstract class Product<T> {

    @BsonId
    private ObjectId _id;
    @BsonProperty("name")
    private String name;
    @BsonProperty("price")
    private float price;
    @BsonProperty("quantity")
    private int quantity;
    @BsonProperty("property")
    private T property;

    public Product() {

    }

    public Product(ObjectId _id, String name, float price, int quantity, T property) {
        this._id = _id;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.property = property;
    }

    public void setId(ObjectId _id) {
        this._id = _id;
    }

    public ObjectId getId() {
        return _id;
    }

    public T getProperty() {
        return property;
    }

    public void setProperty(T property) {
        this.property = property;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}

