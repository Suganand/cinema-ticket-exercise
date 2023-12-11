package uk.gov.dwp.uc.pairtest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException.ValidationError.ACCOUNTID_CANNOT_BE_ZERO_OR_NEGATIVE;
import static uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException.ValidationError.CANNOT_PURCHASE_INFANT_OR_CHILD_TICKET_WITHOUT_ADULT_TICKET;
import static uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException.ValidationError.CANNOT_PURCHASE_MORE_INFANTS_THAN_ADULTS;
import static uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException.ValidationError.CANNOT_PURCHASE_MORE_THAN_20_TICKETS;
import static uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException.ValidationError.NOOFTICKETS_CANNOT_BE_ZERO_OR_NEGATIVE;
import static uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException.ValidationError.TICKETPURCHASEREQUEST_CANNOT_BE_NULL;
import static uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException.ValidationError.TICKETTYPEREQUESTS_CANNOT_BE_NULL_OR_EMPTY;

import java.util.List;
import java.util.stream.Stream;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import com.google.common.collect.Lists;

import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.TicketPurchaseRequest;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest.Type;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException.ValidationError;

public class TicketServiceImplTest {

        private TicketService ticketService;
        private SeatReservationService mockedSeatReservationService;
        private TicketPaymentService mockedTicketPaymentService;

        @BeforeEach
        void beforeEach() {
                mockedSeatReservationService = mock(SeatReservationService.class);
                mockedTicketPaymentService = mock(TicketPaymentService.class);
                ticketService = new TicketServiceImpl(new TicketPurchaseRequestValidator(),
                                mockedSeatReservationService,
                                mockedTicketPaymentService);
        }

        void assertThrowsExceptionWithExpectedErrors(TicketPurchaseRequest ticketPurchaseRequest,
                        List<ValidationError> expectedErrors) {
                InvalidPurchaseException exception = assertThrows(InvalidPurchaseException.class, () -> {
                        ticketService.purchaseTickets(ticketPurchaseRequest);
                });
                List<ValidationError> actualErrors = exception.getValidationErrors();

                assertThat(actualErrors, Matchers.containsInAnyOrder(expectedErrors.toArray()));
        }

        @Test
        void testInputIsNull() {
                assertThrowsExceptionWithExpectedErrors(null, Lists.newArrayList(TICKETPURCHASEREQUEST_CANNOT_BE_NULL));
        }

        @ParameterizedTest
        @ValueSource(ints = { 0, -1 })
        void testAccountIdIsNotValid(int accountId) {
                assertThrowsExceptionWithExpectedErrors(new TicketPurchaseRequest(accountId,
                                new TicketTypeRequest[] { new TicketTypeRequest(Type.ADULT, 1) }),
                                Lists.newArrayList(ACCOUNTID_CANNOT_BE_ZERO_OR_NEGATIVE));
        }

        @ParameterizedTest
        @NullSource
        @EmptySource
        void testTicketTypeRequestsIsNotValid(
                        TicketTypeRequest[] ticketTypeRequests) {
                assertThrowsExceptionWithExpectedErrors(new TicketPurchaseRequest(1,
                                ticketTypeRequests),
                                Lists.newArrayList(TICKETTYPEREQUESTS_CANNOT_BE_NULL_OR_EMPTY));
        }

        @ParameterizedTest
        @ValueSource(ints = { 0, -1 })
        void testNoOfTicketsIsNotValid(int noOfTickets) {
                assertThrowsExceptionWithExpectedErrors(new TicketPurchaseRequest(1,
                                new TicketTypeRequest[] { new TicketTypeRequest(Type.ADULT, noOfTickets) }),
                                Lists.newArrayList(NOOFTICKETS_CANNOT_BE_ZERO_OR_NEGATIVE));
        }

