# Dự án Quản Lý Bán Hàng

Đây là một ứng dụng web quản lý bán hàng được xây dựng bằng Spring Boot.

## Mục lục

- [Công nghệ và Thư viện](#công-nghệ-và-thư-viện)
- [Tính năng](#tính-năng)
- [Hướng dẫn cài đặt và chạy dự án](#hướng-dẫn-cài-đặt-và-chạy-dự-án)
    - [Yêu cầu](#yêu-cầu)
    - [Các bước cài đặt](#các-bước-cài-đặt)
- [Cấu trúc thư mục](#cấu-trúc-thư-mục)
- [Hướng dẫn sử dụng](#hướng-dẫn-sử-dụng)
    - [Đăng nhập](#đăng-nhập)
    - [Trang chủ](#trang-chủ)
    - [Trang sản phẩm](#trang-sản-phẩm)
    - [Giỏ hàng](#giỏ-hàng)
    - [Trang cá nhân](#trang-cá-nhân)
    - [Trang quản trị (Admin)](#trang-quản-trị-admin)
    - [Trang nhân viên (Employee)](#trang-nhân-viên-employee)


## Công nghệ và Thư viện

Dự án sử dụng các công nghệ và thư viện chính sau:

- **Java 17**
- **Spring Boot 3.4.5**: Framework chính để xây dựng ứng dụng.
    - `spring-boot-starter-thymeleaf`: Template engine để xây dựng giao diện người dùng.
    - `spring-boot-starter-web`: Hỗ trợ xây dựng ứng dụng web, bao gồm RESTful API.
    - `spring-boot-devtools`: Cung cấp các công cụ phát triển nhanh (tự động restart, live reload).
    - `spring-boot-starter-test`: Hỗ trợ viết unit test và integration test.
- **Maven**: Công cụ quản lý dự án và build.
- **Jackson Databind**: Thư viện xử lý JSON (serialize và deserialize Java objects).
- **Jackson Datatype JSR310**: Hỗ trợ Jackson xử lý các kiểu dữ liệu ngày giờ của Java 8+ (java.time).
- **MySQL Connector/J 8.3.0**: Driver JDBC để kết nối với cơ sở dữ liệu MySQL (Mặc dù trong code hiện tại, dự án đang sử dụng file JSON làm nơi lưu trữ dữ liệu thay vì MySQL).
- **Thymeleaf**: Template engine phía server để render HTML.
- **HTML, CSS, JavaScript**: Ngôn ngữ cơ bản để xây dựng giao diện người dùng và tương tác phía client.
- **Font Awesome**: Thư viện icon.
- **Chart.js**: Thư viện vẽ biểu đồ (sử dụng trong trang admin để hiển thị doanh thu).

Dữ liệu của ứng dụng được lưu trữ trong các file JSON, bao gồm:
- `src/main/resources/static/data-user/users.json`: Thông tin người dùng.
- `src/main/resources/static/data-product/product.json`: Thông tin sản phẩm.
- `src/main/resources/static/data-cart/cart.json`: Thông tin giỏ hàng của người dùng.
- `src/main/resources/static/data-buy/buy.json`: Thông tin đơn hàng đã được xác nhận mua.
- `src/main/resources/static/dang-giao/danggiao.json`: Thông tin đơn hàng đang được giao bởi nhân viên.
- `src/main/resources/static/lichsugiaodich/history.json`: Lịch sử các giao dịch đã hoàn thành.
- `src/main/resources/static/report/report.json`: Thông tin các đơn hàng bị nhân viên báo cáo (report).

## Tính năng

### Chức năng chung:
- **Xem sản phẩm**: Hiển thị danh sách sản phẩm, chi tiết sản phẩm.
- **Tìm kiếm sản phẩm**: Tìm kiếm sản phẩm theo tags.
- **Phân loại sản phẩm**: Lọc sản phẩm theo danh mục.

### Chức năng người dùng (Khách hàng):
- **Đăng ký**: Tạo tài khoản mới.
- **Đăng nhập**: Truy cập vào hệ thống bằng tài khoản đã đăng ký.
- **Đăng xuất**: Thoát khỏi hệ thống.
- **Quản lý giỏ hàng**:
    - Thêm sản phẩm vào giỏ.
    - Xem giỏ hàng.
    - Cập nhật số lượng sản phẩm trong giỏ.
    - Xóa sản phẩm khỏi giỏ.
- **Đặt hàng**:
    - Nhập thông tin giao hàng (địa chỉ, số điện thoại).
    - Lưu thông tin đơn hàng.
    - Sau khi đặt hàng, sản phẩm sẽ bị xóa khỏi giỏ hàng.
- **Xem thông tin cá nhân**: Xem và cập nhật thông tin cá nhân (họ tên, email, mật khẩu).
- **Xem lịch sử giao dịch**: (Chức năng này có thể chưa được triển khai đầy đủ ở phía người dùng, nhưng dữ liệu lịch sử được lưu trữ).

### Chức năng Nhân viên (Employee):
- **Đăng nhập**: Với vai trò "EMPLOYEE".
- **Xem danh sách đơn hàng**:
    - Xem các đơn hàng mới (chưa có nhân viên nào nhận).
    - Xem các đơn hàng mình đang giao.
- **Nhận đơn hàng**: Chọn một đơn hàng mới để thực hiện giao hàng.
    - Đơn hàng sau khi nhận sẽ được chuyển vào danh sách "đang giao" của nhân viên đó (`danggiao.json`).
- **Hoàn tất giao hàng**:
    - Xác nhận đơn hàng đã giao thành công.
    - Đơn hàng sẽ được chuyển từ `danggiao.json` sang `history.json`.
    - Đơn hàng gốc trong `buy.json` sẽ được xóa.
- **Report đơn hàng**:
    - Báo cáo vấn đề với một đơn hàng (mới hoặc đang giao).
    - Đơn hàng bị report sẽ được ghi vào `report.json`.
    - Đơn hàng bị report sẽ được xóa khỏi `buy.json` và `danggiao.json` (nếu có).

### Chức năng Quản trị viên (Admin):
- **Đăng nhập**: Với tài khoản admin (mặc định: username `admin`, password `admin`).
- **Quản lý sản phẩm**:
    - Xem danh sách sản phẩm.
    - Thêm sản phẩm mới (bao gồm thông tin chi tiết, các phiên bản, màu sắc, giá, ảnh).
    - Sửa thông tin sản phẩm hiện có.
    - Xóa sản phẩm (bao gồm cả việc xóa ảnh liên quan).
    - Ảnh sản phẩm được lưu trữ tại `src/main/resources/static/data-product/image/` và `target/classes/static/data-product/image/`.
- **Quản lý người dùng**:
    - Xem danh sách tất cả người dùng.
    - Sửa thông tin người dùng (họ tên, email, trạng thái cấm, vai trò).
    - Xóa người dùng (không thể xóa tài khoản admin).
- **Quản lý đơn hàng (Khách đặt)**:
    - Xem danh sách tất cả các đơn hàng khách đã đặt (từ `buy.json`).
- **Xem báo cáo (Report) từ nhân viên**:
    - Xem danh sách các đơn hàng bị nhân viên report.
    - Xóa tất cả các report.
- **Xem báo cáo doanh thu**:
    - Xem tổng doanh thu từ trước đến nay.
    - Xem doanh thu theo từng tháng của một năm cụ thể.
    - Hiển thị biểu đồ doanh thu theo tháng.

## Hướng dẫn cài đặt và chạy dự án

### Yêu cầu
- JDK 17 hoặc mới hơn.
- Maven 3.x.
- Một IDE hỗ trợ Java và Maven (ví dụ: IntelliJ IDEA, Eclipse, VS Code).

### Các bước cài đặt
1.  **Clone repository (Tải mã nguồn)**:
    ```bash
    git clone <URL_repository_cua_ban>
    cd quanlybanhang 
    ```
    Hoặc tải trực tiếp file ZIP và giải nén.

2.  **Build dự án bằng Maven**:
    Mở Terminal hoặc Command Prompt tại thư mục gốc của dự án (thư mục `quanlybanhang` chứa file `pom.xml`) và chạy lệnh:
    ```bash
    mvn clean install
    ```
    Lệnh này sẽ tải các thư viện cần thiết và build dự án.

3.  **Chạy ứng dụng**:
    Sau khi build thành công, bạn có thể chạy ứng dụng bằng lệnh:
    ```bash
    mvn spring-boot:run
    ```
    Hoặc, bạn có thể chạy file JAR đã được build trong thư mục `target`:
    ```bash
    java -jar target/quanlybanhang-0.0.1-SNAPSHOT.jar 
    ```
    (Tên file JAR có thể thay đổi tùy theo version trong `pom.xml`).

4.  **Truy cập ứng dụng**:
    Mở trình duyệt web và truy cập vào địa chỉ: `http://localhost:8080`

### Cấu hình dữ liệu ban đầu (Quan trọng)
Dự án sử dụng các file JSON để lưu trữ dữ liệu. Các file này nằm trong thư mục `src/main/resources/static/`.
- `data-user/users.json`
- `data-product/product.json`
- `data-cart/cart.json`
- `data-buy/buy.json`
- `dang-giao/danggiao.json`
- `lichsugiaodich/history.json`
- `report/report.json`

Khi ứng dụng chạy, nó sẽ cố gắng đọc dữ liệu từ các file này trong `src/main/resources/static/` và đồng bộ sang `target/classes/static/` (đối với `product.json`, `users.json`, `buy.json`). Nếu các file này không tồn tại hoặc rỗng ở `src`, ứng dụng có thể khởi tạo chúng với một mảng JSON rỗng `[]`.

**LƯU Ý QUAN TRỌNG**:
- Để ứng dụng hoạt động chính xác với đầy đủ chức năng (đặc biệt là admin và employee), bạn cần **tạo thủ công** các file JSON trên nếu chúng chưa có, hoặc đảm bảo chúng có cấu trúc đúng và có dữ liệu mẫu ban đầu.
- **Tài khoản Admin**: Mặc định, chức năng đăng nhập admin kiểm tra username "admin" và password "admin". Bạn cần đảm bảo file `users.json` *không* cần phải chứa tài khoản này, vì logic đăng nhập admin được hardcode riêng.
- **Tài khoản Employee**: Để đăng nhập với vai trò nhân viên, bạn cần tạo một user trong `users.json` với trường `"role": "EMPLOYEE"`. Ví dụ:
  ```json
  [
    {
      "username": "employee1",
      "password": "password123",
      "fullname": "Nhân Viên A",
      "email": "employee1@example.com",
      "ban": "true", 
      "role": "EMPLOYEE"
    }
  ]
  
    ```

- **Dữ liệu sản phẩm** : File product.json cần có dữ liệu sản phẩm để hiển thị trên trang chủ và các trang khác.
- **Thư mục ảnh sản phẩm** : Ảnh sản phẩm được quản lý bởi AdminProductController và lưu tại src/main/resources/static/data-product/image/. Khi thêm sản phẩm qua trang admin, ảnh sẽ được tải lên đây. Đảm bảo thư mục này tồn tại.


### cấu-trúc-thư-mục

```json5
quanlybanhang/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/project/quanlybanhang/
│   │   │       ├── Admin/                # Controllers và logic cho Admin
│   │   │       │   ├── admin.java
│   │   │       │   └── AdminProductController.java
│   │   │       ├── Buy/                  # Logic xử lý mua hàng
│   │   │       │   ├── BuyData.java
│   │   │       │   └── Buycontroller.java
│   │   │       ├── Cart/                 # Logic giỏ hàng
│   │   │       │   ├── cart.java
│   │   │       │   ├── Cartcontroller.java
│   │   │       │   ├── Cartutils.java
│   │   │       │   └── iteams.java
│   │   │       ├── Employee/             # Logic cho Nhân viên
│   │   │       │   ├── DangGiaoData.java
│   │   │       │   ├── EmployeeController.java
│   │   │       │   ├── EmployeeService.java
│   │   │       │   ├── ReportData.java
│   │   │       │   └── ReportService.java
│   │   │       ├── Home/                 # Controller cho trang chủ
│   │   │       │   └── Home.java
│   │   │       ├── Login/                # Controller cho đăng nhập
│   │   │       │   └── LoginController.java
│   │   │       ├── Order/                # Service quản lý đơn hàng (buy.json)
│   │   │       │   └── OrderService.java
│   │   │       ├── Product/              # Logic quản lý sản phẩm
│   │   │       │   ├── Product.java
│   │   │       │   ├── ProductApiController.java
│   │   │       │   ├── ProductController.java
│   │   │       │   ├── Productservice.java
│   │   │       │   ├── Variant.java
│   │   │       │   ├── colorprice.java
│   │   │       │   └── managerdataproduct.java (interface)
│   │   │       ├── Register/             # Controller cho đăng ký
│   │   │       │   └── RegisterController.java
│   │   │       ├── SalesHistory/         # Logic cho lịch sử bán hàng và doanh thu
│   │   │       │   ├── HistoryEntry.java
│   │   │       │   └── RevenueService.java
│   │   │       ├── Search/               # Controller cho tìm kiếm
│   │   │       │   └── SearchController.java
│   │   │       ├── User/                 # Logic quản lý người dùng
│   │   │       │   ├── User.java
│   │   │       │   ├── UserController.java
│   │   │       │   └── UserService.java
│   │   │       ├── Utils/                # Các lớp tiện ích
│   │   │       │   └── Numberutils.java
│   │   │       └── QuanlybanhangApplication.java # Lớp chính của Spring Boot
│   │   └── resources/
│   │       ├── static/
│   │       │   ├── css/                  # Các file CSS
│   │       │   │   ├── admin.css
│   │       │   │   ├── cart.css
│   │       │   │   ├── genaral.css
│   │       │   │   ├── product.css
│   │       │   │   └── user-info.css
│   │       │   ├── data-buy/
│   │       │   │   └── buy.json          # Dữ liệu đơn hàng
│   │       │   ├── data-cart/
│   │       │   │   └── cart.json         # Dữ liệu giỏ hàng
│   │       │   ├── data-product/
│   │       │   │   ├── image/            # Thư mục chứa ảnh sản phẩm
│   │       │   │   └── product.json      # Dữ liệu sản phẩm
│   │       │   ├── data-user/
│   │       │   │   └── users.json        # Dữ liệu người dùng
│   │       │   ├── dang-giao/
│   │       │   │   └── danggiao.json     # Dữ liệu đơn hàng đang giao
│   │       │   ├── js/                   # Các file JavaScript
│   │       │   │   ├── home.js
│   │       │   │   └── search.js
│   │       │   ├── lichsugiaodich/
│   │       │   │   └── history.json      # Lịch sử giao dịch
│   │       │   └── report/
│   │       │       └── report.json       # Dữ liệu report
│   │       ├── templates/
│   │       │   └── html/                 # Các file HTML Thymeleaf
│   │       │       ├── admin.html
│   │       │       ├── cart.html
│   │       │       ├── employer.html
│   │       │       ├── home.html
│   │       │       ├── login.html
│   │       │       ├── product.html
│   │       │       ├── register.html
│   │       │       ├── selectproduct.html
│   │       │       └── user-info.html
│   │       └── application.properties    # File cấu hình Spring Boot
│   └── test/
│       └── java/
│           └── com/project/quanlybanhang/
│               └── QuanlybanhangApplicationTests.java # Lớp test
├── pom.xml                               # File cấu hình Maven
└── README.md                             # File này
```

# Quản Lý Bán Hàng - Spring Boot Project

Dự án xây dựng hệ thống quản lý bán hàng đơn giản sử dụng Spring Boot. Ứng dụng hỗ trợ các chức năng cơ bản cho **Admin**, **Nhân viên**, và **Khách hàng**, với dữ liệu lưu trữ dưới dạng JSON.

## 🚀 Chức năng chính

### 👤 Người dùng
- Đăng ký / Đăng nhập
- Xem và chỉnh sửa thông tin cá nhân
- Tìm kiếm sản phẩm
- Thêm sản phẩm vào giỏ hàng và tiến hành mua

### 🛒 Giỏ hàng
- Lưu sản phẩm đã chọn
- Tính tổng tiền
- Mua hàng từ giỏ

### 📦 Sản phẩm
- Xem chi tiết sản phẩm
- Quản lý sản phẩm (Admin)
- Đa dạng biến thể (màu sắc, giá...)

### 👨‍💼 Nhân viên
- Quản lý đơn hàng đang giao
- Xem báo cáo bán hàng, doanh thu

### 👑 Quản trị (Admin)
- Quản lý sản phẩm
- Xem danh sách đơn hàng
- Quản lý người dùng

# Hướng dẫn sử dụng

## Đăng nhập

- Truy cập `/login` để vào trang đăng nhập.
- **Admin**: Sử dụng username `admin` và password `admin`.
- **Nhân viên**: Sử dụng tài khoản có role là `EMPLOYEE` trong `users.json`.
- **Khách hàng**: Sử dụng tài khoản đã đăng ký hoặc đăng ký tài khoản mới tại `/register`.

---

## Trang chủ

- Hiển thị danh sách sản phẩm.
- Cho phép tìm kiếm sản phẩm theo `tags`.
- Lọc sản phẩm theo danh mục.
- **Popup yêu cầu đăng nhập** nếu người dùng chưa đăng nhập mà cố truy cập các tính năng cần xác thực (ví dụ: xem giỏ hàng từ trang chủ nếu chưa đăng nhập).

---

## Trang sản phẩm 

- Hiển thị chi tiết thông tin sản phẩm, bao gồm:
    - Các phiên bản
    - Màu sắc
    - Giá
- Cho phép:
    - Thêm sản phẩm vào giỏ hàng
    - Đặt hàng trực tiếp

---

## Giỏ hàng 

- Hiển thị các sản phẩm đã thêm vào giỏ.
- Cho phép:
    - Thay đổi số lượng
    - Xóa sản phẩm
- Tiến hành đặt hàng (yêu cầu nhập địa chỉ, số điện thoại).
- **Nếu chưa đăng nhập**, sẽ hiển thị popup yêu cầu đăng nhập.

---

## Trang cá nhân

- Hiển thị thông tin cá nhân của người dùng đã đăng nhập.
- Cho phép cập nhật:
    - Họ tên
    - Email
    - Mật khẩu
- Có nút đăng xuất.

---

## Trang quản trị (Admin) 

### Chỉnh sửa sản phẩm

- Xem danh sách sản phẩm.
- Chọn sản phẩm để sửa.
- Form sửa chi tiết sản phẩm:
    - Tên
    - Thương hiệu
    - Danh mục
    - Mô tả
    - Đánh giá
    - Tags

### Quản lý các phiên bản (variants)

- Thêm, sửa (ảnh, bộ nhớ, RAM, thông số kỹ thuật), xóa.

### Quản lý màu sắc & giá (colorprices)

- Thêm, sửa, xóa từng phiên bản.

### Upload ảnh

- Upload ảnh cho từng phiên bản.

### Thêm sản phẩm

- Form thêm sản phẩm mới với các thông tin tương tự như form sửa.

### Thông tin người dùng

- Xem danh sách tất cả người dùng (`customers`, `employees`).
- Sửa thông tin người dùng:
    - Họ tên
    - Email
    - Trạng thái (cấm/không cấm)
    - Vai trò (`CUSTOMER`, `EMPLOYEE`)
- Xóa người dùng (trừ tài khoản admin).

### Hàng đang đặt

- Xem danh sách các đơn hàng khách hàng đã xác nhận mua (`buy.json`).
- Hiển thị chi tiết từng đơn hàng.

### Doanh thu

- Xem tổng doanh thu từ trước đến nay.
- Lọc và xem doanh thu theo từng tháng của một năm cụ thể.
- Biểu đồ cột hiển thị doanh thu theo tháng.

### Thông báo report

- Xem danh sách các đơn hàng bị nhân viên report.
- Xem chi tiết đơn hàng bị report.
- Cho phép xóa tất cả các report.

---

## Trang nhân viên (Employee) 

### Danh sách đơn hàng

- Hiển thị đơn hàng:
    - **"Mới"** (chưa ai nhận, từ `buy.json`)
    - **"Đang giao"** (đã được nhân viên hiện tại nhận, từ `danggiao.json`)

### Hành động với đơn hàng

- **Nhận hàng**:
    - Dành cho đơn "Mới"
    - Nhấn "Nhận hàng" -> đơn chuyển sang "Đang giao", lưu vào `danggiao.json`
- **Giao thành công**:
    - Dành cho đơn "Đang giao"
    - Nhấn "Giao thành công" -> đơn chuyển vào `history.json`, xóa khỏi `danggiao.json` và `buy.json`
- **Report đơn hàng**:
    - Đối với cả đơn "Mới" và "Đang giao"
    - Popup yêu cầu nhập lý do
    - Ghi vào `report.json`, xóa khỏi `buy.json` và `danggiao.json` (nếu có)

### Thông tin nhân viên

- Hiển thị:
    - Tên
    - Username của nhân viên đang đăng nhập
- Có nút đăng xuất
