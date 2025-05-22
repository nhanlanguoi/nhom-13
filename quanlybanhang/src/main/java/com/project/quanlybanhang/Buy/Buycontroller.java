package com.project.quanlybanhang.Buy;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.project.quanlybanhang.Cart.cart; 
import com.project.quanlybanhang.User.User; 
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Controller
public class Buycontroller {

    private static final String URL_BUY = "src/main/resources/static/data-buy/buy.json";
    private static final String URL_CART = "src/main/resources/static/data-cart/cart.json"; 

    private static final ObjectMapper mapper = new ObjectMapper();

    static {
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.enable(SerializationFeature.INDENT_OUTPUT); 
    }

    @PostMapping("/save-buy")
    public String saveBuy(@RequestParam String address,
                          @RequestParam String phone,
                          @RequestParam String productId,    
                          @RequestParam String variantId,     
                          @RequestParam String color,         
                          @RequestParam int quantity,       
                          HttpSession session) {

        User user = (User) session.getAttribute("user");
        if (user == null) {
            
            return "redirect:/login?error=sessionExpired";
        }

        try {
            
            BuyData newBuyEntry = new BuyData(
                    variantId,
                    "confirmed_purchase", 
                    quantity,
                    productId,
                    color,
                    user.getUsername(),
                    LocalDateTime.now(),
                    address,
                    phone
            );

            
            File buyFile = new File(URL_BUY);
            List<BuyData> buyList;
            if (buyFile.exists() && buyFile.length() > 0) {
                buyList = mapper.readValue(buyFile, new TypeReference<List<BuyData>>() {});
            } else {
                buyList = new ArrayList<>();
            }

            
            buyList.add(newBuyEntry);
            if (!buyFile.exists()) {
                buyFile.getParentFile().mkdirs();
                buyFile.createNewFile();
            }
            mapper.writeValue(buyFile, buyList);
            System.out.println("Successfully saved to buy.json: " + newBuyEntry);


            // 4. Remove item from cart.json
            File cartFile = new File(URL_CART);
            List<cart> cartList = new ArrayList<>(); // Use the correct 'cart' class name
            if (cartFile.exists() && cartFile.length() > 0) {
                try {
                    cartList = mapper.readValue(cartFile, new TypeReference<List<cart>>() {});
                } catch (IOException e) {
                    System.err.println("Error reading cart.json for removal: " + e.getMessage());
                    // Decide how to handle: proceed without removal, or return error
                    return "redirect:/cart?error=cartReadError";
                }
            }

            boolean cartItemRemoved = false;
            for (cart userCart : cartList) {
                if (userCart.getUsername().equals(user.getUsername()) && userCart.getIteam() != null) {
                   
                    cartItemRemoved = userCart.getIteam().removeIf(item ->
                            item.getProduct().getId().equals(productId) &&
                                    item.getVariantId().equals(variantId) &&
                                    item.getColor().equals(color)
                    );
                    if (cartItemRemoved) break;
                }
            }

            if (cartItemRemoved) {
                mapper.writeValue(cartFile, cartList);
                System.out.println("Successfully removed item from cart.json");
            } else {
                System.err.println("Could not find item in cart.json to remove for user: " + user.getUsername() +
                        " ProductId: " + productId + " VariantId: " + variantId + " Color: " + color);
                
            }

        } catch (IOException e) {
            System.err.println("Error processing purchase: " + e.getMessage());
            e.printStackTrace(); 
            
            return "redirect:/cart?error=purchaseFailed";
        }

       
        return "redirect:/cart?purchaseSuccess=true";
    }
}