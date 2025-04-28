package com.project.quanlybanhang.Product;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.quanlybanhang.Product.Product;
import org.springframework.stereotype.Service;


public class Productservice implements managerdataproduct {


    @Override
    public List<Product> getAllProducts() throws IOException {
        //InputStream readfile = getClass().getResourceAsStream("/static/data-product/product.json");
        File readfile = new File("src/main/resources/static/data-product/product.json");

        ObjectMapper mapper = new ObjectMapper();

        List<Product> listproduct = mapper.readValue(readfile , new TypeReference<List<Product>>() {});
        return listproduct;
    }

    @Override
    public Product getProductById(String id) throws IOException {
        List<Product> products = getAllProducts();

        for (Product p : products){
            if(p.getId().equals(id)){
                return p;
            }
        }

        return null;
    }
}
