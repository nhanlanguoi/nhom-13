package com.project.quanlybanhang.Product;

import java.util.List;

import static com.project.quanlybanhang.Product.Productservice.processingprice;
import static com.project.quanlybanhang.Utils.Numberutils.parseSafeLongadddot;

public class Variant {
    private String id;

    private  String storage;
    private  String ram;
    private List<colorprice> colorprices;

    private String image;
    private String screen;
    private String chip;
    private String memory;
    private String camera_end;
    private String camera_start;
    private String pin;
    private String power;

    public Variant() {
    }

    public Variant(String id, String storage, String ram, List<colorprice> colorprices, String image, String screen, String chip, String memory, String camera_end, String camera_start, String pin, String power) {
        this.id = id;
        this.storage = storage;
        this.ram = ram;
        this.colorprices = colorprices;
        this.image = image;
        this.screen = screen;
        this.chip = chip;
        this.memory = memory;
        this.camera_end = camera_end;
        this.camera_start = camera_start;
        this.pin = pin;
        this.power = power;
    }

    public String getId() {
        return id;
    }

    public String getStorage() {
        return storage;
    }

    public String getRam() {
        return ram;
    }

    public List<colorprice> getColorprices() {
        return colorprices;
    }

    public String getImage() {
        return image;
    }

    public String getScreen() {
        return screen;
    }

    public String getChip() {
        return chip;
    }

    public String getMemory() {
        return memory;
    }

    public String getCamera_end() {
        return camera_end;
    }

    public String getCamera_start() {
        return camera_start;
    }

    public String getPin() {
        return pin;
    }

    public String getPower() {
        return power;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setStorage(String storage) {
        this.storage = storage;
    }

    public void setRam(String ram) {
        this.ram = ram;
    }

    public void setColorprices(List<colorprice> colorprices) {
        this.colorprices = colorprices;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setScreen(String screen) {
        this.screen = screen;
    }

    public void setChip(String chip) {
        this.chip = chip;
    }

    public void setMemory(String memory) {
        this.memory = memory;
    }

    public void setCamera_end(String camera_end) {
        this.camera_end = camera_end;
    }

    public void setCamera_start(String camera_start) {
        this.camera_start = camera_start;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public void setPower(String power) {
        this.power = power;
    }
}