        @Test
        void testNoOfTicketsInATypeIsMoreThan20() {
                assertThrowsExceptionWithExpectedErrors(new TicketPurchaseRequest(1,
                                new TicketTypeRequest[] { new TicketTypeRequest(Type.ADULT, 21) }),
                                Lists.newArrayList(CANNOT_PURCHASE_MORE_THAN_20_TICKETS));
        }

        @Test
        void testTotalNumberOfTicketsOnlyInSumOfAllTheTypesIsMoreThan20() {
                assertThrowsExceptionWithExpectedErrors(new TicketPurchaseRequest(1,
                                new TicketTypeRequest[] { new TicketTypeRequest(Type.ADULT, 7),
                                                new TicketTypeRequest(Type.INFANT, 7),
                                                new TicketTypeRequest(Type.CHILD, 7) }),
                                Lists.newArrayList(CANNOT_PURCHASE_MORE_THAN_20_TICKETS));
        }

        @Test
        void testInfantTicketIsRequestedWithoutAdultTicket() {
                assertThrowsExceptionWithExpectedErrors(new TicketPurchaseRequest(1,
                                new TicketTypeRequest[] { new TicketTypeRequest(Type.INFANT, 1) }),
                                Lists.newArrayList(CANNOT_PURCHASE_INFANT_OR_CHILD_TICKET_WITHOUT_ADULT_TICKET));
        }

        @Test
        void testChildTicketIsRequestedWithoutAdultTicket() {
                assertThrowsExceptionWithExpectedErrors(new TicketPurchaseRequest(1,
                                new TicketTypeRequest[] { new TicketTypeRequest(Type.CHILD, 1) }),
                                Lists.newArrayList(CANNOT_PURCHASE_INFANT_OR_CHILD_TICKET_WITHOUT_ADULT_TICKET));
        }

        @Test
        void testInfantAndChildTicketsAreRequestedWithoutAdultTicket() {
                assertThrowsExceptionWithExpectedErrors(new TicketPurchaseRequest(1,
                                new TicketTypeRequest[] { new TicketTypeRequest(Type.INFANT, 1),
                                                new TicketTypeRequest(Type.CHILD, 1) }),
                                Lists.newArrayList(CANNOT_PURCHASE_INFANT_OR_CHILD_TICKET_WITHOUT_ADULT_TICKET));
        }

        @Test
        void testMoreInfantsThanAdults() {
                assertThrowsExceptionWithExpectedErrors(new TicketPurchaseRequest(1,
                                new TicketTypeRequest[] { new TicketTypeRequest(Type.ADULT, 1),
                                                new TicketTypeRequest(Type.INFANT, 2) }),
                                Lists.newArrayList(CANNOT_PURCHASE_MORE_INFANTS_THAN_ADULTS));
        }

        @ParameterizedTest
        @ValueSource(ints = { 0, -1 })
        void testAccoundIdIsNotValidAndTicketTypeRequestsIsNull(int accountId) {
                assertThrowsExceptionWithExpectedErrors(new TicketPurchaseRequest(accountId, null),
                                Lists.newArrayList(TICKETTYPEREQUESTS_CANNOT_BE_NULL_OR_EMPTY,
                                                ACCOUNTID_CANNOT_BE_ZERO_OR_NEGATIVE));
        }

        @ParameterizedTest
        @ValueSource(ints = { 0, -1 })
        void testAccoundIdIsNotValidAndTicketTypeRequestsIsEmpty(int accountId) {
                assertThrowsExceptionWithExpectedErrors(
                                new TicketPurchaseRequest(accountId, new TicketTypeRequest[] {}),
                                Lists.newArrayList(TICKETTYPEREQUESTS_CANNOT_BE_NULL_OR_EMPTY,
                                                ACCOUNTID_CANNOT_BE_ZERO_OR_NEGATIVE));
        }

        private static Stream<Arguments> accountIdAndNoOfTickets() {
                return Stream.of(
                                arguments(0, 0),
                                arguments(0, -1),
                                arguments(-1, 0),
                                arguments(-1, -1));
        }

