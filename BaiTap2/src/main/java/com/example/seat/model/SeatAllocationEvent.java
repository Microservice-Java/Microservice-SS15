package com.example.seat.model;

import java.util.List;

public class SeatAllocationEvent {

    private String cinemaBookingId;
    private String movieCode;
    private List<String> seatNumbers;
    private String customerEmail;
    private Double totalPrice;
    private String seatStatus; // "RESERVED"

    public SeatAllocationEvent() {
    }

    public SeatAllocationEvent(String cinemaBookingId, String movieCode, List<String> seatNumbers,
                               String customerEmail, Double totalPrice, String seatStatus) {
        this.cinemaBookingId = cinemaBookingId;
        this.movieCode = movieCode;
        this.seatNumbers = seatNumbers;
        this.customerEmail = customerEmail;
        this.totalPrice = totalPrice;
        this.seatStatus = seatStatus;
    }

    public String getCinemaBookingId() { return cinemaBookingId; }
    public void setCinemaBookingId(String cinemaBookingId) { this.cinemaBookingId = cinemaBookingId; }
    public String getMovieCode() { return movieCode; }
    public void setMovieCode(String movieCode) { this.movieCode = movieCode; }
    public List<String> getSeatNumbers() { return seatNumbers; }
    public void setSeatNumbers(List<String> seatNumbers) { this.seatNumbers = seatNumbers; }
    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }
    public Double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(Double totalPrice) { this.totalPrice = totalPrice; }
    public String getSeatStatus() { return seatStatus; }
    public void setSeatStatus(String seatStatus) { this.seatStatus = seatStatus; }
}
