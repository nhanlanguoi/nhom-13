package com.project.quanlybanhang.Admin;

import com.project.quanlybanhang.Product.Product; 
import com.project.quanlybanhang.Product.Productservice; 
import org.springframework.beans.factory.annotation.Autowired; 
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model; 
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException; 
import java.util.List;
import java.util.ArrayList; 

@Controller
public class admin { 

    private final Productservice productService;

    @Autowired
    public admin(Productservice productService) { 
        this.productService = productService;
    }

    @GetMapping("/Admin")
    public String adminPage(Model model){ 
        System.out.println("[admin Controller] GET /Admin - Loading admin page.");
        try {
            List<Product> allProducts = productService.getAllProducts();
            System.out.println("[admin Controller] Fetched " + (allProducts != null ? allProducts.size() : "null") + " products for /Admin page.");
            model.addAttribute("products", allProducts); 
            model.addAttribute("productToEdit", null); 
           
        } catch (IOException e) {
            System.err.println("[admin Controller] IOException when fetching products for /Admin: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("errorMessage", "Lỗi khi tải dữ liệu sản phẩm: " + e.getMessage());
            model.addAttribute("products", new ArrayList<>());
        }
        return "html/admin"; 
    }
}