        @ParameterizedTest
        @MethodSource("accountIdAndNoOfTickets")
        void testAccoundIdIsNotValidAndNoOfTicketsIsNotValid(int accountId,
                        int noOfTickets) {
                assertThrowsExceptionWithExpectedErrors(new TicketPurchaseRequest(accountId,
                                new TicketTypeRequest[] { new TicketTypeRequest(Type.ADULT, noOfTickets) }),
                                Lists.newArrayList(NOOFTICKETS_CANNOT_BE_ZERO_OR_NEGATIVE,
                                                ACCOUNTID_CANNOT_BE_ZERO_OR_NEGATIVE));
        }

        @ParameterizedTest
        @ValueSource(ints = { 0, -1 })
        void testAccoundIdIsNotValidAndTotalNumberOfTicketsExceededInASingleType(
                        int accountId) {
                assertThrowsExceptionWithExpectedErrors(new TicketPurchaseRequest(accountId,
                                new TicketTypeRequest[] { new TicketTypeRequest(Type.ADULT, 21) }),
                                Lists.newArrayList(CANNOT_PURCHASE_MORE_THAN_20_TICKETS,
                                                ACCOUNTID_CANNOT_BE_ZERO_OR_NEGATIVE));
        }

        @ParameterizedTest
        @ValueSource(ints = { 0, -1 })
        void testAccoundIdIsNotValidAndTotalNumberOfTicketsExceededInMultipeTypes(
                        int accountId) {
                assertThrowsExceptionWithExpectedErrors(new TicketPurchaseRequest(accountId,
                                new TicketTypeRequest[] { new TicketTypeRequest(Type.ADULT, 7),
                                                new TicketTypeRequest(Type.CHILD, 7),
                                                new TicketTypeRequest(Type.INFANT, 7) }),
                                Lists.newArrayList(CANNOT_PURCHASE_MORE_THAN_20_TICKETS,
                                                ACCOUNTID_CANNOT_BE_ZERO_OR_NEGATIVE));
        }

        @ParameterizedTest
        @ValueSource(ints = { 0, -1 })
        void testAccoundIdIsNotValidAndInfantWithoutAdult(int accountId) {
                assertThrowsExceptionWithExpectedErrors(new TicketPurchaseRequest(accountId,
                                new TicketTypeRequest[] { new TicketTypeRequest(Type.INFANT, 1) }),
                                Lists.newArrayList(
                                                CANNOT_PURCHASE_INFANT_OR_CHILD_TICKET_WITHOUT_ADULT_TICKET,
                                                ACCOUNTID_CANNOT_BE_ZERO_OR_NEGATIVE));
        }

        @ParameterizedTest
        @ValueSource(ints = { 0, -1 })
        void testAccoundIdIsNotValidAndChildWithoutAdult(int accountId) {
                assertThrowsExceptionWithExpectedErrors(new TicketPurchaseRequest(accountId,
                                new TicketTypeRequest[] { new TicketTypeRequest(Type.CHILD, 1) }),
                                Lists.newArrayList(
                                                CANNOT_PURCHASE_INFANT_OR_CHILD_TICKET_WITHOUT_ADULT_TICKET,
                                                ACCOUNTID_CANNOT_BE_ZERO_OR_NEGATIVE));
        }

        @ParameterizedTest
        @ValueSource(ints = { 0, -1 })
        void testAccoundIdIsNotValidAndInfantOnlyAndTotalNumbersExceeded(
                        int accountId) {
                assertThrowsExceptionWithExpectedErrors(new TicketPurchaseRequest(accountId,
                                new TicketTypeRequest[] { new TicketTypeRequest(Type.INFANT, 21) }),
                                Lists.newArrayList(
                                                CANNOT_PURCHASE_INFANT_OR_CHILD_TICKET_WITHOUT_ADULT_TICKET,
                                                ACCOUNTID_CANNOT_BE_ZERO_OR_NEGATIVE,
                                                CANNOT_PURCHASE_MORE_THAN_20_TICKETS));
        }

