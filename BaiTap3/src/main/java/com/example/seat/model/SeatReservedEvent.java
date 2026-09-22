package com.example.seat.model;

public class SeatReservedEvent {

    private String correlationId;
    private String customerEmail;
    private String concertCode;
    private Integer ticketQuantity;
    private String status;

    public SeatReservedEvent() {
    }

    public SeatReservedEvent(String correlationId, String customerEmail, String concertCode, Integer ticketQuantity, String status) {
        this.correlationId = correlationId;
        this.customerEmail = customerEmail;
        this.concertCode = concertCode;
        this.ticketQuantity = ticketQuantity;
        this.status = status;
    }

    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }
    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }
    public String getConcertCode() { return concertCode; }
    public void setConcertCode(String concertCode) { this.concertCode = concertCode; }
    public Integer getTicketQuantity() { return ticketQuantity; }
    public void setTicketQuantity(Integer ticketQuantity) { this.ticketQuantity = ticketQuantity; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
