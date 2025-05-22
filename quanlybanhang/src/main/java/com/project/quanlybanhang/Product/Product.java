package com.project.quanlybanhang.Product;


import com.project.quanlybanhang.Product.Variant;
import java.util.List;

public class Product {
    private String id;
    private String name;
    private String brand;
    private String category;
    private String description;
    private double rating;
    private List<Variant> variants;
    private List<String> tags;
    private String releaseDate;

    public Product(String id, String name, String brand, String category, String description, double rating, List<Variant> variants, List<String> tags, String releaseDate) {
        this.id = id;
        this.name = name;
        this.brand = brand;
        this.category = category;
        this.description = description;
        this.rating = rating;
        this.variants = variants;
        this.tags = tags;
        this.releaseDate = releaseDate;
    }

    public Product() {
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getBrand() {
        return brand;
    }

    public String getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }

    public double getRating() {
        return rating;
    }

    public List<Variant> getVariants() {
        return variants;
    }

    public List<String> getTags() {
        return tags;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public void setVariants(List<Variant> variants) {
        this.variants = variants;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }


}
