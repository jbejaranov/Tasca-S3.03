package org.nivell1.stores;

import org.nivell1.products.Product;

import java.util.List;
import java.util.UUID;

public class Ticket {

    private final List<Product> list;
    private String id;

    public Ticket(List<Product> list) {
        id = UUID.randomUUID().toString();
        this.list = list;
    }

    public List<Product> getList() {
        return list;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
