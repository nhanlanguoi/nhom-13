package com.project.quanlybanhang.Cart;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.project.quanlybanhang.Product.Product;
import com.project.quanlybanhang.Product.Variant;

import static com.project.quanlybanhang.Product.Productservice.processingprice;
import static com.project.quanlybanhang.Utils.Numberutils.parseSafeLongadddot;


@JsonIgnoreProperties(ignoreUnknown = true)
public class iteams {
    private String variantId;
    private int quantity;
    private String color;
    private long priceAtOrderTime;
    private  int discount;
    private String image;
    private Variant variant;
    private Product product;

    public iteams() {
    }

    public iteams(String variantId, int quantity, String color, long priceAtOrderTime, int discount, String image, Variant variant, Product product) {
        this.variantId = variantId;
        this.quantity = quantity;
        this.color = color;
        this.priceAtOrderTime = priceAtOrderTime;
        this.discount = discount;
        this.image = image;
        this.variant = variant;
        this.product = product;
    }

    public String getVariantId() {
        return variantId;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getColor() {
        return color;
    }

    public long getPriceAtOrderTime() {
        return priceAtOrderTime;
    }

    public int getDiscount() {
        return discount;
    }

    public String getImage() {
        return image;
    }

    public Variant getVariant() {
        return variant;
    }

    public Product getProduct() {
        return product;
    }

    public void setVariantId(String variantId) {
        this.variantId = variantId;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public void setPriceAtOrderTime(long priceAtOrderTime) {
        this.priceAtOrderTime = priceAtOrderTime;
    }

    public void setDiscount(int discount) {
        this.discount = discount;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setVariant(Variant variant) {
        this.variant = variant;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public String getFormattedPricewas() {
        String pricediscount = Long.toString(processingprice(priceAtOrderTime, discount));
        return parseSafeLongadddot(pricediscount);
    }

    public String getFormattedPrice(){
        return parseSafeLongadddot(Long.toString(priceAtOrderTime));
    }

    public String getFormattedPricesale() {
        String pricediscount = Long.toString(processingprice(priceAtOrderTime, discount));
        return parseSafeLongadddot(Long.toString(priceAtOrderTime-Long.parseLong(pricediscount)));
    }

}
