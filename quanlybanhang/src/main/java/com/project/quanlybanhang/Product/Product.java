package com.project.quanlybanhang.Product;

public class Product {
    private String id;
    private String img;
    private String name;
    private int price;
    private int sale;
    private int ratting;
    private String thuonghieu;
    public Product() {}
    public Product(String id, String img, String name, int price, int sale, int ratting ,String thuonghieu) {
        this.id = id;
        this.img = img;
        this.name = name;
        this.price = price;
        this.sale = sale;
        this.ratting = ratting;
        this.thuonghieu = thuonghieu;
    }

    public String getThuonghieu() {
        return thuonghieu;
    }

    public String getId() {
        return id;
    }

    public String getImg() {
        return img;
    }

    public String getName() {
        return name;
    }

    public int getPrice() {
        return price;
    }

    public int getSale() {
        return sale;
    }

    public int getRatting() {
        return ratting;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public void setSale(int sale) {
        this.sale = sale;
    }

    public void setRatting(int ratting) {
        this.ratting = ratting;
    }

    public void setThuonghieu(String thuonghieu) {
        this.thuonghieu = thuonghieu;
    }
}
