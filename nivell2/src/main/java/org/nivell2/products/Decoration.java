package org.nivell2.products;

public class Decoration extends Product{

    private String material;

    public Decoration(int id, String name, float price, int quantity, String material) {
        super(id, name, price, quantity);
        this.material = material;
    }

    public String getMaterial() {
        return material;
    }

    public void setMaterial(String material) {
        this.material = material;
    }
}
