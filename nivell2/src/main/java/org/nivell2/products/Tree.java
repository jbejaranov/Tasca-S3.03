package org.nivell2.products;

public class Tree extends Product {

    private double height;

    public Tree(int id, String name, float price, int quantity, double height) {
        super(id, name, price, quantity);
        this.height = height;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }
}
