package org.nivell1.products;

import java.util.UUID;

public abstract class Product {

    private String name;
    private float price;
    private final String id;

    private int quantity;

    //TODO: cambiar ID para que sea el timestamp
    protected Product(String name, float price, int quantity) {
        id = UUID.randomUUID().toString();
        this.name = name;
        this.price = price;
        this.quantity = quantity;
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

    public String getId() {
        return id;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
