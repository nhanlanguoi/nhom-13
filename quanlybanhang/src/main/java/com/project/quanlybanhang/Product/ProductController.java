package com.project.quanlybanhang.Product;

import java.io.IOException;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import static com.project.quanlybanhang.Product.Productservice.processingprice;
import static com.project.quanlybanhang.Utils.Numberutils.parseSafeLongadddot;

@Controller
public class ProductController {

    Productservice productService = new Productservice();

    @GetMapping("/product/{productid}/{id}")
    public String product(
        @PathVariable String productid, @PathVariable String id,  @RequestParam int colorid,
            Model model) throws IOException {

        Product product = productService.getProductById(productid);
        System.out.println("Received productid: " + productid);
        System.out.println("Received id: " + id);


        List<Variant> variantList = product.getVariants();

        Variant foundvariant = null;

        for (Variant variant : variantList){
            if(variant.getId().equals(id)){
                foundvariant = variant;
            }
        }



        List<colorprice> colorpriceList = foundvariant.getColorprices();
        colorprice foundcolorprice =null;
        for (colorprice colorprice : colorpriceList) {
            if (colorprice.getId() == colorid) {
                foundcolorprice = colorprice;
            }
        }

        


        
        model.addAttribute("colorprice" ,foundcolorprice);
        model.addAttribute("variants" , variantList);
        model.addAttribute("variant" , foundvariant);
        model.addAttribute("product" , product);
        model.addAttribute("colorprices", colorpriceList);
        model.addAttribute("colorprice", foundcolorprice);

        


        String price = foundcolorprice.getPrice();
        Long pricetopare = processingprice(Long.parseLong(price),foundcolorprice.getDiscount());
        price = parseSafeLongadddot(price);

        model.addAttribute("price" , price);


        model.addAttribute("pricetopare", parseSafeLongadddot(Long.toString(pricetopare)));
        
        String category = product.getCategory();
        String description = product.getDescription();
        if ("Phụ kiện".equals(category)) {
            if ("Tai nghe".equals(description)) {
                return "html/product-phukien-tainghe";
            }
            if ("Chuột máy tính".equals(description)) {
                return "html/product-phukien-chuotmaytinh";
            }
            if ("Bàn phím".equals(description)) {
                return "html/product-phukien-banphim";
            }
        }



        return "html/product";
    }

}