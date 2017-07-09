package com.example.trantrungduong95.truesms.Model;

public class Block {
    private int id_;
    private String number_;

    public Block(int id, String number) {
        this.id_ = id;
        this.number_ = number;
    }
    public Block() {
    }

    public int getId() {
        return id_;
    }

    public void setId(int id) {
        this.id_ = id;
    }

    public String getNumber() {
        return number_;
    }

    public void setNumber(String number) {
        this.number_ = number;
    }
}
