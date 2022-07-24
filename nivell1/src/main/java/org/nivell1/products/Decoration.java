package org.nivell1.products;

public class Decoration extends Product{

    private final String material;

    public Decoration(String name, float price, String material) {
        super(name, price);
        this.material = material;
    }

    public String getMaterial() {
        return material;
    }
}