        @ParameterizedTest
        @ValueSource(ints = { 0, -1 })
        void testAccoundIdIsNotValidAndChildOnlyAndTotalNumbersExceeded(
                        int accountId) {
                assertThrowsExceptionWithExpectedErrors(new TicketPurchaseRequest(accountId,
                                new TicketTypeRequest[] { new TicketTypeRequest(Type.CHILD, 21) }),
                                Lists.newArrayList(
                                                CANNOT_PURCHASE_INFANT_OR_CHILD_TICKET_WITHOUT_ADULT_TICKET,
                                                ACCOUNTID_CANNOT_BE_ZERO_OR_NEGATIVE,
                                                CANNOT_PURCHASE_MORE_THAN_20_TICKETS));
        }

        @ParameterizedTest
        @ValueSource(ints = { 0, -1 })
        void testAccoundIdIsNotValidAndInfantAndChildOnlyAndTotalNumbersExceeded(
                        int accountId) {
                assertThrowsExceptionWithExpectedErrors(new TicketPurchaseRequest(accountId,
                                new TicketTypeRequest[] { new TicketTypeRequest(Type.CHILD, 11),
                                                new TicketTypeRequest(Type.CHILD, 10) }),
                                Lists.newArrayList(
                                                CANNOT_PURCHASE_INFANT_OR_CHILD_TICKET_WITHOUT_ADULT_TICKET,
                                                ACCOUNTID_CANNOT_BE_ZERO_OR_NEGATIVE,
                                                CANNOT_PURCHASE_MORE_THAN_20_TICKETS));
        }

        @ParameterizedTest
        @ValueSource(ints = { 0, -1 })
        void testAccoundIdIsNotValidAndMoreInfantsThanAdultsAndTotalNumbersExceeded(
                        int accountId) {
                assertThrowsExceptionWithExpectedErrors(new TicketPurchaseRequest(accountId,
                                new TicketTypeRequest[] { new TicketTypeRequest(Type.ADULT, 10),
                                                new TicketTypeRequest(Type.INFANT, 11) }),
                                Lists.newArrayList(
                                                CANNOT_PURCHASE_MORE_INFANTS_THAN_ADULTS,
                                                ACCOUNTID_CANNOT_BE_ZERO_OR_NEGATIVE,
                                                CANNOT_PURCHASE_MORE_THAN_20_TICKETS));
        }

        @Test
        void testValidInputThenDoesNotThrowException() {
                assertDoesNotThrow(() -> {
                        ticketService.purchaseTickets(new TicketPurchaseRequest(1,
                                        new TicketTypeRequest[] { new TicketTypeRequest(Type.ADULT, 20) }));
                });
        }

        @Test
        void testInputIsValidThenTicketPaymentServiceAndSeatBookingServiceAreCalled() {
                ticketService.purchaseTickets(new TicketPurchaseRequest(1,
                                new TicketTypeRequest[] { new TicketTypeRequest(Type.ADULT, 4),
                                                new TicketTypeRequest(Type.CHILD, 2),
                                                new TicketTypeRequest(Type.INFANT, 3) }));

                verify(mockedSeatReservationService).reserveSeat(1, 6);
                verify(mockedTicketPaymentService).makePayment(1, 100);
        }

        @Test
        void testInputIsNotValidThenTicketPaymentServiceAndSeatBookingServiceAreNotCalled() {
                assertThrowsExceptionWithExpectedErrors(new TicketPurchaseRequest(1,
                                new TicketTypeRequest[] { new TicketTypeRequest(Type.ADULT, 2),
                                                new TicketTypeRequest(Type.INFANT, 3) }),
                                Lists.newArrayList(CANNOT_PURCHASE_MORE_INFANTS_THAN_ADULTS));

                verifyNoInteractions(mockedTicketPaymentService);
                verifyNoInteractions(mockedSeatReservationService);
        }

}
