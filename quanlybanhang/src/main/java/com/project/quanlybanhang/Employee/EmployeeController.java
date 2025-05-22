package com.project.quanlybanhang.Employee;

import com.project.quanlybanhang.Buy.BuyData;
import com.project.quanlybanhang.Order.OrderService;
import com.project.quanlybanhang.User.User; // Import User class
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.project.quanlybanhang.User.User;
import com.project.quanlybanhang.Order.OrderService;
import com.project.quanlybanhang.Buy.BuyData;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.format.annotation.DateTimeFormat;


import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Controller
public class EmployeeController {

    private final EmployeeService employeeService;
    private final OrderService orderService;
    private final ReportService reportService;

    @Autowired
    public EmployeeController(EmployeeService employeeService, OrderService orderService, ReportService reportService) {
        this.employeeService = employeeService;
        this.orderService = orderService;
        this.reportService = reportService;
    }

    @GetMapping("/Employee") // Giữ nguyên mapping từ file employee.java của bạn
    public String employeePage(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        User employee = (User) session.getAttribute("user");
        if (employee == null || !"EMPLOYEE".equals(employee.getRole())) {
            redirectAttributes.addFlashAttribute("error", "Bạn cần đăng nhập với vai trò nhân viên để truy cập trang này.");
            return "redirect:/login";
        }
        model.addAttribute("employeeUsername", employee.getUsername());

        try {
            List<BuyData> allBuyOrders = employeeService.getAllBuyOrders();
            List<DangGiaoData> allDangGiaoOrders = employeeService.getAllDangGiaoOrders();
            String currentEmployeeUsername = employee.getUsername();

            List<DisplayOrder> ordersForEmployee = new ArrayList<>();

            // Xử lý đơn hàng đang được giao bởi nhân viên hiện tại
            for (DangGiaoData dgOrder : allDangGiaoOrders) {
                if (dgOrder.getEmployeeUsername().equals(currentEmployeeUsername)) {
                    ordersForEmployee.add(new DisplayOrder(dgOrder.getOrderData(), "accepted_by_me", dgOrder.getId()));
                }
            }

            // Xử lý đơn hàng mới (chưa ai nhận hoặc đã giao xong)
            for (BuyData buyOrder : allBuyOrders) {
                boolean isAlreadyAcceptedByOther = allDangGiaoOrders.stream()
                        .anyMatch(dg -> dg.getOrderData().equals(buyOrder) && !dg.getEmployeeUsername().equals(currentEmployeeUsername));

                boolean isAlreadyAcceptedByMe = ordersForEmployee.stream()
                        .anyMatch(deo -> deo.getBuyData().equals(buyOrder) && "accepted_by_me".equals(deo.getStatus()));

                if (!isAlreadyAcceptedByOther && !isAlreadyAcceptedByMe) {
                    boolean isInHistory = false; // Cần thêm logic kiểm tra history.json nếu muốn ẩn đơn đã hoàn thành hoàn toàn
                    // Tạm thời: Nếu không bị người khác nhận và không phải mình đang nhận thì là đơn mới
                    if (!isAlreadyAcceptedByMe) { // Kiểm tra lại để tránh trùng lặp nếu đơn đó vừa được chuyển
                        // Tạo một ID giả lập cho buyOrder nếu nó chưa có ID hoặc dùng trường timestamp + username + productID
                        // Ở đây, ta truyền các thông tin cần thiết để định danh đơn hàng
                        String uniqueOrderIdForBuy = buyOrder.getUsername() + "_" + buyOrder.getOrderTimestamp().toString() + "_" + buyOrder.getProductId() + "_" + buyOrder.getVariantId();
                        ordersForEmployee.add(new DisplayOrder(buyOrder, "new", uniqueOrderIdForBuy));
                    }
                }
            }
            // Loại bỏ trùng lặp nếu có đơn hàng mới mà bạn đã nhận
            List<DisplayOrder> distinctOrders = ordersForEmployee.stream()
                    .filter(distinctByKey(DisplayOrder::getCompositeKeyForBuyData))
                    .collect(Collectors.toList());


            model.addAttribute("orders", distinctOrders);

        } catch (IOException e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "Lỗi khi tải danh sách đơn hàng: " + e.getMessage());
            model.addAttribute("orders", new ArrayList<>());
        }

