package uk.gov.dwp.uc.pairtest;

import static uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest.Type.ADULT;
import static uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest.Type.INFANT;
import static uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException.ValidationError.ACCOUNTID_CANNOT_BE_ZERO_OR_NEGATIVE;
import static uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException.ValidationError.CANNOT_PURCHASE_INFANT_OR_CHILD_TICKET_WITHOUT_ADULT_TICKET;
import static uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException.ValidationError.CANNOT_PURCHASE_MORE_INFANTS_THAN_ADULTS;
import static uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException.ValidationError.CANNOT_PURCHASE_MORE_THAN_20_TICKETS;
import static uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException.ValidationError.NOOFTICKETS_CANNOT_BE_ZERO_OR_NEGATIVE;
import static uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException.ValidationError.TICKETPURCHASEREQUEST_CANNOT_BE_NULL;
import static uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException.ValidationError.TICKETTYPEREQUESTS_CANNOT_BE_NULL_OR_EMPTY;

import uk.gov.dwp.uc.pairtest.domain.TicketPurchaseRequest;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

public class TicketPurchaseRequestValidator {

    public void validate(TicketPurchaseRequest ticketPurchaseRequest) throws InvalidPurchaseException {
        if (ticketPurchaseRequest == null) {
            InvalidPurchaseException exception = new InvalidPurchaseException();
            exception.getValidationErrors().add(TICKETPURCHASEREQUEST_CANNOT_BE_NULL);
            throw exception;
        } else {
            InvalidPurchaseException exception = new InvalidPurchaseException();
            if (ticketPurchaseRequest.accountId() <= 0) {
                exception.getValidationErrors().add(ACCOUNTID_CANNOT_BE_ZERO_OR_NEGATIVE);
            }
            if (ticketPurchaseRequest.ticketTypeRequests() == null) {
                exception.getValidationErrors().add(TICKETTYPEREQUESTS_CANNOT_BE_NULL_OR_EMPTY);
            } else if (ticketPurchaseRequest.ticketTypeRequests().length == 0) {
                exception.getValidationErrors().add(TICKETTYPEREQUESTS_CANNOT_BE_NULL_OR_EMPTY);
            } else {
                int totalNumberOfTickets = 0;
                boolean isAdult = false;
                int totalAdults = 0;
                int totalInfants = 0;
                for (TicketTypeRequest ticketTypeRequest : ticketPurchaseRequest.ticketTypeRequests()) {
                    if (ticketTypeRequest.type().equals(ADULT)) {
                        isAdult = true;
                        totalAdults += ticketTypeRequest.noOfTickets();
                    }
                    if (ticketTypeRequest.type().equals(INFANT)) {
                        totalInfants += ticketTypeRequest.noOfTickets();
                    }
                    if (ticketTypeRequest.noOfTickets() <= 0) {
                        exception.getValidationErrors().add(NOOFTICKETS_CANNOT_BE_ZERO_OR_NEGATIVE);
                        totalNumberOfTickets = 0; // This is to ignore the other errors (below)
                        break;
                    } else {
                        totalNumberOfTickets += ticketTypeRequest.noOfTickets();
                    }
                }
                if (totalNumberOfTickets > 20) {
                    exception.getValidationErrors().add(CANNOT_PURCHASE_MORE_THAN_20_TICKETS);
                }
                if (totalNumberOfTickets > 0 && !isAdult) {
                    exception.getValidationErrors().add(CANNOT_PURCHASE_INFANT_OR_CHILD_TICKET_WITHOUT_ADULT_TICKET);
                } else if (totalNumberOfTickets > 0 && totalInfants > totalAdults) {
                    exception.getValidationErrors().add(CANNOT_PURCHASE_MORE_INFANTS_THAN_ADULTS);
                }
            }
            if (exception.getValidationErrors().size() > 0) {
                throw exception;
            }
        }
    }
}
