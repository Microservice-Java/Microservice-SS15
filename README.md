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

## Hướng dẫn chạy và kiểm thử

### Bài Tập 1
```bash
cd BaiTap1
./gradlew test
```
