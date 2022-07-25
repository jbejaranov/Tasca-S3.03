package org.nivell1.products;

public class Flower extends Product{

    private final String color;

    public Flower(String name, float price, int quantity, String color) {
        super(name, price, quantity);
        this.color = color;
    }

    public String getColor() {
        return color;
    }
}
