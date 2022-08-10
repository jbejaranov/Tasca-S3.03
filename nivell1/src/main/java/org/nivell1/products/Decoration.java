package org.nivell1.products;

public class Decoration extends Product{

    private  String material;

    public Decoration(String name, float price, int quantity, String material) {
        super(name, price, quantity);
        this.material = material;
    }

    public String getMaterial() {
        return material;
    }

    public void setMaterial(String material) {
        this.material = material;
    }
}
