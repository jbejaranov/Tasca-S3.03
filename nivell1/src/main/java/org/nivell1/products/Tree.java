package org.nivell1.products;

public class Tree extends Product{

    private final double height;

    public Tree(String name, float price, int quantity, double height) {
        super(name, price, quantity);
        this.height = height;
    }

    public double getHeight() {
        return height;
    }
}
