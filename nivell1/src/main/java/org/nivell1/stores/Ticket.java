package org.nivell1.stores;

import java.util.List;
import java.util.UUID;

public class Ticket {

    private final List<List<String>> list;
    private String id;

    public Ticket(List<List<String>> list) {
        id = UUID.randomUUID().toString();
        this.list = list;
    }

    public List<List<String>> getList() {
        return list;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
