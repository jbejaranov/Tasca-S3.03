package org.nivell1.utils;

import org.nivell1.products.Decoration;
import org.nivell1.products.Flower;
import org.nivell1.products.Product;
import org.nivell1.products.Tree;

import java.util.function.Predicate;

public class PropertyFilter {

    //Filtrem la propietat concreta de cada producte
    public Predicate<Product> filterProperty(Product product) {
        Predicate<Product> predicate = null;
        switch (product.getClass().getSimpleName()) {
            case "Decoration" -> predicate = product1 -> ((Decoration) product1).getMaterial().equals(((Decoration) product).getMaterial());
            case "Flower" -> predicate = product1 -> ((Flower) product1).getColor().equals(((Flower) product).getColor());
            case "Tree" -> predicate = product1 -> ((Tree) product1).getHeight() == ((Tree) product).getHeight();
        }
        return predicate;
    }
}
