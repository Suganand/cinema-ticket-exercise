package uk.gov.dwp.uc.pairtest.domain;

public record TicketPurchaseRequest(long accountId, TicketTypeRequest[] ticketTypeRequests) {
}
