package com.project.quanlybanhang.Advertisement;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Controller // Hoặc @RestController nếu tất cả các method đều trả về @ResponseBody
public class AdvertisementController {

    // Đường dẫn đến thư mục lưu ảnh quảng cáo trong static resources
    // QUAN TRỌNG: Đường dẫn này hoạt động tốt trong môi trường phát triển (IDE).
    // Khi đóng gói thành file JAR để deploy, việc ghi vào classpath sẽ có vấn đề.
    // Bạn sẽ cần một thư mục bên ngoài project và cấu hình Spring để phục vụ file từ đó.
    private final Path advertisementStorageDir = Paths.get("src/main/resources/static/advertisements");

    public AdvertisementController() {
        // Tạo thư mục nếu chưa tồn tại khi khởi tạo Controller
        try {
            Files.createDirectories(advertisementStorageDir);
        } catch (IOException e) {
            System.err.println("Không thể tạo thư mục lưu trữ ảnh quảng cáo: " + e.getMessage());
            // Có thể throw RuntimeException ở đây nếu thư mục này là bắt buộc
        }
    }

    @PostMapping("/admin/advertisements/update-images")
    @ResponseBody // Để trả về text/plain hoặc JSON
    public ResponseEntity<String> updateAdvertisementImages(@RequestParam("newImages") MultipartFile[] newImages) {
        if (newImages == null || newImages.length == 0 || Arrays.stream(newImages).allMatch(MultipartFile::isEmpty)) {
            return ResponseEntity.badRequest().body("Vui lòng chọn ít nhất một ảnh để tải lên.");
        }

        try {
            // 1. Xóa tất cả các file hiện có trong thư mục advertisements
            try (Stream<Path> existingFilesStream = Files.list(advertisementStorageDir)) {
                existingFilesStream
                        .filter(path -> !Files.isDirectory(path)) // Chỉ xóa file, không xóa thư mục con (nếu có)
                        .forEach(path -> {
                            try {
                                Files.delete(path);
                            } catch (IOException e) {
                                System.err.println("Không thể xóa file cũ: " + path + " - " + e.getMessage());
                                // Cân nhắc việc dừng lại hoặc ghi log nghiêm trọng hơn
                            }
                        });
            }

            // 2. Lưu các file ảnh mới
            // Đặt tên file đơn giản để dễ dàng sắp xếp và hiển thị (ví dụ: ad_0.jpg, ad_1.jpg, ...)
            for (int i = 0; i < newImages.length; i++) {
                MultipartFile file = newImages[i];
                if (file.isEmpty()) {
                    continue;
                }
                String originalFilename = file.getOriginalFilename();
                String extension = "";
                if (originalFilename != null && originalFilename.contains(".")) {
                    extension = originalFilename.substring(originalFilename.lastIndexOf("."));
                }
                // Đặt tên file có thứ tự để dễ dàng hiển thị tuần tự nếu muốn
                String newFileName = "ad_" + i + extension;
                Path targetLocation = advertisementStorageDir.resolve(newFileName);
                Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            }

            return ResponseEntity.ok("Đã cập nhật " + newImages.length + " ảnh quảng cáo thành công!");

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi server: Không thể cập nhật ảnh quảng cáo. " + e.getMessage());
        }
    }

    // API để trang home.html lấy danh sách các ảnh quảng cáo hiện tại
    @GetMapping("/api/advertisements/current-images")
    @ResponseBody
    public ResponseEntity<List<String>> getCurrentAdvertisementImages() {
        List<String> imageUrls = new ArrayList<>();
        if (!Files.exists(advertisementStorageDir) || !Files.isDirectory(advertisementStorageDir)) {
            return ResponseEntity.ok(imageUrls); // Trả về rỗng nếu thư mục không tồn tại
        }

        try (Stream<Path> imagePathsStream = Files.list(advertisementStorageDir)) {
            imageUrls = imagePathsStream
                    .filter(Files::isRegularFile)
                    .map(path -> "/advertisements/" + path.getFileName().toString()) // Tạo URL tương đối
                    .sorted() // Sắp xếp theo tên file (ad_0.jpg, ad_1.jpg,...)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(imageUrls);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ArrayList<>());
        }
    }
}