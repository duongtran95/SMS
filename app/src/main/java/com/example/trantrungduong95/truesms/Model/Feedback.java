package com.example.trantrungduong95.truesms.Model;


public class Feedback {

    private String id;
    private String name;
    private String email;
    private String address;
    private String describe;

    public Feedback() {
    }

    public Feedback(String id, String name, String email, String address, String describe) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.address = address;
        this.describe = describe;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDescribe() {
        return describe;
    }

    public void setDescribe(String describe) {
        this.describe = describe;
    }
}
