package com.example.orchestrator.model;

import jakarta.persistence.*;

@Entity
@Table(name = "booking_transactions")
public class BookingTransaction {

    @Id
    private String bookingId;
    private String concertCode;
    private String customerId;
    private String customerEmail;
    private Integer ticketQuantity;
    private Double amount;

    @Enumerated(EnumType.STRING)
    private BookingState currentState;

    private String lastError;

    public BookingTransaction() {
    }

    public BookingTransaction(String bookingId, String concertCode, String customerId, String customerEmail, Integer ticketQuantity, Double amount) {
        this.bookingId = bookingId;
        this.concertCode = concertCode;
        this.customerId = customerId;
        this.customerEmail = customerEmail;
        this.ticketQuantity = ticketQuantity;
        this.amount = amount;
        this.currentState = BookingState.INITIATED;
    }

    public String getBookingId() { return bookingId; }
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }
    public String getConcertCode() { return concertCode; }
    public void setConcertCode(String concertCode) { this.concertCode = concertCode; }
    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }
    public Integer getTicketQuantity() { return ticketQuantity; }
    public void setTicketQuantity(Integer ticketQuantity) { this.ticketQuantity = ticketQuantity; }
    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
    public BookingState getCurrentState() { return currentState; }
    public void setCurrentState(BookingState currentState) { this.currentState = currentState; }
    public String getLastError() { return lastError; }
    public void setLastError(String lastError) { this.lastError = lastError; }
}
