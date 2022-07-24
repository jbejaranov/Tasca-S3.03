package org.nivell1.products;

public class Tree extends Product{

    private final double height;

    public Tree(String name, float price, double height) {
        super(name, price);
        this.height = height;
    }

    public double getHeight() {
        return height;
    }
}
