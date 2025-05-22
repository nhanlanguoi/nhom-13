package com.project.quanlybanhang.Cart;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Cartutils {

    private static final String FILE_PATH = "src/main/resources/static/data-cart/cart.json";
    private static final ObjectMapper mapper = new ObjectMapper();

    static {
        
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public static void addToCart(cart newCart, iteams newItem) throws IOException {
        File file = new File(FILE_PATH);
        List<cart> cartList;

        if (!file.exists()) {
            file.getParentFile().mkdirs();
            file.createNewFile();
            cartList = new ArrayList<>();
        } else {
            cartList = mapper.readValue(file, new TypeReference<List<cart>>() {});
        }

        cart existingCart = null;
        for (cart c : cartList) {
            if (c.getUsername().equals(newCart.getUsername())) {
                existingCart = c;
                break;
            }
        }

        if (existingCart != null) {
            boolean found = false;
            for (iteams item : existingCart.getIteam()) {
                if (item.getVariantId().equals(newItem.getVariantId())) {
                    item.setQuantity(item.getQuantity() + 1);
                    found = true;
                    break;
                }
            }

            if (!found) {
                existingCart.getIteam().add(newItem);
            }
        } else {
            List<iteams> items = new ArrayList<>();
            items.add(newItem);
            newCart.setIteam(items);
            newCart.setCreatedatdaytime(LocalDateTime.now());
            cartList.add(newCart);
        }

        mapper.writerWithDefaultPrettyPrinter().writeValue(file, cartList);
    }
}
