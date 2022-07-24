package org.nivell1.products;

import java.util.UUID;

public abstract class Product {

    private String name;
    private float price;
    private final String id;

    //ID, un int o un UUID?
    //Si es fa servir un int, abans d'afegir producte s'hauria de mirar si ja existeix.
    protected Product(String name, float price) {
        id = UUID.randomUUID().toString();
        this.name = name;
        this.price = price;
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
}
