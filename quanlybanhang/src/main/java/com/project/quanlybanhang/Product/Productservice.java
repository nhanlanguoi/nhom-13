package com.project.quanlybanhang.Product;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.quanlybanhang.Product.Product;
import org.springframework.stereotype.Service;


public class Productservice implements managerdataproduct {

    private String URL = "src/main/resources/static/data-product/product.json";
    @Override
    public List<Product> getAllProducts() throws IOException {

        File readfile = new File(URL);

        ObjectMapper mapper = new ObjectMapper();

        List<Product> listproduct = mapper.readValue(readfile , new TypeReference<List<Product>>() {});
        System.out.println("Danh sách sản phẩm: " + listproduct);
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

    @Override
    public List<Variant> getProductVariantsById(String productId) throws IOException {
        List<Product> productList = getAllProducts();
        // Tìm sản phẩm theo productId
        for (Product product : productList) {
            if (product.getId().equals(productId)) {
                // Trả về danh sách variants của sản phẩm có id khớp
                return product.getVariants();
            }
        }
        // Nếu không tìm thấy sản phẩm với productId, trả về danh sách rỗng
        return new ArrayList<>();
    }


    public static long processingprice(long price, int discount){

        long tax = (long)(price*(discount/100.0));

        long pricechange = price-tax;

        return pricechange;
    }


}
