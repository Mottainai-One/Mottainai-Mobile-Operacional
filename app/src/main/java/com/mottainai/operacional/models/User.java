package com.mottainai.operacional.models;

public class User {
    private String uid;
    private String name;
    private String role;
    private String storeId;

    public User(String uid, String name, String role, String storeId) {
        this.uid = uid;
        this.name = name;
        this.role = role;
        this.storeId = storeId;
    }

    public String getUid()      { return uid; }
    public String getName()     { return name; }
    public String getRole()     { return role; }
    public String getStoreId()  { return storeId; }
}
