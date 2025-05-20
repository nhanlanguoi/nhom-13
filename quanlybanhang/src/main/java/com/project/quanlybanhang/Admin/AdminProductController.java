package com.project.quanlybanhang.Admin;

import com.project.quanlybanhang.Product.Product;
import com.project.quanlybanhang.Product.Productservice;
import org.springframework.beans.factory.annotation.Autowired; // THÊM IMPORT NÀY
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/admin/products")
public class AdminProductController {

    private final Productservice productService; // Khai báo final

    @Autowired // Sử dụng constructor injection
    public AdminProductController(Productservice productService) {
        this.productService = productService;
    }

    @GetMapping("/edit")
    public String showEditProductPage(@RequestParam(name = "productId", required = false) String productId, Model model) {
        System.out.println("[AdminProductController] GET /edit - productId: " + productId);
        try {
            // Sử dụng productService đã được inject
            List<Product> allProducts = productService.getAllProducts();
            System.out.println("[AdminProductController] Fetched " + (allProducts != null ? allProducts.size() : "null") + " products via injected service.");

            if (allProducts != null) {
                System.out.println("[AdminProductController] Number of products received in controller: " + allProducts.size());
                // In ra thông tin cơ bản của từng sản phẩm để kiểm tra
                for(Product p : allProducts) {
                    System.out.println("  - Product ID: " + p.getId() + ", Name: " + p.getName());
                }
            } else {
                System.out.println("[AdminProductController] allProducts is null from service.");
            }

            model.addAttribute("products", allProducts); // Thêm vào model
            System.out.println("[AdminProductController] Added 'products' to model. Model contains 'products': " + model.containsAttribute("products"));


            if (allProducts == null || allProducts.isEmpty()) {
                System.out.println("[AdminProductController] No products found to display (list is null or empty after fetching).");
            }


            if (productId != null && allProducts != null) {
                System.out.println("[AdminProductController] Attempting to find product to edit with ID: " + productId);
                Product productToEdit = null;

                for (Product p : allProducts) {
                    if (p.getId().equals(productId)) {
                        productToEdit = p;
                        break;
                    }
                }


                if (productToEdit != null) {
                    System.out.println("[AdminProductController] Found product to edit: " + productToEdit.getName());
                    model.addAttribute("productToEdit", productToEdit);
                } else {
                    System.err.println("[AdminProductController] Product to edit with ID: " + productId + " not found in the fetched list.");
                    model.addAttribute("errorMessage", "Không tìm thấy sản phẩm với ID: " + productId);
                }
            }
        } catch (IOException e) {
            System.err.println("[AdminProductController] IOException in showEditProductPage: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("errorMessage", "Lỗi khi tải dữ liệu sản phẩm: " + e.getMessage());
            model.addAttribute("products", new ArrayList<>());
        } catch (Exception e) { // Bắt thêm các lỗi khác có thể xảy ra
            System.err.println("[AdminProductController] Unexpected error in showEditProductPage: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("errorMessage", "Lỗi không mong muốn: " + e.getMessage());
            model.addAttribute("products", new ArrayList<>());
        }
        System.out.println("[AdminProductController] Returning view 'html/admin'.");
        return "html/admin";
    }


    @PostMapping("/update")
    public String handleUpdateProduct(@ModelAttribute Product product, RedirectAttributes redirectAttributes) {

        System.out.println("[AdminProductController] POST /update - Product ID to update: " + product.getId());


        try {
            boolean success = productService.updateProduct(product); // productService đã được inject
            if (success) {
                System.out.println("[AdminProductController] Product update successful for ID: " + product.getId());
                redirectAttributes.addFlashAttribute("successMessage", "Cập nhật sản phẩm thành công!");
            } else {
                System.err.println("[AdminProductController] Product update failed for ID: " + product.getId() + ". Product not found by service.");
                redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: Không tìm thấy sản phẩm để cập nhật.");
            }
        } catch (IOException e) {
            System.err.println("[AdminProductController] IOException during product update for ID: " + product.getId());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi hệ thống khi cập nhật sản phẩm.");
        }
        return "redirect:/admin/products/edit";
    }
}