package org.nivell3.utils;

import org.nivell3.products.Product;

public class Tuple<T extends Product> {

    private Integer index;
    private T product;

    public Tuple(Integer index, T product) {
        this.index = index;
        this.product = product;
    }

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(T product) {
        this.product = product;
    }
}