        return "html/employer";
    }

    // Predicate để dùng với filter cho stream, loại bỏ trùng lặp dựa trên key
    public static <T> java.util.function.Predicate<T> distinctByKey(java.util.function.Function<? super T, ?> keyExtractor) {
        java.util.Set<Object> seen = java.util.concurrent.ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
    }


    @PostMapping("/employee/accept-order")
    public String acceptOrder(@RequestParam String customerUsername,
                              @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime orderTimestamp,
                              @RequestParam String productId,
                              @RequestParam String variantId,
                              @RequestParam String color,
                              HttpSession session, RedirectAttributes redirectAttributes) {
        User employee = (User) session.getAttribute("user");
        if (employee == null || !"EMPLOYEE".equals(employee.getRole())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Phiên làm việc hết hạn hoặc không có quyền.");
            return "redirect:/login";
        }

        try {
            // Tìm BuyData gốc
            BuyData originalOrder = employeeService.findBuyData(customerUsername, orderTimestamp, productId, variantId, color);

            if (originalOrder == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy đơn hàng gốc.");
                return "redirect:/Employee";
            }

            // Kiểm tra xem đơn hàng này đã có ai nhận trong danggiao.json chưa
            List<DangGiaoData> currentDangGiaoOrders = employeeService.getAllDangGiaoOrders();
            boolean alreadyAccepted = currentDangGiaoOrders.stream()
                    .anyMatch(dgOrder -> dgOrder.getOrderData().equals(originalOrder));

            if (alreadyAccepted) {
                redirectAttributes.addFlashAttribute("warningMessage", "Đơn hàng này đã được nhân viên khác nhận!");
                return "redirect:/Employee";
            }


            DangGiaoData dangGiaoData = new DangGiaoData(originalOrder, employee.getUsername(), employee.getFullname());
            employeeService.addOrderToDangGiao(dangGiaoData);
            redirectAttributes.addFlashAttribute("successMessage", "Đã nhận đơn hàng thành công!");
        } catch (IOException e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi hệ thống khi nhận đơn: " + e.getMessage());
        }
        return "redirect:/Employee";
    }

    @PostMapping("/employee/complete-delivery")
    public String completeDelivery(@RequestParam String dangGiaoId, HttpSession session, RedirectAttributes redirectAttributes) {
        User employee = (User) session.getAttribute("user");
        if (employee == null || !"EMPLOYEE".equals(employee.getRole())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Phiên làm việc hết hạn hoặc không có quyền.");
            return "redirect:/login";
        }

        try {
            // Kiểm tra nhân viên có đúng là người giữ đơn
            List<DangGiaoData> dangGiaoOrders = employeeService.getAllDangGiaoOrders();
            DangGiaoData orderBeingCompleted = dangGiaoOrders.stream()
                    .filter(dg -> dg.getId().equals(dangGiaoId) && dg.getEmployeeUsername().equals(employee.getUsername()))
                    .findFirst()
                    .orElse(null);
            if(orderBeingCompleted == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy đơn hàng hoặc bạn không có quyền hoàn tất đơn này.");
                return "redirect:/Employee";
            }

            // Bước 1: Chuyển đơn hàng sang history và lấy thông tin BuyData gốc
            BuyData completedBuyOrderInfo = employeeService.moveOrderToHistory(dangGiaoId);

            if (completedBuyOrderInfo != null) {
                // Bước 2: Xóa đơn hàng gốc khỏi buy.json
                boolean removedFromBuy = orderService.removeOrder(completedBuyOrderInfo);
                if (removedFromBuy) {
                    redirectAttributes.addFlashAttribute("successMessage", "Đã giao hàng thành công, chuyển vào lịch sử và xóa khỏi danh sách đặt mua!");
                } else {
                    // Vẫn thành công ở bước 1, nhưng có lỗi khi xóa ở buy.json (hiếm khi xảy ra nếu logic đúng)
                    redirectAttributes.addFlashAttribute("warningMessage", "Giao hàng thành công và vào lịch sử, nhưng có lỗi khi cập nhật danh sách đặt mua chính.");
                }
            } else {
                // Lỗi: không tìm thấy đơn hàng trong danggiao.json để xử lý (đã được kiểm tra ở trên nhưng để an toàn)
                redirectAttributes.addFlashAttribute("errorMessage", "Không thể xử lý đơn hàng do không tìm thấy trong danh sách đang giao.");
            }

        } catch (IOException e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi hệ thống khi hoàn tất đơn hàng: " + e.getMessage());
        }
        return "redirect:/Employee";
    }

    @PostMapping("/employee/report-order")
    public String reportOrder(@RequestParam String orderId,
                              @RequestParam String orderType, // 'new' or 'accepted_by_me'
            /* Thêm các tham số khác của đơn hàng nếu cần để report */
                              HttpSession session, RedirectAttributes redirectAttributes) {
        User employee = (User) session.getAttribute("user");
        if (employee == null || !"EMPLOYEE".equals(employee.getRole())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng đăng nhập.");
            return "redirect:/login";
        }
        // TODO: Implement report logic
        // 1. Lấy thông tin đơn hàng dựa vào orderId và orderType
        // 2. Lưu thông tin report vào một file JSON mới (ví dụ: report.json) hoặc gửi thông báo
        // 3. Có thể thay đổi trạng thái của đơn hàng nếu cần (ví dụ: tạm khóa)

        System.out.println("Reported order ID: " + orderId + " of type: " + orderType + " by employee: " + employee.getUsername());
        redirectAttributes.addFlashAttribute("infoMessage", "Đã gửi báo cáo cho đơn hàng (ID: " + orderId + ").");
        return "redirect:/Employee";
    }

    // Lớp nội bộ để đóng gói dữ liệu hiển thị
    public static class DisplayOrder {
        private BuyData buyData;
        private String status; // "new", "accepted_by_me"
        private String dangGiaoId; // ID của đơn hàng trong danggiao.json, nếu có

        public DisplayOrder(BuyData buyData, String status, String id) {
            this.buyData = buyData;
            this.status = status;
            if ("accepted_by_me".equals(status)) {
                this.dangGiaoId = id;
            } else { // For "new" orders, id is a composite key from BuyData
                this.dangGiaoId = id; // Use this id for form submission if it's a new order
            }
        }

        // Tạo key tổng hợp từ BuyData để so sánh
        public String getCompositeKeyForBuyData() {
            if (buyData == null) return null;
            return buyData.getUsername() + "_" +
                    (buyData.getOrderTimestamp() != null ? buyData.getOrderTimestamp().toString() : "null_ts") + "_" +
                    buyData.getProductId() + "_" +
                    buyData.getVariantId() + "_" +
                    buyData.getColor();
        }

        public BuyData getBuyData() { return buyData; }
        public String getStatus() { return status; }
        public String getDangGiaoId() { return dangGiaoId; } // Sẽ là ID của DangGiaoData nếu status là "accepted_by_me", hoặc key định danh của BuyData nếu "new"
    }


    @PostMapping("/employee/submit-report")
    public String submitReport(@RequestParam String reportReason,
                               @RequestParam String orderType, // "new" hoặc "accepted_by_me"
                               // Các tham số để xác định đơn hàng
                               @RequestParam(required = false) String dangGiaoId, // Nếu orderType là "accepted_by_me"
                               @RequestParam(required = false) String customerUsername,
                               @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime orderTimestamp,
                               @RequestParam(required = false) String productId,
                               @RequestParam(required = false) String variantId,
                               @RequestParam(required = false) String color,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {

        User currentEmployee = (User) session.getAttribute("user");
        if (currentEmployee == null || !"EMPLOYEE".equals(currentEmployee.getRole())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng đăng nhập với vai trò nhân viên.");
            return "redirect:/login";
        }

        BuyData orderToReportDetails = null;
        String originalDangGiaoIdForReport = null;
        String originalAcceptingEmployeeForReport = null;
        boolean deleteFromDangGiao = false;

        try {
            if ("new".equals(orderType)) {
                // Tìm đơn hàng trong buy.json
                // Sử dụng EmployeeService hoặc OrderService để tìm BuyData, tùy thuộc bạn đặt findBuyData ở đâu
                // Ví dụ nếu dùng EmployeeService:
                orderToReportDetails = employeeService.findBuyData(customerUsername, orderTimestamp, productId, variantId, color);
                // Hoặc nếu dùng OrderService:
                // orderToReportDetails = orderService.findBuyData(customerUsername, orderTimestamp, productId, variantId, color);

            } else if ("accepted_by_me".equals(orderType) || "accepted_by_other".equals(orderType)) { // Mở rộng cho trường hợp admin report đơn đã có người nhận
                // Tìm đơn hàng trong danggiao.json
                if (dangGiaoId == null || dangGiaoId.isEmpty()) {
                    redirectAttributes.addFlashAttribute("errorMessage", "Thiếu ID đơn hàng đang giao để report.");
                    return "redirect:/Employee";
                }
                DangGiaoData dgOrder = employeeService.findDangGiaoOrderById(dangGiaoId);
                if (dgOrder != null) {
                    orderToReportDetails = dgOrder.getOrderData();
                    originalDangGiaoIdForReport = dgOrder.getId();
                    originalAcceptingEmployeeForReport = dgOrder.getEmployeeUsername();
                    deleteFromDangGiao = true; // Sẽ xóa khỏi danggiao.json
                }
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Loại đơn hàng không hợp lệ để report.");
                return "redirect:/Employee";
            }

            if (orderToReportDetails == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy đơn hàng để report.");
                return "redirect:/Employee";
            }

            // Tạo đối tượng ReportData
            ReportData newReport = new ReportData(
                    currentEmployee.getUsername(),
                    currentEmployee.getFullname(),
                    reportReason,
                    orderToReportDetails,
                    originalDangGiaoIdForReport,
                    originalAcceptingEmployeeForReport
            );

            // Lưu report
            reportService.addReport(newReport);

            // Xóa đơn hàng khỏi buy.json
            boolean removedFromBuy = orderService.removeOrder(orderToReportDetails);
            if (!removedFromBuy) {
                // Ghi log hoặc thông báo lỗi nếu không xóa được khỏi buy.json nhưng vẫn tiếp tục
                System.err.println("Warning: Could not remove reported order from buy.json: " + orderToReportDetails.getProductId());
            }

            // Nếu đơn hàng nằm trong danggiao.json, xóa nó đi
            if (deleteFromDangGiao && originalDangGiaoIdForReport != null) {
                employeeService.removeOrderFromDangGiao(originalDangGiaoIdForReport);
            }

            redirectAttributes.addFlashAttribute("successMessage", "Đã gửi report thành công và xử lý đơn hàng.");

        } catch (IOException e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi hệ thống khi gửi report: " + e.getMessage());
        }
        return "redirect:/Employee";
    }

}