package uk.gov.dwp.uc.pairtest.exception;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InvalidPurchaseException extends RuntimeException {
    List<ValidationError> validationErrors = new ArrayList<ValidationError>();

    public enum ValidationError {
        TICKETPURCHASEREQUEST_CANNOT_BE_NULL,
        TICKETTYPEREQUESTS_CANNOT_BE_NULL_OR_EMPTY,
        ACCOUNTID_CANNOT_BE_ZERO_OR_NEGATIVE,
        NOOFTICKETS_CANNOT_BE_ZERO_OR_NEGATIVE,
        CANNOT_PURCHASE_MORE_THAN_20_TICKETS,
        CANNOT_PURCHASE_INFANT_OR_CHILD_TICKET_WITHOUT_ADULT_TICKET,
        CANNOT_PURCHASE_MORE_INFANTS_THAN_ADULTS
    }

}
