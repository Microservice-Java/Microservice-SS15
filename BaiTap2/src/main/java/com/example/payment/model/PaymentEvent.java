package com.example.payment.model;

public class PaymentEvent {

    private String cinemaBookingId;
    private Double totalPrice;
    private String paymentStatus; // "SUCCESS"

    public PaymentEvent() {
    }

    public PaymentEvent(String cinemaBookingId, Double totalPrice, String paymentStatus) {
        this.cinemaBookingId = cinemaBookingId;
        this.totalPrice = totalPrice;
        this.paymentStatus = paymentStatus;
    }

    public String getCinemaBookingId() { return cinemaBookingId; }
    public void setCinemaBookingId(String cinemaBookingId) { this.cinemaBookingId = cinemaBookingId; }
    public Double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(Double totalPrice) { this.totalPrice = totalPrice; }
    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
}
