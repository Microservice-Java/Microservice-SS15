package com.example.movie.model;

import java.util.List;

public class CinemaBookingRequest {

    private String cinemaBookingId;
    private String movieCode;
    private String showTime;
    private List<String> seatNumbers;
    private String customerEmail;
    private Double totalPrice;

    public CinemaBookingRequest() {
    }

    public CinemaBookingRequest(String cinemaBookingId, String movieCode, String showTime,
                                List<String> seatNumbers, String customerEmail, Double totalPrice) {
        this.cinemaBookingId = cinemaBookingId;
        this.movieCode = movieCode;
        this.showTime = showTime;
        this.seatNumbers = seatNumbers;
        this.customerEmail = customerEmail;
        this.totalPrice = totalPrice;
    }

    public String getCinemaBookingId() { return cinemaBookingId; }
    public void setCinemaBookingId(String cinemaBookingId) { this.cinemaBookingId = cinemaBookingId; }
    public String getMovieCode() { return movieCode; }
    public void setMovieCode(String movieCode) { this.movieCode = movieCode; }
    public String getShowTime() { return showTime; }
    public void setShowTime(String showTime) { this.showTime = showTime; }
    public List<String> getSeatNumbers() { return seatNumbers; }
    public void setSeatNumbers(List<String> seatNumbers) { this.seatNumbers = seatNumbers; }
    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }
    public Double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(Double totalPrice) { this.totalPrice = totalPrice; }
}
