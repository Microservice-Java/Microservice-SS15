package com.storex.twopc.model;

public class BookingData {

    private String bookingId;
    private String trainCode;
    private String customerWalletId;
    private Double price;

    public BookingData() {
    }

    public BookingData(String bookingId, String trainCode, String customerWalletId, Double price) {
        this.bookingId = bookingId;
        this.trainCode = trainCode;
        this.customerWalletId = customerWalletId;
        this.price = price;
    }

    public String getBookingId() { return bookingId; }
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }
    public String getTrainCode() { return trainCode; }
    public void setTrainCode(String trainCode) { this.trainCode = trainCode; }
    public String getCustomerWalletId() { return customerWalletId; }
    public void setCustomerWalletId(String customerWalletId) { this.customerWalletId = customerWalletId; }
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    @Override
    public String toString() {
        return "BookingData{" +
                "bookingId='" + bookingId + '\'' +
                ", trainCode='" + trainCode + '\'' +
                ", customerWalletId='" + customerWalletId + '\'' +
                ", price=" + price +
                '}';
    }
}
