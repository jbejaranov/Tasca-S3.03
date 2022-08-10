package org.nivell1.products;

public class Flower extends Product{

    private String color;

    public Flower(String name, float price, int quantity, String color) {
        super(name, price, quantity);
        this.color = color;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}
