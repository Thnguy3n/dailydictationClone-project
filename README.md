# dailydictationClone-project
VIDEO DEMO: https://www.youtube.com/watch?v=ODp1OA0uQF8&list=PLRawx3CjGdI80lxda0ePITVyRPSelK6-I

# ABCSHOP – Hệ thống thương mại điện tử bán quần áo

Một nền tảng e-commerce cho quần áo, xây dựng với **Java Spring Boot**, giao diện **JSP + Sitemesh**, cơ sở dữ liệu **MySQL**, hỗ trợ **xác thực/ủy quyền** (JWT/OAuth2) và các tiện ích như xuất **PDF hóa đơn** và **gửi email xác nhận**.

## 1) Tính năng chính

### Cho khách hàng
- Duyệt, tìm kiếm sản phẩm theo danh mục/giá; phân trang.
- Quản lý giỏ hàng: thêm/xóa/cập nhật số lượng.
- Đặt hàng (checkout) với nhiều phương thức thanh toán.
- Theo dõi lịch sử và trạng thái đơn hàng.
- Tài khoản cá nhân: đăng ký, đăng nhập (hỗ trợ **Google OAuth2**).

### Cho quản trị viên
- CRUD sản phẩm, danh mục, nhà cung cấp.
- Quản lý đơn hàng: cập nhật trạng thái.
- Quản lý người dùng và cấu hình phương thức vận chuyển.
- Báo cáo thống kê doanh thu/đơn hàng.
- Hệ thống thông báo nội bộ.

### Nâng cao
- Sinh **PDF hóa đơn** tự động (iTextPDF), **email** xác nhận đơn hàng/OTP.
- Hệ thống giảm giá (Strategy Pattern), thời gian bắt đầu/kết thúc.
- Thông báo real-time cho admin (event-driven).
- Bộ validator (điện thoại, email, mật khẩu…).

## 2) Kiến trúc & cấu trúc mã nguồn

- Mô hình **MVC** (JSP View ↔ Controller ↔ Model/JPA).
- Phân lớp: `controller/`, `service/`, `repository/`, `entity/`, `config/`, `security/`, `api/`, `utils/`.
- Áp dụng các **Design Patterns**: Strategy, State (trạng thái đơn hàng), Decorator (validation), Proxy, Facade (đặt hàng), Iterator (duyệt thông báo).

## 3) Công nghệ

- **Spring Boot 2.2.x**, **Spring Security**, **Spring Data JPA**, **OAuth2** (Google).
- **MySQL 8.x**; **Lombok**, **ModelMapper**, **iTextPDF**, **Spring Mail**.
- View: **JSP**, **Sitemesh**, **Bootstrap**, **jQuery**.
- Đóng gói: **Maven**, chạy bằng **Tomcat embedded**; **Docker** (tuỳ chọn).

## 4) CSDL (chính)

- `User`, `Role`, `Cart`, `Product`, `Category`, `Order`, `OrderItem`, `Payment`, `ShippingMethod`…
- Quan hệ nổi bật: `User 1–1 Cart`, `User 1–N Order`, `Order 1–N OrderItem`, `Order 1–1 Payment`, `Product N–1 Category`…

## 5) Bảo mật

- Form login + **Google OAuth2**.
- Phân quyền theo vai trò `USER/ADMIN` (RBAC).
- Mã hoá mật khẩu **BCrypt**, CSRF protection, quản lý session an toàn.
- Ràng buộc/validator: email duy nhất, số điện thoại hợp lệ, mật khẩu mạnh…

## 6) Cài đặt & chạy (Local)

### Yêu cầu
- JDK 17 (hoặc 11+ nếu dự án hiện dùng).
- Maven 3.8+
- MySQL 8.x (hoặc Docker)
- (Tuỳ chọn) Docker & Docker Compose.

### Chạy
```bash
# 1) Build
mvn clean package -DskipTests

# 2) Run
java -jar target/abcshop-*.jar
# hoặc: mvn spring-boot:run
```

### Docker (tuỳ chọn)
```bash
# MySQL bằng docker
docker run -d --name mysql-abcshop -e MYSQL_DATABASE=abcshop -e MYSQL_USER=abcshop -e MYSQL_PASSWORD=abcshop -e MYSQL_ROOT_PASSWORD=root -p 3306:3306 mysql:8

# App Dockerfile (ví dụ)
docker build -t abcshop-app .
docker run -p 8080:8080 --env-file .env --link mysql-abcshop abcshop-app
```

## 7) API Docs & Testing
- Tài liệu **OpenAPI/Swagger** tại `/swagger-ui.html` (nếu bật).
- **Postman collection** (thêm vào repo nếu có).
- Kiểm thử: **JUnit5 + Mockito** (gợi ý bổ sung Testcontainers cho MySQL).

## 8) Tài khoản mẫu (demo)
- `admin@abcshop.com` / `Admin@123` (ROLE_ADMIN) – cập nhật theo seed.
- `user@abcshop.com` / `User@123` (ROLE_USER).

## 9) Lộ trình/roadmap
- Nâng cấp Spring Boot 3.x, tách module API/ADMIN.
- Bổ sung caching, search toàn văn, giỏ hàng Redis.
- CI/CD GitHub Actions; triển khai Docker Compose/Swarm/K8s.
