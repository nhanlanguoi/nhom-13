package com.project.quanlybanhang.Employee;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.project.quanlybanhang.Buy.BuyData;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmployeeService {

    private static final String BUY_DATA_PATH = "src/main/resources/static/data-buy/buy.json";
    private static final String DANG_GIAO_DATA_PATH = "src/main/resources/static/dang-giao/danggiao.json";
    private static final String HISTORY_DATA_PATH = "src/main/resources/static/lichsugiaodich/history.json";

    private final ObjectMapper mapper;

    public EmployeeService() {
        this.mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    private <T> List<T> readJsonFile(String filePath, TypeReference<List<T>> typeReference) throws IOException {
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            Files.createDirectories(path.getParent());
            Files.writeString(path, "[]", StandardOpenOption.CREATE);
            return new ArrayList<>();
        }
        if (Files.size(path) == 0) {
            return new ArrayList<>();
        }
        return mapper.readValue(path.toFile(), typeReference);
    }

    private <T> void writeJsonFile(String filePath, List<T> data) throws IOException {
        Path path = Paths.get(filePath);
        if (!Files.exists(path.getParent())) {
            Files.createDirectories(path.getParent());
        }
        mapper.writeValue(path.toFile(), data);
    }

    public List<BuyData> getAllBuyOrders() throws IOException {
        return readJsonFile(BUY_DATA_PATH, new TypeReference<>() {});
    }

    public List<DangGiaoData> getAllDangGiaoOrders() throws IOException {
        return readJsonFile(DANG_GIAO_DATA_PATH, new TypeReference<>() {});
    }

    public synchronized void addOrderToDangGiao(DangGiaoData dangGiaoData) throws IOException {
        List<DangGiaoData> dangGiaoList = getAllDangGiaoOrders();
        dangGiaoList.add(dangGiaoData);
        writeJsonFile(DANG_GIAO_DATA_PATH, dangGiaoList);
    }

    public List<DangGiaoData> getAllHistoryOrders() throws IOException {
        return readJsonFile(HISTORY_DATA_PATH, new TypeReference<>() {});
    }

    public synchronized BuyData moveOrderToHistory(String dangGiaoId) throws IOException {
        List<DangGiaoData> dangGiaoList = getAllDangGiaoOrders();
        List<DangGiaoData> historyList = getAllHistoryOrders();

        DangGiaoData orderToMove = null;
        int orderIndex = -1;

        for (int i = 0; i < dangGiaoList.size(); i++) {
            if (dangGiaoList.get(i).getId().equals(dangGiaoId)) {
                orderToMove = dangGiaoList.get(i);
                orderIndex = i;
                break;
            }
        }

        if (orderToMove != null) {
            BuyData originalBuyData = orderToMove.getOrderData(); // Lấy thông tin BuyData gốc

            orderToMove.setCompletedTimestamp(LocalDateTime.now());
            historyList.add(orderToMove);

            // Xóa khỏi dangGiaoList sau khi đã lấy thông tin
            dangGiaoList.remove(orderIndex);

            writeJsonFile(DANG_GIAO_DATA_PATH, dangGiaoList);
            writeJsonFile(HISTORY_DATA_PATH, historyList);

            return originalBuyData; // Trả về BuyData gốc
        } else {
            System.err.println("Order with id " + dangGiaoId + " not found in danggiao.json");
            return null; // Trả về null nếu không tìm thấy
        }
    }

    // Helper để tìm BuyData dựa trên các trường chính (cần đảm bảo tính duy nhất)
    public BuyData findBuyData(String username, LocalDateTime orderTimestamp, String productId, String variantId, String color) throws IOException {
        List<BuyData> buyOrders = getAllBuyOrders(); // Phương thức này đã có trong EmployeeService
        return buyOrders.stream()
                .filter(order -> order.getUsername().equals(username) &&
                        order.getOrderTimestamp().isEqual(orderTimestamp) && // Dùng isEqual cho LocalDateTime
                        order.getProductId().equals(productId) &&
                        order.getVariantId().equals(variantId) &&
                        order.getColor().equals(color))
                .findFirst().orElse(null);
    }

    public synchronized boolean removeOrderFromDangGiao(String dangGiaoId) throws IOException {
        List<DangGiaoData> dangGiaoList = getAllDangGiaoOrders();
        boolean removed = dangGiaoList.removeIf(order -> order.getId().equals(dangGiaoId));
        if (removed) {
            writeJsonFile(DANG_GIAO_DATA_PATH, dangGiaoList); // DANG_GIAO_DATA_PATH đã định nghĩa
        }
        return removed;
    }

    public synchronized DangGiaoData findDangGiaoOrderById(String dangGiaoId) throws IOException {
        List<DangGiaoData> dangGiaoList = getAllDangGiaoOrders();
        return dangGiaoList.stream()
                .filter(order -> order.getId().equals(dangGiaoId))
                .findFirst()
                .orElse(null);
    }



}