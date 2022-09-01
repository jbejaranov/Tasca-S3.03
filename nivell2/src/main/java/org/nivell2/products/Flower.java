package org.nivell2.products;

public class Flower extends Product {

    private String color;

    public Flower(int id, String name, float price, int quantity, String color) {
        super(id, name, price, quantity);
        this.color = color;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}
