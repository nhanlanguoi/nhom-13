package com.project.quanlybanhang.Product;

import java.io.IOException;
import java.util.List;

public interface managerdataproduct {
    List<Product> getAllProducts() throws IOException;

    boolean addProduct(Product newProduct) throws IOException;

    Product getProductById(String id) throws IOException;

    List<Variant> getProductVariantsById(String productId) throws IOException;

    boolean updateProduct(Product updatedProduct) throws IOException;

    boolean deleteProduct(String productId) throws IOException;
}
