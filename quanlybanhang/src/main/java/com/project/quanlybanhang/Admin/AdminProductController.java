package com.project.quanlybanhang.Admin;

import com.project.quanlybanhang.Buy.BuyData;
import com.project.quanlybanhang.Order.OrderService;
import com.project.quanlybanhang.Product.Product;
import com.project.quanlybanhang.Product.Productservice;
import com.project.quanlybanhang.Product.Variant;
import com.project.quanlybanhang.Product.colorprice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.beans.PropertyEditorSupport;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import com.project.quanlybanhang.User.User;
import com.project.quanlybanhang.User.UserService;

@Controller
@RequestMapping("/admin/products")
public class AdminProductController {

    private final Productservice productService;
    // UPLOAD_DIR_RELATIVE_TO_RESOURCES là đường dẫn tương đối từ thư mục resources
    // ví dụ: "static/data-product/image/"
    private final OrderService orderService;
    private static final String UPLOAD_DIR_RELATIVE_TO_RESOURCES = "static/data-product/image/";
    private final Path rootLocation; // Sẽ là .../src/main/resources/static/data-product/image/
    private final UserService userService;

    @Autowired
    public AdminProductController(Productservice productService, OrderService orderService, UserService userService) {
        this.productService = productService;
        this.orderService = orderService;
        this.userService = userService;
        String projectPath = System.getProperty("user.dir");
        // rootLocation trỏ đến thư mục lưu ảnh trong src/main/resources
        this.rootLocation = Paths.get(projectPath, "src", "main", "resources", UPLOAD_DIR_RELATIVE_TO_RESOURCES);
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            System.err.println("Không thể khởi tạo thư mục lưu trữ upload tại src: " + e.getMessage());
        }
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(int.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) throws IllegalArgumentException {
                if (text == null || text.trim().isEmpty()) {
                    setValue(0);
                } else {
                    try {
                        setValue(Integer.parseInt(text));
                    } catch (NumberFormatException e) { setValue(0); }
                }
            }
        });
        binder.registerCustomEditor(Integer.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) throws IllegalArgumentException {
                if (text == null || text.trim().isEmpty()) {
                    setValue(null);
                } else {
                    try {
                        setValue(Integer.valueOf(text));
                    } catch (NumberFormatException e) { setValue(null); }
                }
            }
        });
    }

    @PostMapping("/delete")
    public String handleDeleteProduct(@RequestParam("productId") String productId,
                                      RedirectAttributes redirectAttributes) {
        System.out.println("[AdminProductController] POST /delete - Attempting to delete product ID: " + productId);
        try {
            // Giả định productService.deleteProduct đã được cập nhật để xóa ảnh liên quan
            boolean success = productService.deleteProduct(productId);
            if (success) {
                redirectAttributes.addFlashAttribute("successMessage", "Đã xóa sản phẩm (ID: " + productId + ") và các ảnh liên quan thành công!");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy sản phẩm (ID: " + productId + ") để xóa.");
            }
        } catch (IOException e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi hệ thống khi xóa sản phẩm: " + e.getMessage());
        }
        return "redirect:/admin/products/edit";
    }

    @GetMapping("/edit")
    public String showEditProductPage(@RequestParam(name = "productId", required = false) String productId, Model model) {
        System.out.println("[AdminProductController] GET /edit - productId: " + productId);
        try {
            List<Product> allProducts = productService.getAllProducts();
            model.addAttribute("products", allProducts);
            model.addAttribute("activeTab", "chinh-sua-san-pham");

            if (!model.containsAttribute("newProduct")) {
                Product newEmptyProduct = new Product();
                Variant initialVariant = new Variant();
                initialVariant.setColorprices(new ArrayList<>(Arrays.asList(new colorprice())));
                newEmptyProduct.setVariants(new ArrayList<>(Arrays.asList(initialVariant)));
                newEmptyProduct.setTags(new ArrayList<>());
                model.addAttribute("newProduct", newEmptyProduct);
            }

            if (productId != null && allProducts != null) {
                Product productToEdit = allProducts.stream()
                        .filter(p -> p.getId().equals(productId))
                        .findFirst()
                        .orElse(null);
                if (productToEdit != null) {
                    if (productToEdit.getTags() == null) {
                        productToEdit.setTags(new ArrayList<>());
                    }
                    model.addAttribute("productToEdit", productToEdit);
                } else {
                    model.addAttribute("errorMessage", "Không tìm thấy sản phẩm với ID: " + productId);
                }
            }
        } catch (IOException e) {
            model.addAttribute("errorMessage", "Lỗi khi tải dữ liệu sản phẩm: " + e.getMessage());
            model.addAttribute("products", new ArrayList<>());
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Lỗi không mong muốn: " + e.getMessage());
            model.addAttribute("products", new ArrayList<>());
        }
        return "html/admin";
    }

    @GetMapping("/add")
    public String showAddProductForm(Model model) {
        System.out.println("[AdminProductController] GET /add - Displaying add product form.");
        Product newProduct = new Product();
        Variant firstVariant = new Variant();
        firstVariant.setColorprices(new ArrayList<>(Arrays.asList(new colorprice())));
        newProduct.setVariants(new ArrayList<>(Arrays.asList(firstVariant)));
        newProduct.setTags(new ArrayList<>());
        model.addAttribute("newProduct", newProduct);

        try {
            model.addAttribute("products", productService.getAllProducts());
        } catch (IOException e) {
            model.addAttribute("products", new ArrayList<>());
        }
        model.addAttribute("activeTab", "them-san-pham");
        return "html/admin";
    }

    @PostMapping("/save")
    public String handleAddProduct(@ModelAttribute("newProduct") Product productToAdd,
                                   @RequestParam(name = "variantImageFiles", required = false) List<MultipartFile> variantImageFiles,
                                   @RequestParam(name = "tagsString", required = false) String tagsString,
                                   BindingResult bindingResult,
                                   RedirectAttributes redirectAttributes) {
        System.out.println("[AdminProductController] POST /save - Adding product: " + productToAdd.getName());

        if (bindingResult.hasErrors()) {
            System.err.println("Lỗi binding khi thêm sản phẩm: " + bindingResult.getAllErrors());
            redirectAttributes.addFlashAttribute("errorMessage", "Dữ liệu không hợp lệ. Vui lòng kiểm tra lại.");
            redirectAttributes.addFlashAttribute("newProduct", productToAdd);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.newProduct", bindingResult);
            return "redirect:/admin/products/add";
        }

        if (tagsString != null && !tagsString.trim().isEmpty()) {
            productToAdd.setTags(Arrays.stream(tagsString.split(","))
                    .map(String::trim)
                    .filter(tag -> !tag.isEmpty())
                    .collect(Collectors.toList()));
        } else {
            productToAdd.setTags(new ArrayList<>());
        }

        if (productToAdd.getVariants() != null && variantImageFiles != null) {
            int numVariants = productToAdd.getVariants().size();
            int numFiles = variantImageFiles.size();
            System.out.println("Số variants: " + numVariants + ", Số files ảnh: " + numFiles);

            for (int i = 0; i < numVariants; i++) {
                Variant variant = productToAdd.getVariants().get(i);
                if (i < numFiles) {
                    MultipartFile imageFile = variantImageFiles.get(i);
                    if (imageFile != null && !imageFile.isEmpty()) {
                        try {
                            String originalFilename = StringUtils.cleanPath(imageFile.getOriginalFilename());
                            String fileExtension = "";
                            int lastDot = originalFilename.lastIndexOf('.');
                            if (lastDot >= 0) { // Sửa: lastDot > 0 thành lastDot >= 0 để xử lý file không có phần mở rộng
                                fileExtension = originalFilename.substring(lastDot);
                            }

                            String variantIdPart = (variant.getId() != null && !variant.getId().isEmpty()) ? variant.getId() : "newvar" + i;
                            // productToAdd.getId() sẽ là null ở thời điểm này vì ID được tạo trong productService
                            // Sử dụng một chuỗi ngẫu nhiên hoặc một phần của tên sản phẩm nếu muốn
                            String baseNameForProduct = "prod_" + UUID.randomUUID().toString().substring(0, 4);
                            String uniqueFilenameBase = baseNameForProduct + "_" + variantIdPart + "_" + UUID.randomUUID().toString().substring(0, 8);
                            String uniqueFilename = uniqueFilenameBase + fileExtension;

                            Path destinationFile = this.rootLocation.resolve(uniqueFilename).normalize().toAbsolutePath();
                            if (!destinationFile.getParent().equals(this.rootLocation.toAbsolutePath())) {
                                throw new IOException("Không thể lưu file bên ngoài thư mục upload quy định.");
                            }
                            try (InputStream inputStream = imageFile.getInputStream()) {
                                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
                                // Sau khi lưu vào src, cũng cố gắng lưu vào target để thay đổi có hiệu lực ngay
                                Path targetRootLocation = Paths.get(System.getProperty("user.dir"), "target", "classes", UPLOAD_DIR_RELATIVE_TO_RESOURCES);
                                Files.createDirectories(targetRootLocation); // Đảm bảo thư mục target tồn tại
                                Path destinationFileInTarget = targetRootLocation.resolve(uniqueFilename).normalize().toAbsolutePath();
                                if (destinationFileInTarget.getParent().equals(targetRootLocation.toAbsolutePath())) {
                                    try (InputStream inputStreamTarget = imageFile.getInputStream()) { // Cần lấy lại InputStream
                                        Files.copy(inputStreamTarget, destinationFileInTarget, StandardCopyOption.REPLACE_EXISTING);
                                        System.out.println("Đã sao chép ảnh vào target: " + destinationFileInTarget);
                                    } catch (IOException e_target) {
                                        System.err.println("Lỗi sao chép ảnh vào target cho variant " + i + ": " + e_target.getMessage());
                                    }
                                }
                            }
                            variant.setImage("/data-product/image/" + uniqueFilename);
                            System.out.println("Đã upload ảnh cho variant " + i + " vào src: " + variant.getImage());
                        } catch (IOException e) {
                            System.err.println("Lỗi upload file cho variant " + i + ": " + e.getMessage());
                            variant.setImage(null);
                            redirectAttributes.addFlashAttribute("warningMessage", "Lỗi upload ảnh cho phiên bản " + (i + 1) + ": " + e.getMessage() + ". Ảnh sẽ không được lưu.");
                        }
                    }
                }
            }
        }

        try {
            boolean success = productService.addProduct(productToAdd);
            if (success) {
                redirectAttributes.addFlashAttribute("successMessage", "Sản phẩm '" + productToAdd.getName() + "' đã được thêm thành công!");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Không thể thêm sản phẩm. Có lỗi xảy ra khi lưu.");
            }
        } catch (IOException e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi hệ thống khi thêm sản phẩm: " + e.getMessage());
        }
        return "redirect:/admin/products/edit";
    }

    @PostMapping("/update")
    public String handleUpdateProduct(@ModelAttribute("productToEdit") Product productToUpdate,
                                      @RequestParam(name = "variantImageFiles", required = false) List<MultipartFile> variantImageFiles,
                                      @RequestParam(name = "tagsString", required = false) String tagsString,
                                      BindingResult bindingResult,
                                      RedirectAttributes redirectAttributes) {
        System.out.println("[AdminProductController] POST /update - Product ID: " + productToUpdate.getId());
        if (bindingResult.hasErrors()) {
            System.err.println("Lỗi binding khi update: " + bindingResult.getAllErrors());
            redirectAttributes.addFlashAttribute("errorMessage", "Dữ liệu cập nhật không hợp lệ.");
            redirectAttributes.addFlashAttribute("productToEdit", productToUpdate);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.productToEdit", bindingResult);
            return "redirect:/admin/products/edit?productId=" + productToUpdate.getId();
        }

        if (tagsString != null && !tagsString.trim().isEmpty()) {
            productToUpdate.setTags(Arrays.stream(tagsString.split(","))
                    .map(String::trim)
                    .filter(tag -> !tag.isEmpty())
                    .collect(Collectors.toList()));
        } else {
            productToUpdate.setTags(new ArrayList<>());
        }

        if (productToUpdate.getVariants() != null && variantImageFiles != null) {
            int numVariants = productToUpdate.getVariants().size();
            int numFiles = variantImageFiles.size();

            for (int i = 0; i < numVariants; i++) {
                Variant variant = productToUpdate.getVariants().get(i);
                if (i < numFiles) {
                    MultipartFile imageFile = variantImageFiles.get(i);
                    if (imageFile != null && !imageFile.isEmpty()) { // Nếu có file ảnh mới được upload cho variant này
                        try {
                            // 1. Xóa ảnh cũ (nếu có) khỏi cả src và target
                            String oldImageRelativePath = variant.getImage(); // Đường dẫn cũ dạng /data-product/image/ten_file.jpg
                            if (oldImageRelativePath != null && !oldImageRelativePath.isEmpty()) {
                                String oldFilename = oldImageRelativePath.substring(oldImageRelativePath.lastIndexOf("/") + 1);
                                try {
                                    // Xóa từ src/main/resources/...
                                    Path oldFileInSrc = this.rootLocation.resolve(oldFilename).normalize();
                                    if (Files.exists(oldFileInSrc) && oldFileInSrc.startsWith(this.rootLocation.toAbsolutePath())) {
                                        Files.deleteIfExists(oldFileInSrc);
                                        System.out.println("Đã xóa ảnh cũ trong src: " + oldFileInSrc);
                                    }
                                    // Xóa từ target/classes/...
                                    Path targetRootLocation = Paths.get(System.getProperty("user.dir"), "target", "classes", UPLOAD_DIR_RELATIVE_TO_RESOURCES);
                                    Path oldFileInTarget = targetRootLocation.resolve(oldFilename).normalize();
                                    if (Files.exists(oldFileInTarget) && oldFileInTarget.startsWith(targetRootLocation.toAbsolutePath())) {
                                        Files.deleteIfExists(oldFileInTarget);
                                        System.out.println("Đã xóa ảnh cũ trong target: " + oldFileInTarget);
                                    }
                                } catch (IOException ex_delete) {
                                    System.err.println("Không thể xóa ảnh cũ '" + oldFilename + "': " + ex_delete.getMessage());
                                    redirectAttributes.addFlashAttribute("warningMessage", "Không thể xóa ảnh cũ: " + oldFilename);
                                }
                            }

                            // 2. Lưu ảnh mới vào src và target
                            String originalFilename = StringUtils.cleanPath(imageFile.getOriginalFilename());
                            String fileExtension = "";
                            int lastDot = originalFilename.lastIndexOf('.');
                            if (lastDot >= 0) {
                                fileExtension = originalFilename.substring(lastDot);
                            }

                            String variantIdPart = (variant.getId() != null && !variant.getId().isEmpty()) ? variant.getId() : "var" + i;
                            String uniqueFilenameBase = productToUpdate.getId() + "_" + variantIdPart + "_" + UUID.randomUUID().toString().substring(0, 8);
                            String uniqueFilename = uniqueFilenameBase + fileExtension;

                            // Lưu vào src
                            Path destinationFileSrc = this.rootLocation.resolve(uniqueFilename).normalize().toAbsolutePath();
                            if (!destinationFileSrc.getParent().equals(this.rootLocation.toAbsolutePath())) {
                                throw new IOException("Không thể lưu file (src) bên ngoài thư mục upload quy định.");
                            }
                            try (InputStream inputStreamSrc = imageFile.getInputStream()) {
                                Files.copy(inputStreamSrc, destinationFileSrc, StandardCopyOption.REPLACE_EXISTING);
                            }
                            System.out.println("Đã lưu ảnh mới vào src: " + destinationFileSrc);

                            // Lưu vào target
                            Path targetRootLocation = Paths.get(System.getProperty("user.dir"), "target", "classes", UPLOAD_DIR_RELATIVE_TO_RESOURCES);
                            Files.createDirectories(targetRootLocation); // Đảm bảo thư mục target tồn tại
                            Path destinationFileTarget = targetRootLocation.resolve(uniqueFilename).normalize().toAbsolutePath();
                            if (!destinationFileTarget.getParent().equals(targetRootLocation.toAbsolutePath())) {
                                throw new IOException("Không thể lưu file (target) bên ngoài thư mục upload quy định.");
                            }
                            try (InputStream inputStreamTarget = imageFile.getInputStream()) { // Lấy lại InputStream
                                Files.copy(inputStreamTarget, destinationFileTarget, StandardCopyOption.REPLACE_EXISTING);
                            }
                            System.out.println("Đã lưu ảnh mới vào target: " + destinationFileTarget);

                            variant.setImage("/data-product/image/" + uniqueFilename); // Cập nhật đường dẫn ảnh mới cho variant

                        } catch (IOException e) {
                            System.err.println("Lỗi upload ảnh mới cho variant " + i + " (update): " + e.getMessage());
                            redirectAttributes.addFlashAttribute("warningMessage", "Lỗi upload ảnh mới cho phiên bản " + (i + 1) + ". Giữ lại ảnh cũ (nếu có). " + e.getMessage());
                            // Không thay đổi variant.getImage() nếu có lỗi, để giữ lại ảnh cũ nếu đã có.
                        }
                    }
                    // Nếu không có file ảnh mới được upload (imageFile rỗng hoặc null),
                    // thì variant.getImage() sẽ giữ nguyên giá trị cũ (được bind từ form).
                }
            }
        }

        try {
            boolean success = productService.updateProduct(productToUpdate);
            if (success) {
                redirectAttributes.addFlashAttribute("successMessage", "Cập nhật sản phẩm '" + productToUpdate.getName() + "' thành công!");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: Không tìm thấy sản phẩm để cập nhật.");
            }
        } catch (IOException e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi hệ thống khi cập nhật sản phẩm.");
        }
        return "redirect:/admin/products/edit?productId=" + productToUpdate.getId();
    }

    @GetMapping("/users")
    public String showUserManagementPage(@RequestParam(name = "username", required = false) String username, Model model) {
        System.out.println("[AdminProductController] GET /users - Editing username: " + username);
        model.addAttribute("activeTab", "thong-tin-nguoi-dung");
        try {
            List<User> allUsers = userService.getAllUsers();
            model.addAttribute("allUsers", (allUsers == null) ? new ArrayList<>() : allUsers);

            if (username != null) {
                User userToEdit = userService.getUserByUsername(username);
                if (userToEdit != null) {
                    model.addAttribute("userToEdit", userToEdit);
                } else {
                    model.addAttribute("userManagementErrorMessage", "Không tìm thấy người dùng: " + username);
                }
            }
        } catch (IOException e) {
            model.addAttribute("userManagementErrorMessage", "Lỗi khi tải danh sách người dùng: " + e.getMessage());
            model.addAttribute("allUsers", new ArrayList<>());
        } catch (Exception ex) {
            model.addAttribute("userManagementErrorMessage", "Lỗi không mong muốn: " + ex.getMessage());
            model.addAttribute("allUsers", new ArrayList<>());
        }

        if (!model.containsAttribute("products")) {
            try { model.addAttribute("products", productService.getAllProducts()); } catch (IOException e) { model.addAttribute("products", new ArrayList<>());}
        }
        if (!model.containsAttribute("newProduct")) {
            Product newEmptyProduct = new Product();
            Variant initialVariant = new Variant();
            initialVariant.setColorprices(new ArrayList<>(Arrays.asList(new colorprice())));
            newEmptyProduct.setVariants(new ArrayList<>(Arrays.asList(initialVariant)));
            newEmptyProduct.setTags(new ArrayList<>());
            model.addAttribute("newProduct", newEmptyProduct);
        }
        return "html/admin";
    }

    @PostMapping("/users/update")
    public String handleUpdateUser(@ModelAttribute("userToEdit") User userFromForm, RedirectAttributes redirectAttributes) {
        System.out.println("[AdminProductController] POST /users/update - Updating user: " + userFromForm.getUsername());
        System.out.println("[AdminProductController] Role from form received by controller: " + userFromForm.getRole()); // Log để kiểm tra
        try {
            User existingUser = userService.getUserByUsername(userFromForm.getUsername());
            if (existingUser != null) {
                // Cập nhật các trường từ userFromForm (dữ liệu từ form) vào existingUser (đối tượng sẽ được lưu)
                existingUser.setFullname(userFromForm.getFullname());
                existingUser.setEmail(userFromForm.getEmail());
                existingUser.setBan(userFromForm.getBan()); // Giả sử getBan() trả về String "true"/"false"

                // === THÊM DÒNG NÀY ĐỂ CẬP NHẬT ROLE ===
                existingUser.setRole(userFromForm.getRole());



                boolean success = userService.updateUser(existingUser); // Truyền existingUser đã được cập nhật đầy đủ
                if (success) {
                    redirectAttributes.addFlashAttribute("userManagementSuccessMessage", "Cập nhật người dùng '" + userFromForm.getUsername() + "' thành công!");
                } else {
                    redirectAttributes.addFlashAttribute("userManagementErrorMessage", "Lỗi khi cập nhật người dùng '" + userFromForm.getUsername() + "'.");
                }
            } else {
                redirectAttributes.addFlashAttribute("userManagementErrorMessage", "Không tìm thấy người dùng '" + userFromForm.getUsername() + "' để cập nhật.");
            }
        } catch (IOException e) {
            e.printStackTrace(); // In lỗi ra console server để dễ debug
            redirectAttributes.addFlashAttribute("userManagementErrorMessage", "Lỗi hệ thống khi cập nhật người dùng: " + e.getMessage());
        }
        return "redirect:/admin/products/users"; // Redirect về trang danh sách người dùng
    }

    @PostMapping("/users/delete")
    public String handleDeleteUser(@RequestParam("username") String username, RedirectAttributes redirectAttributes) {
        System.out.println("[AdminProductController] POST /users/delete - Deleting user: " + username);
        if ("admin".equalsIgnoreCase(username)) {
            redirectAttributes.addFlashAttribute("userManagementErrorMessage", "Không thể xóa tài khoản admin!");
            return "redirect:/admin/products/users";
        }
        try {
            boolean success = userService.deleteUser(username);
            if (success) {
                redirectAttributes.addFlashAttribute("userManagementSuccessMessage", "Đã xóa người dùng '" + username + "' thành công!");
            } else {
                redirectAttributes.addFlashAttribute("userManagementErrorMessage", "Không tìm thấy người dùng '" + username + "' để xóa hoặc không thể xóa.");
            }
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("userManagementErrorMessage", "Lỗi hệ thống khi xóa người dùng: " + e.getMessage());
        }
        return "redirect:/admin/products/users";
    }



    @GetMapping("/orders")
    public String showOrderManagementPage(Model model) {
        System.out.println("[AdminProductController] GET /orders - Loading order management page.");
        model.addAttribute("activeTab", "hang-dang-dat"); // Để active đúng tab trên sidebar
        try {
            List<BuyData> allOrders = orderService.getAllOrders();
            model.addAttribute("allOrders", allOrders);
            if (allOrders.isEmpty()) {
                model.addAttribute("orderManagementMessage", "Không có đơn hàng nào.");
            }
        } catch (IOException e) {
            e.printStackTrace();
            model.addAttribute("orderManagementErrorMessage", "Lỗi khi tải danh sách đơn hàng: " + e.getMessage());
            model.addAttribute("allOrders", new ArrayList<>());
        }

        // Thêm các model attributes cần thiết khác cho trang admin.html nếu nó dùng chung template
        if (!model.containsAttribute("products")) {
            try { model.addAttribute("products", productService.getAllProducts()); } catch (IOException e) { model.addAttribute("products", new ArrayList<>());}
        }
        if (!model.containsAttribute("newProduct")) {
            Product newEmptyProduct = new Product();
            Variant initialVariant = new Variant();
            if (initialVariant.getColorprices() == null) initialVariant.setColorprices(new ArrayList<>());
            if (initialVariant.getColorprices().isEmpty()) initialVariant.getColorprices().add(new colorprice());
            newEmptyProduct.setVariants(new ArrayList<>(List.of(initialVariant)));
            newEmptyProduct.setTags(new ArrayList<>());
            model.addAttribute("newProduct", newEmptyProduct);
        }
        if (!model.containsAttribute("allUsers")) { // Đảm bảo có allUsers cho tab khác
            try { model.addAttribute("allUsers", userService.getAllUsers()); }
            catch (IOException e) { model.addAttribute("allUsers", new ArrayList<>()); }
        }

        return "html/admin";
    }
}