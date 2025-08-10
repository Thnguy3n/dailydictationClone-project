# dailydictationClone-project
VIDEO DEMO: https://www.youtube.com/watch?v=ODp1OA0uQF8&list=PLRawx3CjGdI80lxda0ePITVyRPSelK6-I

# English Listening Practice System (Daily Dictation Clone)

Nền tảng luyện nghe/chính tả tiếng Anh theo **challenges**; frontend **Flutter**, backend **Spring Boot microservices**; hạ tầng **PostgreSQL + Kafka + Firebase Storage**. Hỗ trợ **đăng nhập Google, JWT**, theo dõi tiến độ học và **thanh toán QR** cho gói premium.

## 1) Kiến trúc tổng quan

Microservices: **API Gateway**, **Discovery (Eureka)**, **User Service**, **Audio Service**, **Payment Service**. Giao tiếp REST; xử lý bất đồng bộ qua **Kafka**. Lưu trữ media trên **Firebase Storage**; đóng gói bằng **Docker**.

```
root/
 ├─ gateway/
 ├─ discovery/
 ├─ services/
 │   ├─ user-service/
 │   ├─ audio-service/
 │   └─ payment-service/
 ├─ infra/ (docker-compose: postgres, kafka, zookeeper, firebase emulators?)
 └─ clients/ (flutter, reactjs)
```

## 2) Tính năng nổi bật

- **Learning**: Topics → Sections → Lessons → Challenges; nghe audio và nhập đáp án; nhiều tốc độ phát; theo dõi **progress** chi tiết.
- **Audio pipeline**: Upload audio → **AssemblyAI** transcribe → tạo từ/đáp án chấp nhận → **FFmpeg** cắt segment → lưu metadata + URL → phát trực tuyến.
- **Premium**: gói đăng ký; thanh toán QR; cập nhật trạng thái premium theo thời gian thực.
- **Auth**: Google Sign‑In; **JWT**; phân quyền tính năng premium.
- **Cross‑platform**: Flutter (mobile/web) & ReactJS (web client).

## 3) Công nghệ

- **Backend**: Java 21, Spring Boot 3.x, Spring Cloud (Gateway, Eureka, LoadBalancer), Spring Security (JWT), Spring Data JPA (PostgreSQL), **Kafka**, Firebase Admin SDK, **FFmpeg**, AssemblyAI.
- **Frontend**: Flutter 3.x (Riverpod, Dio/HTTP, AudioPlayers), ReactJS.
- **DevOps**: Docker & Docker Compose, Maven

## 4) Luồng nghiệp vụ chính

1. **Tạo lesson/challenge**  
   Upload audio → gửi tới AssemblyAI → xử lý kết quả → tạo word data & acceptable answers → cắt **segments** bằng FFmpeg → lưu trữ (Firebase) & metadata (PostgreSQL).

2. **Học & chấm điểm**  
   Ứng dụng tải segments, phát ở tốc độ tuỳ chọn, người dùng nhập đáp án, hệ thống kiểm tra và phản hồi ngay; ghi lại tiến độ học.

3. **Thanh toán**  
   Tạo QR cho gói premium, theo dõi trạng thái **real‑time** và cập nhật quyền truy cập premium khi thành công.

## 5) Cài đặt & chạy (Local)

### Yêu cầu
- JDK 21, Maven 3.9+
- Docker & Docker Compose
- Tài khoản Firebase + khóa dịch vụ 
- API key AssemblyAI

### Cấu hình biến môi trường 
Tạo file `.env` cho mỗi service:

### Build & Run
```bash
# Run discovery & gateway
mvn -f discovery/pom.xml spring-boot:run
mvn -f gateway/pom.xml spring-boot:run

# Run services
mvn -f services/user-service/pom.xml spring-boot:run
mvn -f services/audio-service/pom.xml spring-boot:run
mvn -f services/payment-service/pom.xml spring-boot:run
```



