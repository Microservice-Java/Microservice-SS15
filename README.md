# Microservice SS15 - Two-Phase Commit (2PC) & Distributed Transactions

Repository lưu trữ bài tập thực hành về **Two-Phase Commit (2PC)** và **Giao dịch phân tán (Distributed Transactions)** trong kiến trúc Microservices.

## Danh sách bài tập

### [Bài Tập 1: Mô Phỏng Giao Thức 2PC Cho Giao Dịch Đặt Vé](./BaiTap1)
- **Mục tiêu**: Tái hiện đầy đủ quy trình 2 pha Prepare và Commit/Rollback giữa `TwoPhaseCommitCoordinator`, `TrainService` (Quản lý chỗ ngồi vé tàu) và `WalletService` (Quản lý tiền trong ví điện tử).
- **Giải pháp**:
  - Phase 1 (Prepare): Coordinator phát lệnh `PREPARE` tới từng service. Service kiểm tra tồn tài nguyên và trả về phiếu bầu `VOTE_COMMIT` hoặc `VOTE_ABORT`.
  - Phase 2 (Decision): Nếu 100% phiếu bầu là `VOTE_COMMIT` $\rightarrow$ Phát `COMMIT` cho tất cả service. Ngược lại, nếu có bất kỳ phiếu bầu nào là `VOTE_ABORT` $\rightarrow$ Phát `ROLLBACK` cho tất cả service để giải phóng tài nguyên.
- **Báo cáo chi tiết & Pseudocode**: [BaoCao_BaiTap1.md](./BaiTap1/BaoCao_BaiTap1.md)

---

### [Bài Tập 2: Xây Dựng "Bản Đồ Dẫn Đường" Với Correlation ID & Tracing](./BaiTap2)
- **Mục tiêu**: Thiết lập cơ chế sinh và truyền `Correlation ID` xuyên suốt qua Kafka Record Headers giữa 3 Microservices (`MovieBookingService` $\rightarrow$ `SeatAllocationService` $\rightarrow$ `PaymentService`) trong giao dịch đặt vé xem phim trực tuyến.
- **Giải pháp**:
  - Sinh UUID `correlationId` tại `MovieBookingService` và đính kèm vào Kafka Header (không xâm nhập payload JSON).
  - Trích xuất `correlationId` từ Kafka Header tại `SeatAllocationService` & `PaymentService` để ghi log tracing tập trung.
  - Kế thừa và tiếp tục truyền `correlationId` ở header các sự kiện kế tiếp trong chuỗi Choreography Saga.
- **Báo cáo chi tiết**: [BaoCao_BaiTap2.md](./BaiTap2/BaoCao_BaiTap2.md)

---

### [Bài Tập 3: Triển Khai Choreography Saga Với Apache Kafka](./BaiTap3)
- **Mục tiêu**: Thiết lập luồng Choreography Saga giao tiếp 100% qua Kafka Topics giữa 3 dịch vụ (`ConcertBookingService` $\rightarrow$ `SeatAssignmentService` $\rightarrow$ `NotificationService`) cho hệ thống đặt vé xem hòa nhạc mà không sử dụng REST API giữa các service.
- **Giải pháp**:
  - `ConcertBookingService` phát `ConcertBookingEvent` (`correlationId: "CONCERT-2024-999"`) lên topic `concert-events`.
  - `SeatAssignmentService` tiêu thụ sự kiện, giữ chỗ ghế, và tiếp tục phát `SeatReservedEvent` mang cùng `correlationId` lên topic `seat-events`.
  - `NotificationService` tiêu thụ `SeatReservedEvent` và mô phỏng gửi email xác nhận.
- **Báo cáo chi tiết**: [BaoCao_BaiTap3.md](./BaiTap3/BaoCao_BaiTap3.md)

---

## Hướng dẫn chạy và kiểm thử

### Bài Tập 1
```bash
cd BaiTap1
./gradlew test
```

### Bài Tập 2
```bash
cd BaiTap2
./gradlew test
```

### Bài Tập 3
```bash
cd BaiTap3
./gradlew test
```
