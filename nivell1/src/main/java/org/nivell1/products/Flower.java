package org.nivell1.products;

public class Flower extends Product{

    private final String color;

    public Flower(String name, float price, String color) {
        super(name, price);
        this.color = color;
    }

    public String getColor() {
        return color;
    }
}
