package com.project.quanlybanhang.Cart;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.project.quanlybanhang.Buy.BuyData;
import com.project.quanlybanhang.Product.Product;
import com.project.quanlybanhang.Product.Productservice;
import com.project.quanlybanhang.Product.Variant;
import com.project.quanlybanhang.Product.colorprice;
import com.project.quanlybanhang.User.User;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.*;


@Controller
public class Cartcontroller {
   
    private String URL = "src/main/resources/static/data-cart/cart.json";
    Productservice productService = new Productservice();
    private String URL_BUY = "src/main/resources/static/data-buy/buy.json";

    private static final ObjectMapper mapper = new ObjectMapper();

    static {
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @GetMapping("/cart")
    public String cart(HttpSession session, Model model) throws IOException {
        Object userObj = session.getAttribute("user");
        if (userObj == null) {
            model.addAttribute("showLoginPopup", true); // Specific flag for login popup from cart
            model.addAttribute("products", productService.getAllProducts());
            return "html/home";
        }

        User user = (User) session.getAttribute("user");
        File readfile = new File(URL);
        if (!readfile.exists()) {
            readfile.getParentFile().mkdirs();
            readfile.createNewFile();
            Files.write(readfile.toPath(), "[]".getBytes(StandardCharsets.UTF_8));
        }

        List<cart> cartList = mapper.readValue(readfile, new TypeReference<List<cart>>() {});
        cart cartobject = new cart(); // Default empty cart
        Optional<cart> userCartOpt = cartList.stream()
                .filter(c -> c.getUsername().equals(user.getUsername()))
                .findFirst();
        if (userCartOpt.isPresent()) {
            cartobject = userCartOpt.get();
        } else {
          
            cartobject.setUsername(user.getUsername());
            cartobject.setIteam(new ArrayList<>()); // Initialize with empty item list
           
        }

        model.addAttribute("cart", cartobject.getIteam() != null ? cartobject.getIteam() : new ArrayList<>());
       
        return "html/cart";
    }


    @PostMapping("/add-to-cart")
    public String addtocart(@RequestParam String productId, @RequestParam String variantid,
                            @RequestParam int colorpriceid, HttpSession session, Model model) throws IOException {

        Object userObj = session.getAttribute("user");
        if (userObj == null) {
            model.addAttribute("showLoginPopup", true); // Specific flag for login popup from cart
            model.addAttribute("products", productService.getAllProducts());
            return "html/home";
        }

        User user = (User) userObj; // Safe cast after check

        cart cartToUpdate = new cart();
        LocalDateTime now = LocalDateTime.now();
        cartToUpdate.setCreatedatdaytime(now);
        cartToUpdate.setUsername(user.getUsername());

        Product productobject = productService.getProductById(productId);
        if (productobject == null) {
            // Handle product not found - redirect with error?
            return "redirect:/cart?error=productNotFound";
        }

        iteams iteamsobject = new iteams();
        iteamsobject.setProduct(productobject);

        Optional<Variant> foundVariant = productobject.getVariants().stream()
                .filter(v -> v.getId().equals(variantid))
                .findFirst();

        if (foundVariant.isPresent()) {
            Variant variant = foundVariant.get();
            Optional<colorprice> foundColorPrice = variant.getColorprices().stream()
                    .filter(cp -> cp.getId() == colorpriceid)
                    .findFirst();

            if (foundColorPrice.isPresent()) {
                colorprice cp = foundColorPrice.get();
                iteamsobject.setQuantity(1); // Default quantity
                iteamsobject.setVariantId(variantid);
                iteamsobject.setVariant(variant);
                iteamsobject.setColor(cp.getColor());
                iteamsobject.setPriceAtOrderTime(Long.parseLong(cp.getPrice()));
                iteamsobject.setDiscount(cp.getDiscount());
                iteamsobject.setImage(variant.getImage()); // Assuming variant has a general image
            } else {
                // Handle colorprice not found
                return "redirect:/cart?error=colorPriceNotFound";
            }
        } else {
            // Handle variant not found
            return "redirect:/cart?error=variantNotFound";
        }

        Cartutils.addToCart(cartToUpdate, iteamsobject); 
        return "redirect:/cart";
    }

    @PostMapping("/add-to-cart-and-buy")
    public String addtocartandbuy(@RequestParam String productId,
                                  @RequestParam String variantid,
                                  @RequestParam int colorpriceid, 
                                  HttpSession session,
                                  Model model,
                                  RedirectAttributes redirectAttributes) throws IOException { 

        Object userObj = session.getAttribute("user");
       
        String productPageUrl = "/product/" + productId + "/" + variantid;
       
        if (colorpriceid > 0) { 
            productPageUrl += "?colorid=" + colorpriceid;
        }


        if (userObj == null) {
          
            model.addAttribute("showLoginPopup", true); // Specific flag for login popup from cart
            model.addAttribute("products", productService.getAllProducts());
            return "html/home";
        }

        User user = (User) userObj;

        cart cartToUpdate = new cart();
        LocalDateTime now = LocalDateTime.now();
        cartToUpdate.setCreatedatdaytime(now);
        cartToUpdate.setUsername(user.getUsername());

        Product productobject = productService.getProductById(productId);
        if (productobject == null) {
            redirectAttributes.addFlashAttribute("cart_error_message", "Sản phẩm không tìm thấy.");
            return "redirect:" + productPageUrl;
        }

        iteams iteamsobject = new iteams();
        iteamsobject.setProduct(productobject);

        Optional<Variant> foundVariant = productobject.getVariants().stream()
                .filter(v -> v.getId().equals(variantid))
                .findFirst();

        if (foundVariant.isPresent()) {
            Variant variant = foundVariant.get();
            Optional<colorprice> foundColorPrice = variant.getColorprices().stream()
                    .filter(cp -> cp.getId() == colorpriceid)
                    .findFirst();

            if (foundColorPrice.isPresent()) {
                colorprice cp = foundColorPrice.get();
                iteamsobject.setQuantity(1);
                iteamsobject.setVariantId(variantid);
                iteamsobject.setVariant(variant);
                iteamsobject.setColor(cp.getColor());
                iteamsobject.setPriceAtOrderTime(Long.parseLong(cp.getPrice()));
                iteamsobject.setDiscount(cp.getDiscount());
                iteamsobject.setImage(variant.getImage());
            } else {
                redirectAttributes.addFlashAttribute("cart_error_message", "Màu sắc hoặc giá không hợp lệ.");
                return "redirect:" + productPageUrl;
            }
        } else {
            redirectAttributes.addFlashAttribute("cart_error_message", "Phiên bản sản phẩm không hợp lệ.");
            return "redirect:" + productPageUrl;
        }

        try {
            // Sử dụng phương thức updateCartFile đã được định nghĩa trong các câu trả lời trước
            // để xử lý logic đọc, cập nhật và ghi file JSON.
            updateCartFile(user.getUsername(), iteamsobject, now);
            redirectAttributes.addFlashAttribute("cart_success_message", "Đã thêm sản phẩm vào giỏ hàng!");
        } catch (IOException e) {
            System.err.println("Lỗi khi lưu giỏ hàng: " + e.getMessage());
            redirectAttributes.addFlashAttribute("cart_error_message", "Có lỗi xảy ra khi lưu giỏ hàng.");
        }

        // Redirect trở lại trang sản phẩm hiện tại
        return "redirect:" + productPageUrl;
    }

   
    private synchronized void updateCartFile(String username, iteams newItem, LocalDateTime updateTime) throws IOException {
        File cartFile = new File(URL);
        List<cart> allCarts;

        if (!cartFile.exists() || cartFile.length() == 0) {
            if(cartFile.getParentFile() != null) cartFile.getParentFile().mkdirs();
            Files.write(cartFile.toPath(), "[]".getBytes(StandardCharsets.UTF_8));
            allCarts = new ArrayList<>();
        } else {
            allCarts = mapper.readValue(cartFile, new TypeReference<List<cart>>() {});
        }

        Optional<cart> userCartOpt = allCarts.stream()
                .filter(c -> c.getUsername().equals(username))
                .findFirst();

        cart userCart;
        if (userCartOpt.isPresent()) {
            userCart = userCartOpt.get();
            if (userCart.getIteam() == null) {
                userCart.setIteam(new ArrayList<>());
            }
            Optional<iteams> existingItemOpt = userCart.getIteam().stream()
                    .filter(it -> it.getProduct().getId().equals(newItem.getProduct().getId()) &&
                            Objects.equals(it.getVariantId(), newItem.getVariantId()) &&
                            Objects.equals(it.getColor(), newItem.getColor()))
                    .findFirst();

            if (existingItemOpt.isPresent()) {
                iteams existingItem = existingItemOpt.get();
                existingItem.setQuantity(existingItem.getQuantity() + newItem.getQuantity());
            } else {
                userCart.getIteam().add(newItem);
            }
            userCart.setCreatedatdaytime(updateTime);
        } else {
            userCart = new cart();
            userCart.setUsername(username);
            userCart.setCreatedatdaytime(updateTime);
            userCart.setIteam(new ArrayList<>(Collections.singletonList(newItem)));
            allCarts.add(userCart);
        }
        mapper.writeValue(cartFile, allCarts);
    }
    // ... (Các phương thức khác của controller)


    @PostMapping("/cart/confirm")
    public String confirm(@RequestParam String variantid,
                          @RequestParam String action,
                          @RequestParam int quantity, // This quantity is from the cart item
                          @RequestParam String productid,
                          @RequestParam String color,
                          HttpSession session, Model model) throws IOException {

        User user = (User) session.getAttribute("user");
        if (user == null) {
            model.addAttribute("showLoginPopupFromCart", true);
            model.addAttribute("products", productService.getAllProducts());
            return "html/home"; // Or return "redirect:/login";
        }

        File cartFile = new File(URL);
        List<cart> cartList = new ArrayList<>();
        if (cartFile.exists() && cartFile.length() > 0) {
            try {
                cartList = mapper.readValue(cartFile, new TypeReference<List<cart>>() {});
            } catch (IOException e) {
                System.err.println("Lỗi đọc cart.json trong confirm: " + e.getMessage());
                // Potentially initialize cartList = new ArrayList<>() to prevent further errors
            }
        }

        boolean cartUpdated = false;
        cart currentUserCart = null;
        for (cart c : cartList) {
            if (c.getUsername().equals(user.getUsername())) {
                currentUserCart = c;
                break;
            }
        }

        if (currentUserCart == null) { // Should not happen if user has items, but good to check
            return "redirect:/cart?error=noCartFound";
        }

        if ("remove".equals(action)) {
            if (currentUserCart.getIteam() != null) {
                currentUserCart.getIteam().removeIf(item -> item.getVariantId().equals(variantid) && item.getColor().equals(color));
                cartUpdated = true;
            }
        } else if ("add".equals(action)) { 
            
            Map<String, Object> itemToBuyDetails = new HashMap<>();
            itemToBuyDetails.put("productId", productid);
            itemToBuyDetails.put("variantId", variantid);
            itemToBuyDetails.put("color", color);
            itemToBuyDetails.put("quantity", quantity);
           
            model.addAttribute("itemToBuyDetails", itemToBuyDetails);
            model.addAttribute("showAddressPopup", true); // Flag to show the new popup

           
            model.addAttribute("cart", currentUserCart.getIteam() != null ? currentUserCart.getIteam() : new ArrayList<>());
           
            return "html/cart"; 
        }
      
        if (cartUpdated) {
            if (!cartFile.exists()) {
                cartFile.getParentFile().mkdirs();
                cartFile.createNewFile(); 
            }
            mapper.writeValue(cartFile, cartList);
        }

        return "redirect:/cart";
    }




}