// src/main/java/com/project/quanlybanhang/SalesHistory/RevenueService.java
package com.project.quanlybanhang.SalesHistory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.project.quanlybanhang.Product.Product;
import com.project.quanlybanhang.Product.Productservice;
import com.project.quanlybanhang.Product.Variant;
import com.project.quanlybanhang.Product.colorprice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Month;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RevenueService {

    private static final String HISTORY_DATA_RELATIVE_PATH = "static/lichsugiaodich/history.json";
    private final Path srcHistoryJsonFilePath;
    private final Path targetHistoryJsonFilePath;
    private final ObjectMapper mapper;
    private final Productservice productService; // Inject Productservice

    @Autowired
    public RevenueService(Productservice productService) {
        this.productService = productService;
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());
        this.mapper.enable(SerializationFeature.INDENT_OUTPUT);
        this.mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        String projectDir = System.getProperty("user.dir");
        this.srcHistoryJsonFilePath = Paths.get(projectDir, "src", "main", "resources", HISTORY_DATA_RELATIVE_PATH);
        this.targetHistoryJsonFilePath = Paths.get(projectDir, "target", "classes", HISTORY_DATA_RELATIVE_PATH);

        try {
            ensureHistoryDirectoriesExist();
            if (!Files.exists(srcHistoryJsonFilePath)) {
                Files.writeString(srcHistoryJsonFilePath, "[]");
            }
            if (Files.exists(srcHistoryJsonFilePath.getParent()) && !Files.exists(targetHistoryJsonFilePath.getParent())) {
                Files.createDirectories(targetHistoryJsonFilePath.getParent());
            }
            if (!Files.exists(targetHistoryJsonFilePath) && Files.exists(targetHistoryJsonFilePath.getParent())) {
                Files.writeString(targetHistoryJsonFilePath, "[]");
            }
        } catch (IOException e) {
            System.err.println("[RevenueService] Error initializing history.json: " + e.getMessage());
        }
    }

    private void ensureHistoryDirectoriesExist() throws IOException {
        Path srcParentDir = srcHistoryJsonFilePath.getParent();
        if (srcParentDir != null && !Files.exists(srcParentDir)) {
            Files.createDirectories(srcParentDir);
        }
        Path targetParentDir = targetHistoryJsonFilePath.getParent();
        if (targetParentDir != null && !Files.exists(targetParentDir)) {
            Files.createDirectories(targetParentDir);
        }
    }

    public List<HistoryEntry> loadHistoryEntries() throws IOException {
        ensureHistoryDirectoriesExist();
        Path pathToRead = Files.exists(srcHistoryJsonFilePath) ? srcHistoryJsonFilePath : targetHistoryJsonFilePath;

        // DEBUGGING: In ra đường dẫn tuyệt đối đang được sử dụng
        System.out.println("[RevenueService] Attempting to read history from: " + pathToRead.toAbsolutePath().toString());

        if (!Files.exists(pathToRead)) {
            System.out.println("[RevenueService] History file does NOT exist at: " + pathToRead.toAbsolutePath().toString());
            // Cố gắng tạo file nếu nó không tồn tại ở cả src và target
            if (!Files.exists(srcHistoryJsonFilePath)) Files.writeString(srcHistoryJsonFilePath, "[]");
            if (!Files.exists(targetHistoryJsonFilePath) && Files.exists(targetHistoryJsonFilePath.getParent())) Files.writeString(targetHistoryJsonFilePath, "[]");
            return new ArrayList<>();
        } else {
            // DEBUGGING: In ra kích thước file
            System.out.println("[RevenueService] History file exists. Size: " + Files.size(pathToRead) + " bytes.");
            if (Files.size(pathToRead) == 0) {
                System.out.println("[RevenueService] History file is EMPTY (0 bytes).");
                return new ArrayList<>();
            }
        }

        try (InputStream is = Files.newInputStream(pathToRead)) {
            List<HistoryEntry> entries = mapper.readValue(is, new TypeReference<List<HistoryEntry>>() {});
            // DEBUGGING: In ra số lượng entry đọc được
            System.out.println("[RevenueService] Loaded " + entries.size() + " history entries.");
            return entries;
        } catch (IOException e) {
            System.err.println("[RevenueService] IOException while reading history file " + pathToRead.toAbsolutePath().toString() + ": " + e.getMessage());
            throw e; // Ném lại lỗi để controller có thể xử lý
        }
    }

    public List<HistoryEntry> getProcessedHistoryWithRevenue() throws IOException {
        List<HistoryEntry> historyEntries = loadHistoryEntries(); // Tải dữ liệu từ history.json
        List<Product> allProducts = productService.getAllProducts(); // Tải dữ liệu từ product.json

        for (HistoryEntry entry : historyEntries) {
            if (entry.getOrderData() != null) {
                String productIdFromHistory = entry.getOrderData().getProductId();
                String variantIdStringFromHistory = entry.getOrderData().getVariantId();
                String colorNameFromHistory = entry.getOrderData().getColor();
                int quantity = entry.getOrderData().getQuantity();
                double itemPrice = 0;

               
                String storageCapacity = "";
                if (variantIdStringFromHistory != null) {
                    String[] parts = variantIdStringFromHistory.split("-");
                    for (String part : parts) { // Duyệt qua các phần để tìm thông tin dung lượng
                        if (part.toLowerCase().contains("gb") || part.toLowerCase().contains("tb")) {
                            storageCapacity = part.toLowerCase(); // Chuẩn hóa về chữ thường, ví dụ: "128gb"
                            break;
                        }
                    }
                }

               
                Product foundProduct = allProducts.stream()
                        .filter(p -> p.getId().equalsIgnoreCase(productIdFromHistory))
                        .findFirst().orElse(null);

                if (foundProduct != null) {
                   
                    for (Variant variant : foundProduct.getVariants()) {
                        boolean capacityMatch;
                       
                        if (!storageCapacity.isEmpty()) {
                           
                            capacityMatch = variant.getMemory() != null &&
                                    variant.getMemory().equalsIgnoreCase(storageCapacity);
                        } else {
                            
                            capacityMatch = true;
                        }

                        
                        if (capacityMatch) {
                           
                            for (colorprice cp : variant.getColorprices()) {
                              
                                if (cp.getColor().equalsIgnoreCase(colorNameFromHistory)) {
                                    try {
                                        
                                        itemPrice = Double.parseDouble(cp.getPrice().replaceAll("[^\\d.]", ""));
                                    } catch (NumberFormatException e) {
                                        System.err.println("Lỗi parse giá tiền: " + cp.getPrice() +
                                                " cho sản phẩm ID " + productIdFromHistory +
                                                ", màu: " + colorNameFromHistory);
                                        itemPrice = 0;
                                    }
                                    break; 
                                }
                            }
                        }
                        
                        if (itemPrice > 0) {
                            break;
                        }
                    }
                }

              
                if (itemPrice == 0) {
                    System.err.println("Không tìm thấy giá cho sản phẩm: " + productIdFromHistory +
                            ", variantId: " + variantIdStringFromHistory +
                            ", màu: " + colorNameFromHistory +
                            ", storageCapacity parsed: " + storageCapacity);
                }
                entry.setTotalAmount(itemPrice * quantity);
            }
        }
        return historyEntries;
    }

    public Map<String, Double> getMonthlyRevenue(List<HistoryEntry> processedHistory, int year) {
        return processedHistory.stream()
                .filter(entry -> entry.getCompletedTimestamp() != null && entry.getCompletedTimestamp().getYear() == year)
                .collect(Collectors.groupingBy(
                        entry -> entry.getCompletedTimestamp().getMonth().toString(), 
                        LinkedHashMap::new, 
                        Collectors.summingDouble(HistoryEntry::getTotalAmount)
                ));
    }

    public double getTotalRevenue(List<HistoryEntry> processedHistory) {
        return processedHistory.stream()
                .mapToDouble(HistoryEntry::getTotalAmount)
                .sum();
    }
}