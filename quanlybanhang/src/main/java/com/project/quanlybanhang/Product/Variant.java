package com.project.quanlybanhang.Product;

import static com.project.quanlybanhang.Product.Productservice.processingprice;
import static com.project.quanlybanhang.Utils.Numberutils.parseSafeLongadddot;

public class Variant {
    private String id;
    private String color;
    private  String storage;
    private  String ram;
    private String price;
    private  int discount;
    private String image;
    private String screen;
    private String Chip;
    private String Memory;
    private String Camera_end;
    private String Camera_start;
    private String Pin;
    private String Power;

    public Variant() {
    }

    public Variant(String id, String color, String storage, String ram, String price, int discount, String image, String screen, String chip, String memory, String camera_end, String camera_start, String pin, String power) {
        this.id = id;
        this.color = color;
        this.storage = storage;
        this.ram = ram;
        this.price = price;
        this.discount = discount;
        this.image = image;
        this.screen = screen;
        this.Chip = chip;
        this.Memory = memory;
        this.Camera_end = camera_end;
        this.Camera_start = camera_start;
        this.Pin = pin;
        this.Power = power;
    }

    public String getId() {
        return id;
    }

    public String getColor() {
        return color;
    }

    public String getStorage() {
        return storage;
    }

    public String getRam() {
        return ram;
    }

    public String getPrice() {
        return price;
    }

    public int getDiscount() {
        return discount;
    }

    public String getImage() {
        return image;
    }

    public String getScreen() {
        return screen;
    }

    public String getChip() {
        return Chip;
    }

    public String getMemory() {
        return Memory;
    }

    public String getCamera_end() {
        return Camera_end;
    }

    public String getCamera_start() {
        return Camera_start;
    }

    public String getPin() {
        return Pin;
    }

    public String getPower() {
        return Power;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public void setStorage(String storage) {
        this.storage = storage;
    }

    public void setRam(String ram) {
        this.ram = ram;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public void setDiscount(int discount) {
        this.discount = discount;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setScreen(String screen) {
        this.screen = screen;
    }

    public void setChip(String chip) {
        this.Chip = chip;
    }

    public void setMemory(String memory) {
        this.Memory = memory;
    }

    public void setCamera_end(String camera_end) {
        this.Camera_end = camera_end;
    }

    public void setCamera_start(String camera_start) {
        this.Camera_start = camera_start;
    }

    public void setPin(String pin) {
        this.Pin = pin;
    }

    public void setPower(String power) {
        this.Power = power;
    }


    public String getFormattedPrice() {
        String pricediscount = Long.toString(processingprice(Long.parseLong(price), discount));
        return parseSafeLongadddot(pricediscount);
    }

}
