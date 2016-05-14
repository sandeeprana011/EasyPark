package com.techlo.easypark;

/**
 * Created by sandeeprana on 11/05/16.
 */
public class User {
    private String name;
    private String phone;
    private String email;
    private String photoUrl;
    public User(String name,String phone,String email,String photoUrl){
        this.name=name;
        this.phone=phone;
        this.email=email;
        this.photoUrl=photoUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }
}
