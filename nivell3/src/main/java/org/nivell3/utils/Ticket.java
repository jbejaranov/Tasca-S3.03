package org.nivell3.utils;

import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.ObjectId;
import org.nivell3.products.Decoration;
import org.nivell3.products.Flower;
import org.nivell3.products.Tree;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Ticket {

    @BsonId
    private ObjectId _id;
    @BsonProperty("dateTime")
    private String dateTime;
    @BsonProperty("decorationList")
    private final List<Decoration> decorationList = new ArrayList<>();
    @BsonProperty("flowerList")
    private final List<Flower> flowerList = new ArrayList<>();
    @BsonProperty("treeList")
    private final List<Tree> treeList = new ArrayList<>();

    public Ticket() {

    }

    public Ticket(ObjectId _id, LocalDateTime dateTime) {
        this._id = _id;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
        this.dateTime = dateTime.format(formatter);
    }

    public ObjectId get_id() {
        return _id;
    }

    public String getDateTime() {
        return dateTime;
    }

    public List<Decoration> getDecorationList() {
        return decorationList;
    }

    public List<Flower> getFlowerList() {
        return flowerList;
    }

    public List<Tree> getTreeList() {
        return treeList;
    }

    public void addDecoration(Decoration decoration) {
        decorationList.add(decoration);
    }

    public void addFlower(Flower flower) {
        flowerList.add(flower);
    }

    public void addTree(Tree tree) {
        treeList.add(tree);
    }
}
