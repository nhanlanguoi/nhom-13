package com.project.quanlybanhang.Order; // Hoặc com.project.quanlybanhang.Buy

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.project.quanlybanhang.Buy.BuyData; // Import class BuyData
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream; // Cần cho việc tạo file rỗng nếu chưa có
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption; // Cần cho việc tạo file rỗng
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {

    private static final String ORDER_DATA_RELATIVE_PATH = "static/data-buy/buy.json";
    private final ObjectMapper mapper;
    private final Path srcOrderJsonFilePath;
    private final Path targetOrderJsonFilePath;

    public OrderService() {
        this.mapper = new ObjectMapper();
        this.mapper.enable(SerializationFeature.INDENT_OUTPUT);
        this.mapper.registerModule(new JavaTimeModule()); // Rất quan trọng cho LocalDateTime trong BuyData

        String projectDir = System.getProperty("user.dir");
        this.srcOrderJsonFilePath = Paths.get(projectDir, "src", "main", "resources", ORDER_DATA_RELATIVE_PATH);
        this.targetOrderJsonFilePath = Paths.get(projectDir, "target", "classes", ORDER_DATA_RELATIVE_PATH);
    }

    private void ensureOrderDirectoriesExist() throws IOException {
        Path srcParentDir = srcOrderJsonFilePath.getParent();
        if (srcParentDir != null && !Files.exists(srcParentDir)) {
            Files.createDirectories(srcParentDir);
            System.out.println("[OrderService] Created directory: " + srcParentDir.toAbsolutePath());
        }
        Path targetParentDir = targetOrderJsonFilePath.getParent();
        if (targetParentDir != null && !Files.exists(targetParentDir)) {
            Files.createDirectories(targetParentDir);
            System.out.println("[OrderService] Created directory: " + targetParentDir.toAbsolutePath());
        }
    }

    public List<BuyData> getAllOrders() throws IOException {
        System.out.println("[OrderService] Attempting to load orders, prioritizing src.");
        ensureOrderDirectoriesExist();
        List<BuyData> orders = null;
        boolean loadedFromSrc = false;

        if (Files.exists(srcOrderJsonFilePath)) {
            try (InputStream srcIs = Files.newInputStream(srcOrderJsonFilePath)) {
                if (srcIs.available() > 0) {

                    orders = mapper.readValue(srcIs, new TypeReference<List<BuyData>>() {});
                    System.out.println("[OrderService] Successfully loaded " + (orders != null ? orders.size() : 0) + " orders from src: " + srcOrderJsonFilePath.toAbsolutePath());
                    loadedFromSrc = true;

                    if (orders != null) {
                        try (OutputStream targetOs = Files.newOutputStream(targetOrderJsonFilePath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
                            mapper.writeValue(targetOs, orders);
                            System.out.println("[OrderService] Synced order data from src to target: " + targetOrderJsonFilePath.toAbsolutePath());
                        }
                    }
                } else {
                    System.out.println("[OrderService] Order data file in src is empty: " + srcOrderJsonFilePath.toAbsolutePath());
                    orders = new ArrayList<>();
                    loadedFromSrc = true;
                    try (OutputStream targetOs = Files.newOutputStream(targetOrderJsonFilePath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
                        mapper.writeValue(targetOs, orders); // Sync list rỗng
                        System.out.println("[OrderService] Synced empty order list from src to target: " + targetOrderJsonFilePath.toAbsolutePath());
                    }
                }
            } catch (IOException e_src) {
                System.err.println("[OrderService] Error reading order data from src path: " + srcOrderJsonFilePath.toAbsolutePath() + ". Error: " + e_src.getMessage());
                // Sẽ thử đọc từ target
            }
        } else {
            System.out.println("[OrderService] Order data file not found in src: " + srcOrderJsonFilePath.toAbsolutePath());
        }

        if (!loadedFromSrc) {
            System.out.println("[OrderService] Could not load from src or src was missing/empty. Attempting to load from target/classpath.");
            ClassPathResource classpathResource = new ClassPathResource(ORDER_DATA_RELATIVE_PATH);
            if (classpathResource.exists()) {
                try (InputStream cpIs = classpathResource.getInputStream()) {
                    if (cpIs.available() > 0) {
                        try (InputStream freshCpIs = new ClassPathResource(ORDER_DATA_RELATIVE_PATH).getInputStream()) {
                            orders = mapper.readValue(freshCpIs, new TypeReference<List<BuyData>>() {});
                            System.out.println("[OrderService] Successfully loaded " + (orders != null ? orders.size() : 0) + " orders from classpath (target): " + targetOrderJsonFilePath.toAbsolutePath());
                        }
                    } else {
                        System.out.println("[OrderService] Order data file in classpath (target) is empty: " + targetOrderJsonFilePath.toAbsolutePath());
                        orders = new ArrayList<>();
                    }
                } catch (IOException e_cp) {
                    System.err.println("[OrderService] Error reading order data from classpath (target): " + targetOrderJsonFilePath.toAbsolutePath() + ". Error: " + e_cp.getMessage());
                    orders = new ArrayList<>();
                }
            } else {
                System.out.println("[OrderService] Order data file also not found in classpath (target).");
                orders = new ArrayList<>();
            }
        }

        if (orders == null) orders = new ArrayList<>();

        // Nếu cả src và target đều không có file, tạo file rỗng ở cả 2 nơi
        if (!Files.exists(srcOrderJsonFilePath) && !Files.exists(targetOrderJsonFilePath)) {
            System.out.println("[OrderService] Neither src nor target order file exists. Initializing empty files.");
            saveOrdersToFile(new ArrayList<>(), "initialize empty order file"); // Hàm này cần được tạo
        }
        return orders;
    }

    // Hàm saveOrdersToFile (nếu admin cần sửa trạng thái đơn hàng sau này)
    // Hiện tại chỉ cần đọc, nhưng nên có sẵn
    public boolean saveOrdersToFile(List<BuyData> orders, String operation) throws IOException {
        String actionLog = operation != null ? "(" + operation + ")" : "";
        System.out.println("[OrderService] Attempting to save orders to file" + actionLog + ".");
        ensureOrderDirectoriesExist();

        try (OutputStream outputStream = Files.newOutputStream(srcOrderJsonFilePath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            mapper.writeValue(outputStream, orders);
            System.out.println("[OrderService] Order data successfully written to src path: " + srcOrderJsonFilePath.toAbsolutePath());
        } catch (IOException e) {
            System.err.println("[OrderService] CRITICAL: Failed to write order data to src path: " + srcOrderJsonFilePath.toAbsolutePath() + " - " + e.getMessage());
            e.printStackTrace();
            throw e;
        }

        try (OutputStream outputStream = Files.newOutputStream(targetOrderJsonFilePath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            mapper.writeValue(outputStream, orders);
            System.out.println("[OrderService] Order data successfully written to target path: " + targetOrderJsonFilePath.toAbsolutePath());
        } catch (IOException e) {
            System.err.println("[OrderService] WARNING: Failed to write order data to target path: " + targetOrderJsonFilePath.toAbsolutePath() + " - " + e.getMessage());
        }
        return true;
    }
}