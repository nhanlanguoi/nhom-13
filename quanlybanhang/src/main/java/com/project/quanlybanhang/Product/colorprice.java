package com.project.quanlybanhang.Product;

import static com.project.quanlybanhang.Product.Productservice.processingprice;
import static com.project.quanlybanhang.Utils.Numberutils.parseSafeLongadddot;

public class colorprice {
    private int id;
    private String color;
    private String price;
    private  int discount;
    public colorprice() {
    }

    public colorprice(int id, String color, String price, int discount) {
        this.id = id;
        this.color = color;
        this.price = price;
        this.discount = discount;
    }

    public int getId() {
        return id;
    }

    public String getColor() {
        return color;
    }

    public String getPrice() {
        return price;
    }

    public int getDiscount() {
        return discount;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public void setDiscount(int discount) {
        this.discount = discount;
    }

    public String getFormattedPrice() {
        String pricediscount = Long.toString(processingprice(Long.parseLong(price), discount));
        return parseSafeLongadddot(pricediscount);
    }
}
