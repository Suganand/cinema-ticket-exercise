package uk.gov.dwp.uc.pairtest;

import java.util.Arrays;

import lombok.AllArgsConstructor;
import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.TicketPurchaseRequest;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest.Type;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

@AllArgsConstructor
public class TicketServiceImpl implements TicketService {

    private TicketPurchaseRequestValidator ticketPurchaseRequestValidator;
    private SeatReservationService seatReservationService;
    private TicketPaymentService ticketPaymentService;

    /**
     * Should only have private methods other than the one below.
     */
    @Override
    public void purchaseTickets(TicketPurchaseRequest ticketPurchaseRequest) throws InvalidPurchaseException {

        ticketPurchaseRequestValidator.validate(ticketPurchaseRequest);

        seatReservationService.reserveSeat(ticketPurchaseRequest.accountId(),
                calculateTotalSeatsToAllocate(ticketPurchaseRequest));

        ticketPaymentService.makePayment(ticketPurchaseRequest.accountId(),
                calculateTotalCost(ticketPurchaseRequest));

    }

    private int calculateTotalCost(TicketPurchaseRequest ticketPurchaseRequest) {
        return Arrays.stream(ticketPurchaseRequest.ticketTypeRequests())
                .map((r) -> {
                    if (r.type() == Type.ADULT) {
                        return r.noOfTickets() * 20;
                    } else if (r.type() == Type.CHILD) {
                        return r.noOfTickets() * 10;
                    } else
                        return 0;
                }).mapToInt(Integer::intValue)
                .sum();
    }

    private int calculateTotalSeatsToAllocate(TicketPurchaseRequest ticketPurchaseRequest) {

        return Arrays.stream(ticketPurchaseRequest.ticketTypeRequests())
                .filter((r) -> r.type() == Type.ADULT || r.type() == Type.CHILD)
                .map((r) -> r.noOfTickets()).mapToInt(Integer::intValue)
                .sum();
    }
}
