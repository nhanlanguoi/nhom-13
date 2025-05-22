package com.project.quanlybanhang.Order;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.project.quanlybanhang.Buy.BuyData;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {

    // Giống UserService, quản lý cả src và target
    private static final String BUY_DATA_RELATIVE_PATH = "static/data-buy/buy.json";
    private final ObjectMapper mapper;
    private final Path srcBuyJsonFilePath;
    private final Path targetBuyJsonFilePath;

    public OrderService() {
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());
        this.mapper.enable(SerializationFeature.INDENT_OUTPUT);
        this.mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // Quan trọng cho LocalDateTime

        String projectDir = System.getProperty("user.dir");
        this.srcBuyJsonFilePath = Paths.get(projectDir, "src", "main", "resources", BUY_DATA_RELATIVE_PATH);
        this.targetBuyJsonFilePath = Paths.get(projectDir, "target", "classes", BUY_DATA_RELATIVE_PATH);
    }

    private void ensureBuyDirectoriesExist() throws IOException {
        Path srcParentDir = srcBuyJsonFilePath.getParent();
        if (srcParentDir != null && !Files.exists(srcParentDir)) {
            Files.createDirectories(srcParentDir);
        }
        Path targetParentDir = targetBuyJsonFilePath.getParent();
        if (targetParentDir != null && !Files.exists(targetParentDir)) {
            Files.createDirectories(targetParentDir);
        }
    }

    public synchronized List<BuyData> getAllOrders() throws IOException {
        ensureBuyDirectoriesExist();
        List<BuyData> orders = null;
        boolean loadedFromSrc = false;

        if (Files.exists(srcBuyJsonFilePath)) {
            try (InputStream srcIs = Files.newInputStream(srcBuyJsonFilePath)) {
                if (srcIs.available() > 0) {
                    orders = mapper.readValue(srcIs, new TypeReference<List<BuyData>>() {});
                    loadedFromSrc = true;
                    // Sync to target if loaded from src
                    if (orders != null) {
                        try (OutputStream targetOs = Files.newOutputStream(targetBuyJsonFilePath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
                            mapper.writeValue(targetOs, orders);
                        } catch (IOException e_sync) {
                            System.err.println("[OrderService] WARNING: Failed to sync buy.json from src to target. Error: " + e_sync.getMessage());
                        }
                    }
                } else {
                    orders = new ArrayList<>();
                    loadedFromSrc = true;
                    // Sync empty list to target
                    try (OutputStream targetOs = Files.newOutputStream(targetBuyJsonFilePath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
                        mapper.writeValue(targetOs, orders);
                    } catch (IOException e_sync) {
                        System.err.println("[OrderService] WARNING: Failed to sync empty buy.json to target. Error: " + e_sync.getMessage());
                    }
                }
            } catch (IOException e_src) {
                // Fall through to try target
            }
        }

        if (!loadedFromSrc) {
            ClassPathResource classpathResource = new ClassPathResource(BUY_DATA_RELATIVE_PATH);
            if (classpathResource.exists()) {
                try (InputStream cpIs = classpathResource.getInputStream()) {
                    if (cpIs.available() > 0) {
                        try (InputStream freshCpIs = new ClassPathResource(BUY_DATA_RELATIVE_PATH).getInputStream()) { // Get fresh stream
                            orders = mapper.readValue(freshCpIs, new TypeReference<List<BuyData>>() {});
                        }
                    } else {
                        orders = new ArrayList<>();
                    }
                } catch (IOException e_cp) {
                    orders = new ArrayList<>();
                }
            } else {
                orders = new ArrayList<>();
            }
        }

        if (orders == null) orders = new ArrayList<>();

        if (!Files.exists(srcBuyJsonFilePath) && !Files.exists(targetBuyJsonFilePath)) {
            saveOrdersToFile(new ArrayList<>(), "initialize empty buy.json");
        }
        return orders;
    }

    private synchronized boolean saveOrdersToFile(List<BuyData> orders, String operation) throws IOException {
        ensureBuyDirectoriesExist();
        // Write to src
        try (OutputStream outputStream = Files.newOutputStream(srcBuyJsonFilePath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            mapper.writeValue(outputStream, orders);
        } catch (IOException e) {
            System.err.println("[OrderService] CRITICAL: Failed to write buy.json to src path: " + srcBuyJsonFilePath.toAbsolutePath() + " - " + e.getMessage());
            throw e; // Re-throw to indicate failure
        }
        // Write to target
        try (OutputStream outputStream = Files.newOutputStream(targetBuyJsonFilePath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            mapper.writeValue(outputStream, orders);
        } catch (IOException e) {
            System.err.println("[OrderService] WARNING: Failed to write buy.json to target path: " + targetBuyJsonFilePath.toAbsolutePath() + " - " + e.getMessage());
            // Don't re-throw, src write was successful which is primary
        }
        return true;
    }

    public synchronized boolean removeOrder(BuyData orderToRemove) throws IOException {
        if (orderToRemove == null) {
            return false;
        }
        List<BuyData> orders = getAllOrders();
        boolean removed = orders.remove(orderToRemove); // Utilizes BuyData.equals()
        if (removed) {
            return saveOrdersToFile(orders, "removeOrder");
        }
        return false;
    }
    public synchronized void addOrder(BuyData order) throws IOException {
        List<BuyData> orders = getAllOrders();
        orders.add(order);
        saveOrdersToFile(orders, "addOrder");
    }


    public synchronized BuyData findBuyData(String username, LocalDateTime orderTimestamp, String productId, String variantId, String color) throws IOException {
        List<BuyData> buyOrders = getAllOrders(); // Giả sử getAllOrders() đã được cập nhật để đọc/ghi src/target
        return buyOrders.stream()
                .filter(order -> order.getUsername().equals(username) &&
                        order.getOrderTimestamp().isEqual(orderTimestamp) && // Dùng isEqual cho LocalDateTime
                        order.getProductId().equals(productId) &&
                        order.getVariantId().equals(variantId) &&
                        order.getColor().equals(color))
                .findFirst().orElse(null);
    }
}