package com.project.quanlybanhang.Product;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service; // << --- THÊM HOẶC ĐẢM BẢO CÓ ANNOTATION NÀY

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service // Đánh dấu đây là một Spring service bean
public class Productservice implements managerdataproduct {

    private static final String PRODUCT_DATA_PATH = "static/data-product/product.json";
    private ObjectMapper mapper = new ObjectMapper();

    public Productservice() {
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    @Override
    public List<Product> getAllProducts() throws IOException {
        System.out.println("[ProductService] Attempting to load products from classpath: " + PRODUCT_DATA_PATH);
        ClassPathResource resource = new ClassPathResource(PRODUCT_DATA_PATH);

        if (!resource.exists()) {
            System.err.println("[ProductService] Product data file not found at classpath: " + PRODUCT_DATA_PATH);
            return new ArrayList<>();
        }

        try (InputStream inputStream = resource.getInputStream()) {
            if (inputStream.available() == 0) {
                System.err.println("[ProductService] Product data file is empty: " + PRODUCT_DATA_PATH);
                return new ArrayList<>();
            }
            try (InputStream freshInputStream = resource.getInputStream()){ // Tạo stream mới để đọc
                List<Product> products = mapper.readValue(freshInputStream, new TypeReference<List<Product>>() {});
                System.out.println("[ProductService] Successfully loaded " + products.size() + " products.");
                return products;
            }
        } catch (IOException e) {
            System.err.println("[ProductService] Error reading product data from classpath: " + PRODUCT_DATA_PATH);
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    public Product getProductById(String id) throws IOException {
        System.out.println("[ProductService] Attempting to get product by ID: " + id);
        List<Product> products = getAllProducts(); // getAllProducts đã có log
        // (Không cần log isEmpty ở đây nữa vì getAllProducts đã xử lý)
        for (Product p : products) {
            if (p.getId().equals(id)) {
                System.out.println("[ProductService] Found product: " + p.getName() + " for ID: " + id);
                return p;
            }
        }
        System.out.println("[ProductService] Product with ID: " + id + " not found.");
        return null;
    }

    // ... (các phương thức khác giữ nguyên như phiên bản có log trước đó) ...
    @Override
    public List<Variant> getProductVariantsById(String productId) throws IOException {
        System.out.println("Attempting to get variants for product ID: " + productId);
        Product product = getProductById(productId);
        if (product != null) {
            System.out.println("Found " + product.getVariants().size() + " variants for product ID: " + productId);
            return product.getVariants();
        }
        System.out.println("No variants found or product ID " + productId + " does not exist.");
        return new ArrayList<>();
    }

    public static long processingprice(long price, int discount){
        long tax = (long)(price*(discount/100.0));
        return price-tax;
    }

    public boolean updateProduct(Product updatedProduct) throws IOException {
        System.out.println("[ProductService] Attempting to update product with ID: " + updatedProduct.getId());
        List<Product> products = getAllProducts();
        boolean found = false;
        for (int i = 0; i < products.size(); i++) {
            if (products.get(i).getId().equals(updatedProduct.getId())) {
                products.set(i, updatedProduct);
                found = true;
                System.out.println("[ProductService] Product with ID: " + updatedProduct.getId() + " found and marked for update.");
                break;
            }
        }

        if (found) {
            try {
                // Cố gắng lấy đường dẫn file từ classpath để ghi (trong target/classes)
                File outputFileOnClasspath = new ClassPathResource(PRODUCT_DATA_PATH).getFile();
                // Kiểm tra xem có thể ghi vào file này không (ví dụ: nếu chạy từ JAR đã đóng gói thì không được)
                if (outputFileOnClasspath.canWrite()) {
                    System.out.println("[ProductService] Attempting to write to classpath (target/classes) path: " + outputFileOnClasspath.getAbsolutePath());
                    mapper.writeValue(outputFileOnClasspath, products);
                    System.out.println("[ProductService] Product data successfully written to: " + outputFileOnClasspath.getAbsolutePath());
                } else {
                    // Nếu không ghi được vào classpath (ví dụ khi chạy từ JAR), thử ghi vào thư mục gốc của dự án (chỉ dùng cho dev)
                    File projectRootFile = new File("src/main/resources/" + PRODUCT_DATA_PATH);
                    if (projectRootFile.getParentFile() != null) {
                        projectRootFile.getParentFile().mkdirs();
                    }
                    System.out.println("[ProductService] WARN: Cannot write to classpath. Attempting to write to development path: " + projectRootFile.getAbsolutePath());
                    mapper.writeValue(projectRootFile, products);
                    System.out.println("[ProductService] Product data successfully written to (development path): " + projectRootFile.getAbsolutePath());
                }

            } catch (IOException e) {
                System.err.println("[ProductService] Could not get file from classpath to write: " + PRODUCT_DATA_PATH + ". Error: " + e.getMessage());
                // Fallback: Thử ghi vào đường dẫn gốc của dự án như một giải pháp cuối cùng cho môi trường dev
                File projectRootFile = new File("src/main/resources/" + PRODUCT_DATA_PATH);
                if (projectRootFile.getParentFile() != null) {
                    projectRootFile.getParentFile().mkdirs();
                }
                System.out.println("[ProductService] Fallback: Attempting to write to development path: " + projectRootFile.getAbsolutePath());
                mapper.writeValue(projectRootFile, products); // Có thể vẫn ném IOException nếu không có quyền
                System.out.println("[ProductService] Product data successfully written to (fallback development path): " + projectRootFile.getAbsolutePath());
            }
        } else {
            System.out.println("[ProductService] Product with ID: " + updatedProduct.getId() + " not found. Update failed.");
        }
        return found;
    }
}