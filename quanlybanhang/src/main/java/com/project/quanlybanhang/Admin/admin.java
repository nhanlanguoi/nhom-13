package com.project.quanlybanhang.Admin;

import com.project.quanlybanhang.Product.Product; // Thêm
import com.project.quanlybanhang.Product.Productservice; // Thêm
import org.springframework.beans.factory.annotation.Autowired; // Thêm
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model; // Thêm
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException; // Thêm
import java.util.List; // Thêm
import java.util.ArrayList; // Thêm

@Controller
public class admin { // Tên class là "admin"

    private final Productservice productService;

    @Autowired
    public admin(Productservice productService) { // Inject Productservice
        this.productService = productService;
    }

    @GetMapping("/Admin") // URL là /Admin
    public String adminPage(Model model){ // Thêm Model làm tham số
        System.out.println("[admin Controller] GET /Admin - Loading admin page.");
        try {
            List<Product> allProducts = productService.getAllProducts();
            System.out.println("[admin Controller] Fetched " + (allProducts != null ? allProducts.size() : "null") + " products for /Admin page.");
            model.addAttribute("products", allProducts); // Thêm products vào model
            model.addAttribute("productToEdit", null); // Đảm bảo productToEdit là null ban đầu
            // Bạn có thể thêm các model attributes khác nếu cần cho trang /Admin
        } catch (IOException e) {
            System.err.println("[admin Controller] IOException when fetching products for /Admin: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("errorMessage", "Lỗi khi tải dữ liệu sản phẩm: " + e.getMessage());
            model.addAttribute("products", new ArrayList<>());
        }
        return "html/admin"; // Trả về cùng một template
    }
}