package com.project.quanlybanhang.Search;

import com.project.quanlybanhang.Product.Product;
import com.project.quanlybanhang.Product.Productservice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Controller
public class SearchController {

    private final Productservice productService;

    @Autowired
    public SearchController(Productservice productService) {
        this.productService = productService;
    }

    @GetMapping("/search")
    public String searchProductsByTag(@RequestParam(name = "tag", required = false, defaultValue = "") String tagName, Model model) {
        model.addAttribute("searchQuery", tagName);
        if (tagName.trim().isEmpty()) {
            model.addAttribute("noProductsMessage", "Vui lòng nhập từ khóa để tìm kiếm.");
            model.addAttribute("products", new ArrayList<>());
        } else {
            try {
                List<Product> products = productService.getProductsByTag(tagName);
                model.addAttribute("products", products);
                if (products.isEmpty()) {
                    model.addAttribute("noProductsMessage", "Không có sản phẩm nào phù hợp với tag \"" + tagName + "\".");
                }
            } catch (IOException e) {
                System.err.println("Error searching products by tag: " + tagName + " - " + e.getMessage());
                model.addAttribute("products", new ArrayList<>());
                model.addAttribute("searchErrorMessage", "Đã có lỗi xảy ra khi tìm kiếm sản phẩm.");
            }
        }
        return "html/selectproduct"; // Tên view template của bạn
    }
}