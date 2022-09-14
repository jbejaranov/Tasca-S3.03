package org.nivell3.utils;

import org.bson.types.ObjectId;
import org.nivell3.products.Product;

import java.time.LocalDateTime;
import java.util.List;

public class Ticket {

    private ObjectId _id;
    private LocalDateTime dateTime;
    private List<Product> productList;

    public Ticket(ObjectId _id, LocalDateTime dateTime, List<Product> productList) {
        this._id = _id;
        this.dateTime = dateTime;
        this.productList = productList;
    }

    public ObjectId get_id() {
        return _id;
    }

    public void set_id(ObjectId _id) {
        this._id = _id;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public List<Product> getProductList() {
        return productList;
    }

    public void setProductList(List<Product> productList) {
        this.productList = productList;
    }
}
