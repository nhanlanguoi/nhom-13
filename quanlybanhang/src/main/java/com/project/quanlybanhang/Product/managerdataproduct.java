package com.project.quanlybanhang.Product;

import java.io.IOException;
import java.util.List;

public interface managerdataproduct {
    List<Product> getAllProducts() throws IOException;
    Product getProductById(int id) throws IOException;
}
