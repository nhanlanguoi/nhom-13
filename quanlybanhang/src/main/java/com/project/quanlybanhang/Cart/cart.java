package com.project.quanlybanhang.Cart;

import java.time.LocalDateTime;
import java.util.List;


public class cart {

    private String username;
    private LocalDateTime createdatdaytime;

    private List<iteams> iteam;

    public cart() {
    }

    public cart( String username, LocalDateTime createdatdaytime, List<iteams> iteam) {
        this.username = username;
        this.createdatdaytime = createdatdaytime;
        this.iteam = iteam;
    }

    public String getUsername() {
        return username;
    }

    public LocalDateTime getCreatedatdaytime() {
        return createdatdaytime;
    }

    public List<iteams> getIteam() {
        return iteam;
    }





    public void setUsername(String username) {
        this.username = username;
    }

    public void setCreatedatdaytime(LocalDateTime createdatdaytime) {
        this.createdatdaytime = createdatdaytime;
    }

    public void setIteam(List<iteams> iteam) {
        this.iteam = iteam;
    }


}
