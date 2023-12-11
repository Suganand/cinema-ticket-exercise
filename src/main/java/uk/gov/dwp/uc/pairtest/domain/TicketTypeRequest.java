package uk.gov.dwp.uc.pairtest.domain;

public record TicketTypeRequest(Type type, int noOfTickets) {

    public enum Type {
        ADULT, CHILD, INFANT
    }

}
