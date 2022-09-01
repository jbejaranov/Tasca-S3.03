package org.nivell2.utils;

import org.nivell2.products.Product;

import java.util.Comparator;

public class ComparadorProducte implements Comparator<Product> {

    //Ordenem alfabèticament pel nom de la classe de producte
    @Override
    public int compare(Product product1, Product product2) {
        return product1.getClass().getSimpleName().compareTo(product2.getClass().getSimpleName());
    }
}
