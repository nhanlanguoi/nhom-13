package com.project.quanlybanhang.User;

public class User {
    private String username;
    private String password;
    private String fullname;
    private String email;
    private String ban;
    private String role;

    public User() {
    }

    public User(String username, String password, String fullname, String email,String ban,String role) {
        this.username = username;
        this.password = password;
        this.fullname = fullname;
        this.email = email;
        this.ban = ban;
        this.role = role;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getBan() {
        return ban;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setBan(String ban) {
        this.ban = ban;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
