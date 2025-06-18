
package com.project.quanlybanhang.Product;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products") // Đường dẫn cơ sở cho API liên quan đến sản phẩm
public class ProductApiController {

    private final Productservice productService;

    @Autowired
    public ProductApiController(Productservice productService) {
        this.productService = productService;
    }

    @GetMapping("/category/{categoryName}")
    public ResponseEntity<List<Product>> getProductsByCategory(@PathVariable String categoryName) {
        try {
            List<Product> products = productService.getProductsByCategory(categoryName); // Sử dụng phương thức mới
            if (products.isEmpty()) {
                // Trả về 204 No Content nếu không có sản phẩm nào
                return ResponseEntity.noContent().build();
            }
            // Trả về 200 OK cùng với danh sách sản phẩm
            return ResponseEntity.ok(products);
        } catch (IOException e) {
            // Ghi log lỗi ở đây nếu cần
            System.err.println("Error fetching products by category: " + categoryName + " - " + e.getMessage());
            e.printStackTrace();
            // Trả về lỗi 500 Internal Server Error
            return ResponseEntity.status(500).body(null);
        }
    }

    /**
     * API để gợi ý các tags dựa trên query của người dùng.
     * @param query Từ khóa người dùng nhập vào.
     * @return Danh sách các tag phù hợp.
     */
    // Trong ProductApiController.java
    @GetMapping("/tags/suggest")
    public ResponseEntity<List<String>> suggestTags(
            @RequestParam(name = "q", required = false, defaultValue = "") String query) {
        try {
            List<String> allTags = productService.getAllUniqueTags();
            System.out.println("[APIController DEBUG] suggestTags - Query: '" + query + "'");
            System.out.println("[APIController DEBUG] suggestTags - All unique tags từ service: " + allTags);

            if (query.isEmpty()) {
                List<String> limitedTags = allTags.stream().limit(10).collect(Collectors.toList());
                System.out.println("[APIController DEBUG] suggestTags - Query rỗng, trả về: " + limitedTags);
                return ResponseEntity.ok(limitedTags);
            }
            String lowerCaseQuery = query.toLowerCase();
            List<String> matchedTags = allTags.stream()
                    .filter(tag -> tag.toLowerCase().contains(lowerCaseQuery))
                    .collect(Collectors.toList()); // Bỏ tạm .limit(10) để xem có match không đã
            System.out.println("[APIController DEBUG] suggestTags - Tags khớp với query: " + matchedTags);
            return ResponseEntity.ok(matchedTags);
        } catch (IOException e) {
            System.err.println("Error suggesting tags: " + e.getMessage());
            return ResponseEntity.status(500).body(new ArrayList<>());
        }
    }
    
    @GetMapping("/{categoryName}/brand/{brandName}")
    public ResponseEntity<List<Product>> getProductsByBrandWithCategory(@PathVariable String categoryName, @PathVariable String brandName) {
        try {
            List<Product> products = productService.getProductsByBrandWithCategory(categoryName, brandName); // Sử dụng phương thức mới
            if (products.isEmpty()) {
                // Trả về 204 No Content nếu không có sản phẩm nào
                return ResponseEntity.noContent().build();
            }
            // Trả về 200 OK cùng với danh sách sản phẩm
            return ResponseEntity.ok(products);
        } catch (IOException e) {
            // Ghi log lỗi ở đây nếu cần
            System.err.println("Error fetching products by category: " + brandName + " - " + e.getMessage());
            e.printStackTrace();
            // Trả về lỗi 500 Internal Server Error
            return ResponseEntity.status(500).body(null);
        }
    }
}