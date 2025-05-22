package com.project.quanlybanhang.Product;

import com.fasterxml.jackson.annotation.JsonIgnore; // THÊM IMPORT NÀY
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

// ... (các import khác nếu có)

@JsonIgnoreProperties(ignoreUnknown = true)
public class colorprice {
    private int id;
    private String color;
    private String price;
    private int discount;

    public colorprice() {
    }

    public colorprice(int id, String color, String price, int discount) {
        this.id = id;
        this.color = color;
        this.price = price;
        this.discount = discount;
    }

    // Getters
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

    // Setters
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

    @JsonIgnore
    public String getFormattedPrice() {
        
        if (this.price == null || this.price.trim().isEmpty()) {
            return "0.";
        }
        try {
            long priceValue = Long.parseLong(this.price);
            long discountedPriceValue = Productservice.processingprice(priceValue, this.discount); // Chú ý: Productservice.processingprice có thể cần instance nếu không phải static
            return com.project.quanlybanhang.Utils.Numberutils.parseSafeLongadddot(Long.toString(discountedPriceValue));
        } catch (NumberFormatException e) {
            System.err.println("Lỗi NumberFormatException trong getFormattedPrice cho price: '" + this.price + "' - Lỗi: " + e.getMessage());
            return "Lỗi giá.";
        }
    }
}