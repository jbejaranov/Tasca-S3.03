package org.nivell3.utils;

import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.ObjectId;
import org.nivell3.products.Decoration;
import org.nivell3.products.Flower;
import org.nivell3.products.Tree;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Ticket {

    @BsonId
    private ObjectId _id;
    @BsonProperty("dateTime")
    private LocalDateTime dateTime;
    //    @BsonProperty("decorationList")
//    private List<Decoration> decorationList;
//    @BsonProperty("flowerList")
//    private List<Flower> flowerList;
//    @BsonProperty("treeList")
//    private List<Tree> treeList;
    @BsonProperty("decorationList")
    private final List<Decoration> decorationList = new ArrayList<>();
    @BsonProperty("flowerList")
    private final List<Flower> flowerList = new ArrayList<>();
    @BsonProperty("treeList")
    private final List<Tree> treeList = new ArrayList<>();

//    public Ticket(ObjectId _id, LocalDateTime dateTime, List<Decoration> decorationList, List<Flower> flowerList, List<Tree> treeList) {
//        this._id = _id;
//        this.dateTime = dateTime;
//        this.decorationList = decorationList;
//        this.flowerList = flowerList;
//        this.treeList = treeList;
//    }


    public Ticket(ObjectId _id, LocalDateTime dateTime) {
        this._id = _id;
        this.dateTime = dateTime;
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
