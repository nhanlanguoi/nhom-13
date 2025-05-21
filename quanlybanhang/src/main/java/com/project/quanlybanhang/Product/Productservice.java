package com.project.quanlybanhang.Product;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class Productservice implements managerdataproduct {

    // Đường dẫn tương đối của file JSON sản phẩm từ thư mục resources hoặc classes
    private static final String PRODUCT_DATA_RELATIVE_PATH = "static/data-product/product.json";
    // Đường dẫn tương đối của thư mục chứa ảnh sản phẩm từ thư mục static
    private static final String UPLOAD_DIR_RELATIVE_TO_STATIC = "data-product/image/";


    private final ObjectMapper mapper;
    private final Path srcJsonFilePath; // Đường dẫn đến file product.json trong src/main/resources
    private final Path targetJsonFilePath; // Đường dẫn đến file product.json trong target/classes

    public Productservice() {
        this.mapper = new ObjectMapper();
        this.mapper.enable(SerializationFeature.INDENT_OUTPUT); // Để file JSON dễ đọc

        String projectDir = System.getProperty("user.dir");
        this.srcJsonFilePath = Paths.get(projectDir, "src", "main", "resources", PRODUCT_DATA_RELATIVE_PATH);
        this.targetJsonFilePath = Paths.get(projectDir, "target", "classes", PRODUCT_DATA_RELATIVE_PATH);
    }

    /**
     * Đảm bảo các thư mục cha cho file JSON sản phẩm tồn tại.
     * @throws IOException Nếu có lỗi khi tạo thư mục.
     */
    private void ensureBaseDirectoriesExist() throws IOException {
        Path srcParentDir = srcJsonFilePath.getParent();
        if (srcParentDir != null && !Files.exists(srcParentDir)) {
            Files.createDirectories(srcParentDir);
            System.out.println("[ProductService] Created directory: " + srcParentDir.toAbsolutePath());
        }
        Path targetParentDir = targetJsonFilePath.getParent();
        if (targetParentDir != null && !Files.exists(targetParentDir)) {
            Files.createDirectories(targetParentDir);
            System.out.println("[ProductService] Created directory: " + targetParentDir.toAbsolutePath());
        }
    }

    /**
     * Tải danh sách tất cả sản phẩm.
     * Ưu tiên đọc từ target/classes (classpath), nếu không có hoặc rỗng thì đọc từ src/main/resources.
     * Nếu src có dữ liệu và target rỗng/không có, đồng bộ từ src sang target.
     * @return Danh sách sản phẩm.
     * @throws IOException Nếu có lỗi đọc file.
     */
    @Override
    public List<Product> getAllProducts() throws IOException {
        System.out.println("[ProductService] Attempting to load products.");
        ensureBaseDirectoriesExist();

        ClassPathResource classpathResource = new ClassPathResource(PRODUCT_DATA_RELATIVE_PATH);

        if (classpathResource.exists()) {
            try (InputStream cpIs = classpathResource.getInputStream()) {
                if (cpIs.available() > 0) {
                    // Đọc từ classpath (target/classes) nếu có dữ liệu
                    try(InputStream freshCpIs = new ClassPathResource(PRODUCT_DATA_RELATIVE_PATH).getInputStream()) {
                        List<Product> products = mapper.readValue(freshCpIs, new TypeReference<List<Product>>() {});
                        System.out.println("[ProductService] Successfully loaded " + products.size() + " products from classpath: " + targetJsonFilePath.toAbsolutePath());
                        return products;
                    }
                } else {
                    System.out.println("[ProductService] Product data file in classpath is empty: " + targetJsonFilePath.toAbsolutePath());
                    // File target rỗng, thử đọc từ src và đồng bộ nếu src có dữ liệu
                    return loadFromSrcAndSyncToTargetIfNecessary();
                }
            } catch (IOException e) {
                System.err.println("[ProductService] Error reading from classpath, attempting to load from src. Error: " + e.getMessage());
                return loadFromSrcAndSyncToTargetIfNecessary();
            }
        } else {
            System.out.println("[ProductService] Product data file not found in classpath: " + targetJsonFilePath.toAbsolutePath());
            // File target không tồn tại, thử đọc từ src và đồng bộ
            return loadFromSrcAndSyncToTargetIfNecessary();
        }
    }

    /**
     * Helper method để đọc từ src/main/resources và đồng bộ sang target nếu cần.
     */
    private List<Product> loadFromSrcAndSyncToTargetIfNecessary() throws IOException {
        if (Files.exists(srcJsonFilePath)) {
            try (InputStream srcIs = Files.newInputStream(srcJsonFilePath)) {
                if (srcIs.available() > 0) {
                    List<Product> products = mapper.readValue(srcIs, new TypeReference<List<Product>>() {});
                    System.out.println("[ProductService] Successfully loaded " + products.size() + " products from src: " + srcJsonFilePath.toAbsolutePath());
                    // Đồng bộ sang target vì target bị thiếu hoặc rỗng
                    try {
                        Files.copy(srcJsonFilePath, targetJsonFilePath, StandardCopyOption.REPLACE_EXISTING);
                        System.out.println("[ProductService] Synced product data from src to target: " + targetJsonFilePath.toAbsolutePath());
                    } catch (IOException e_sync) {
                        System.err.println("[ProductService] WARNING: Failed to sync product data from src to target. Error: " + e_sync.getMessage());
                    }
                    return products;
                } else {
                    System.out.println("[ProductService] Product data file in src is empty: " + srcJsonFilePath.toAbsolutePath());
                    return new ArrayList<>();
                }
            } catch (IOException e_src) {
                System.err.println("[ProductService] Error reading product data from src path: " + srcJsonFilePath.toAbsolutePath() + ". Error: " + e_src.getMessage());
                throw e_src; // Ném lỗi nếu không đọc được từ src
            }
        } else {
            System.out.println("[ProductService] Product data file not found in src: " + srcJsonFilePath.toAbsolutePath() + ". Returning empty list.");
            return new ArrayList<>();
        }
    }


    /**
     * Lưu danh sách sản phẩm vào file.
     * Ghi vào src/main/resources trước, sau đó ghi vào target/classes.
     * @param products Danh sách sản phẩm cần lưu.
     * @param operation Mô tả thao tác (ví dụ: addProduct, updateProduct).
     * @return true nếu lưu thành công vào src.
     * @throws IOException Nếu có lỗi khi ghi file vào src.
     */
    private boolean saveProductsToFile(List<Product> products, String operation) throws IOException {
        String actionLog = operation != null ? " (" + operation + ")" : "";
        System.out.println("[ProductService] Attempting to save products to file" + actionLog + ".");
        ensureBaseDirectoriesExist();

        // 1. Ghi vào src/main/resources (source of truth trong dev)
        try (OutputStream outputStream = Files.newOutputStream(srcJsonFilePath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            mapper.writeValue(outputStream, products);
            System.out.println("[ProductService] Product data successfully written to src path: " + srcJsonFilePath.toAbsolutePath());
        } catch (IOException e) {
            System.err.println("[ProductService] CRITICAL: Failed to write product data to src path: " + srcJsonFilePath.toAbsolutePath() + " - " + e.getMessage());
            e.printStackTrace();
            throw e; // Ném lại lỗi nếu không ghi được vào src
        }

        // 2. Ghi vào target/classes (để thay đổi có hiệu lực ngay khi dev mà không cần build lại)
        try (OutputStream outputStream = Files.newOutputStream(targetJsonFilePath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            mapper.writeValue(outputStream, products);
            System.out.println("[ProductService] Product data successfully written to target path: " + targetJsonFilePath.toAbsolutePath());
        } catch (IOException e) {
            System.err.println("[ProductService] WARNING: Failed to write product data to target path: " + targetJsonFilePath.toAbsolutePath() + " - " + e.getMessage());
            // Không ném lỗi ở đây, vì việc ghi vào src đã thành công.
        }
        return true;
    }

    /**
     * Thêm một sản phẩm mới.
     * @param newProduct Sản phẩm mới.
     * @return true nếu thêm thành công.
     * @throws IOException Nếu có lỗi đọc/ghi file.
     */
    @Override
    public boolean addProduct(Product newProduct) throws IOException {
        System.out.println("[ProductService] Attempting to add a new product: " + newProduct.getName());
        List<Product> products = getAllProducts();

        if (newProduct.getId() == null || newProduct.getId().trim().isEmpty()) {
            newProduct.setId(UUID.randomUUID().toString());
            System.out.println("[ProductService] Generated UUID for new product: " + newProduct.getId());
        }

        if (newProduct.getVariants() != null) {
            for (Variant variant : newProduct.getVariants()) {
                if (variant.getId() == null || variant.getId().trim().isEmpty()) {
                    variant.setId(newProduct.getId() + "-variant-" + UUID.randomUUID().toString().substring(0, 4));
                    System.out.println("[ProductService] Generated UUID for new variant: " + variant.getId());
                }
                if (variant.getColorprices() != null) {
                    int maxCpId = variant.getColorprices().stream()
                            .mapToInt(colorprice::getId)
                            .filter(id -> id > 0) // Chỉ xét ID hợp lệ
                            .max().orElse(0);
                    for (colorprice cp : variant.getColorprices()) {
                        if (cp.getId() <= 0) { // ID <= 0 (hoặc 0 như bạn dùng) nghĩa là mới
                            maxCpId++;
                            cp.setId(maxCpId);
                            System.out.println("[ProductService] Generated int ID for new colorprice: " + cp.getId());
                        }
                    }
                } else {
                    variant.setColorprices(new ArrayList<>());
                }
            }
        } else {
            newProduct.setVariants(new ArrayList<>());
        }

        if (newProduct.getTags() == null) {
            newProduct.setTags(new ArrayList<>());
        } else { // Đảm bảo tags là list các string không rỗng
            newProduct.setTags(newProduct.getTags().stream().map(String::trim).filter(tag -> !tag.isEmpty()).collect(Collectors.toList()));
        }


        products.add(newProduct);
        boolean success = saveProductsToFile(products, "addProduct");
        if (success) {
            System.out.println("[ProductService] New product '" + newProduct.getName() + "' added successfully. Total products: " + products.size());
        }
        return success;
    }

    @Override
    public Product getProductById(String id) throws IOException {
        System.out.println("[ProductService] Attempting to get product by ID: " + id);
        List<Product> products = getAllProducts();
        for (Product p : products) {
            if (p.getId().equals(id)) {
                System.out.println("[ProductService] Found product: " + p.getName() + " for ID: " + id);
                return p;
            }
        }
        System.out.println("[ProductService] Product with ID: " + id + " not found.");
        return null;
    }

    @Override
    public List<Variant> getProductVariantsById(String productId) throws IOException {
        System.out.println("[ProductService] Attempting to get variants for product ID: " + productId);
        Product product = getProductById(productId);
        if (product != null && product.getVariants() != null) {
            System.out.println("[ProductService] Found " + product.getVariants().size() + " variants for product ID: " + productId);
            return product.getVariants();
        }
        System.out.println("[ProductService] No variants found or product ID " + productId + " does not exist/has no variants.");
        return new ArrayList<>();
    }

    public static long processingprice(long price, int discount) {
        if (discount < 0) discount = 0;
        if (discount > 100) discount = 100;
        double discountRate = discount / 100.0;
        long discountAmount = (long) (price * discountRate);
        return price - discountAmount;
    }

    /**
     * Cập nhật thông tin một sản phẩm.
     * @param updatedProduct Sản phẩm với thông tin đã cập nhật.
     * @return true nếu cập nhật thành công.
     * @throws IOException Nếu có lỗi đọc/ghi file.
     */
    @Override
    public boolean updateProduct(Product updatedProduct) throws IOException {
        System.out.println("[ProductService] Attempting to update product with ID: " + updatedProduct.getId());
        if (updatedProduct.getId() == null || updatedProduct.getId().trim().isEmpty()) {
            System.err.println("[ProductService] Cannot update product with null or empty ID.");
            return false;
        }
        List<Product> products = getAllProducts();
        boolean found = false;
        for (int i = 0; i < products.size(); i++) {
            if (products.get(i).getId().equals(updatedProduct.getId())) {
                // Xử lý ID cho variants và colorprices mới (nếu có) trong sản phẩm cập nhật
                if (updatedProduct.getVariants() != null) {
                    for (Variant variant : updatedProduct.getVariants()) {
                        if (variant.getId() == null || variant.getId().trim().isEmpty()) {
                            variant.setId(updatedProduct.getId() + "-variant-" + UUID.randomUUID().toString().substring(0, 4));
                            System.out.println("[ProductService] Generated UUID for new variant during update: " + variant.getId());
                        }
                        if (variant.getColorprices() != null) {
                            int maxCpId = variant.getColorprices().stream()
                                    .mapToInt(colorprice::getId)
                                    .filter(idVal -> idVal > 0)
                                    .max().orElse(0);
                            for (colorprice cp : variant.getColorprices()) {
                                if (cp.getId() <= 0) { // ID mới
                                    maxCpId++;
                                    cp.setId(maxCpId);
                                    System.out.println("[ProductService] Generated int ID for new colorprice during update: " + cp.getId());
                                }
                            }
                        } else {
                            variant.setColorprices(new ArrayList<>());
                        }
                    }
                } else {
                    updatedProduct.setVariants(new ArrayList<>());
                }
                if (updatedProduct.getTags() == null) {
                    updatedProduct.setTags(new ArrayList<>());
                } else {
                    updatedProduct.setTags(updatedProduct.getTags().stream().map(String::trim).filter(tag -> !tag.isEmpty()).collect(Collectors.toList()));
                }

                products.set(i, updatedProduct);
                found = true;
                System.out.println("[ProductService] Product with ID: " + updatedProduct.getId() + " found and marked for update.");
                break;
            }
        }

        if (found) {
            return saveProductsToFile(products, "updateProduct");
        } else {
            System.out.println("[ProductService] Product with ID: " + updatedProduct.getId() + " not found. Update failed.");
            return false;
        }
    }

    /**
     * Xóa một sản phẩm dựa trên ID. Đồng thời xóa các ảnh liên quan.
     * @param productId ID của sản phẩm cần xóa.
     * @return true nếu xóa thành công.
     * @throws IOException Nếu có lỗi đọc/ghi file.
     */
    @Override
    public boolean deleteProduct(String productId) throws IOException {
        System.out.println("[ProductService] Attempting to delete product with ID: " + productId);
        if (productId == null || productId.trim().isEmpty()) {
            System.err.println("[ProductService] Cannot delete product with null or empty ID.");
            return false;
        }
        List<Product> products = getAllProducts();
        Product productToDelete = products.stream()
                .filter(p -> p.getId().equals(productId))
                .findFirst()
                .orElse(null);

        if (productToDelete != null) {
            // Xóa các file ảnh liên quan trước
            if (productToDelete.getVariants() != null) {
                for (Variant variant : productToDelete.getVariants()) {
                    if (variant.getImage() != null && !variant.getImage().isEmpty()) {
                        deleteImageFile(variant.getImage());
                    }
                }
            }
            // Xóa sản phẩm khỏi danh sách
            products.remove(productToDelete);
            System.out.println("[ProductService] Product with ID: " + productId + " removed from list.");
            return saveProductsToFile(products, "deleteProduct");
        } else {
            System.out.println("[ProductService] Product with ID: " + productId + " not found. Deletion failed.");
            return false;
        }
    }

    /**
     * Xóa file ảnh từ cả thư mục src và target.
     * @param imageWebPath Đường dẫn web của ảnh (ví dụ: /data-product/image/ten_file.jpg).
     */
    private void deleteImageFile(String imageWebPath) {
        if (imageWebPath == null || imageWebPath.trim().isEmpty()) {
            System.err.println("[ProductService] Invalid image web path for deletion: (null or empty)");
            return;
        }

        // imageWebPath ví dụ: /data-product/image/filename.jpg
        // Cần lấy tên file: filename.jpg
        // Và ghép với thư mục gốc chứa ảnh trong static: data-product/image/
        String filename;
        if (imageWebPath.startsWith("/" + UPLOAD_DIR_RELATIVE_TO_STATIC)) {
            filename = imageWebPath.substring(("/" + UPLOAD_DIR_RELATIVE_TO_STATIC).length());
        } else if (imageWebPath.startsWith(UPLOAD_DIR_RELATIVE_TO_STATIC)) { // Trường hợp không có / đầu
            filename = imageWebPath.substring(UPLOAD_DIR_RELATIVE_TO_STATIC.length());
        }
        else if (imageWebPath.contains("/")) { // Lấy phần cuối sau dấu /
            filename = imageWebPath.substring(imageWebPath.lastIndexOf("/") + 1);
        }
        else { // Coi như là tên file
            filename = imageWebPath;
        }

        if (filename.isEmpty()){
            System.err.println("[ProductService] Could not extract filename from image web path: " + imageWebPath);
            return;
        }


        try {
            String projectDir = System.getProperty("user.dir");
            // Đường dẫn tương đối từ gốc static là UPLOAD_DIR_RELATIVE_TO_STATIC + filename
            String relativePathFromStaticRoot = UPLOAD_DIR_RELATIVE_TO_STATIC + filename;

            Path pathToDeleteInSrc = Paths.get(projectDir, "src", "main", "resources", "static", relativePathFromStaticRoot).normalize();
            Path pathToDeleteInTarget = Paths.get(projectDir, "target", "classes", "static", relativePathFromStaticRoot).normalize();

            // Kiểm tra file có nằm trong thư mục upload dự kiến không để tránh xóa nhầm
            Path baseSrcUploadDir = Paths.get(projectDir, "src", "main", "resources", "static", UPLOAD_DIR_RELATIVE_TO_STATIC).normalize();
            Path baseTargetUploadDir = Paths.get(projectDir, "target", "classes", "static", UPLOAD_DIR_RELATIVE_TO_STATIC).normalize();


            if (pathToDeleteInSrc.startsWith(baseSrcUploadDir)) {
                boolean deletedFromSrc = Files.deleteIfExists(pathToDeleteInSrc);
                if (deletedFromSrc) {
                    System.out.println("[ProductService] Deleted image file from src: " + pathToDeleteInSrc.toAbsolutePath());
                } else {
                    System.out.println("[ProductService] Image file not found in src or could not be deleted: " + pathToDeleteInSrc.toAbsolutePath());
                }
            } else {
                System.err.println("[ProductService] SKIPPED Deletion: Image path in src is outside expected upload directory: " + pathToDeleteInSrc.toAbsolutePath());
            }


            if(pathToDeleteInTarget.startsWith(baseTargetUploadDir)){
                boolean deletedFromTarget = Files.deleteIfExists(pathToDeleteInTarget);
                if (deletedFromTarget) {
                    System.out.println("[ProductService] Deleted image file from target: " + pathToDeleteInTarget.toAbsolutePath());
                } else {
                    System.out.println("[ProductService] Image file not found in target or could not be deleted: " + pathToDeleteInTarget.toAbsolutePath());
                }
            } else {
                System.err.println("[ProductService] SKIPPED Deletion: Image path in target is outside expected upload directory: " + pathToDeleteInTarget.toAbsolutePath());
            }


        } catch (IOException e) {
            System.err.println("[ProductService] Error deleting image file for web path '" + imageWebPath + "' (filename: "+filename+"): " + e.getMessage());
        } catch (Exception e) {
            System.err.println("[ProductService] Unexpected error deleting image file for web path '" + imageWebPath + "' (filename: "+filename+"): " + e.getMessage());
            e.printStackTrace();
        }
    }
}