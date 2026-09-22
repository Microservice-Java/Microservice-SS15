package com.example.seat.model;

public class ConcertBookingEvent {

    private String correlationId;
    private String concertCode;
    private String customerEmail;
    private Integer ticketQuantity;

    public ConcertBookingEvent() {
    }

    public ConcertBookingEvent(String correlationId, String concertCode, String customerEmail, Integer ticketQuantity) {
        this.correlationId = correlationId;
        this.concertCode = concertCode;
        this.customerEmail = customerEmail;
        this.ticketQuantity = ticketQuantity;
    }

    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }
    public String getConcertCode() { return concertCode; }
    public void setConcertCode(String concertCode) { this.concertCode = concertCode; }
    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }
    public Integer getTicketQuantity() { return ticketQuantity; }
    public void setTicketQuantity(Integer ticketQuantity) { this.ticketQuantity = ticketQuantity; }
}
