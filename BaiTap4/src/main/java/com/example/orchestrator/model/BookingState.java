package com.example.orchestrator.model;

public enum BookingState {
    INITIATED,          // Khởi tạo
    PAYMENT_PENDING,    // Đang xử lý thanh toán
    PAYMENT_COMPLETED,  // Thanh toán thành công
    SEAT_RESERVING,     // Đang giữ chỗ
    BOOKING_CONFIRMED,  // Đặt vé hoàn tất
    CANCELLED           // Hủy bỏ
}
