package com.btgpactual.funds.application.usecase;

import com.btgpactual.funds.domain.model.Customer;
import com.btgpactual.funds.domain.model.Fund;
import com.btgpactual.funds.domain.ports.out.CustomerRepository;
import com.btgpactual.funds.domain.ports.out.FundRepository;
import com.btgpactual.funds.domain.ports.out.NotificationPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.HashSet;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class SubscribeToFundUseCaseTest {

    private CustomerRepository customerRepository;
    private FundRepository fundRepository;
    private NotificationPort notificationPort;
    private SubscribeToFundUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        customerRepository = Mockito.mock(CustomerRepository.class);
        fundRepository = Mockito.mock(FundRepository.class);
        notificationPort = Mockito.mock(NotificationPort.class);
        useCase = new SubscribeToFundUseCaseImpl(customerRepository, fundRepository, notificationPort);
    }

    @Test
    void shouldThrowExceptionWhenBalanceIsInsufficient() {
        // Arrange
        String customerId = "jesus.castellanos@example.com";
        String fundId = "1";

        Customer mockCustomer = Customer.builder()
                .id(customerId)
                .balance(new BigDecimal("50000")) // Saldo menor
                .activeSubscriptions(new HashSet<>())
                .build();

        Fund mockFund = Fund.builder()
                .id(fundId)
                .name("FPV_BTG_PACTUAL_RECAUDADORA")
                .minimumAmount(new BigDecimal("75000")) // Monto mayor
                .build();

        when(customerRepository.findById(customerId)).thenReturn(Mono.just(mockCustomer));
        when(fundRepository.findById(fundId)).thenReturn(Mono.just(mockFund));

        // Act & Assert
        StepVerifier.create(useCase.execute(customerId, fundId, "EMAIL"))
                .expectErrorMatches(throwable ->
                        // Verificamos el mensaje que definimos en el FundValidationService
                        throwable.getMessage().contains("No tiene saldo disponible"))
                .verify();
    }

    @Test
    void shouldSubscribeSuccessfullyWhenBalanceIsEnough() {
        // Arrange
        String customerId = "jesus.castellanos@example.com";
        String fundId = "1";
        String notificationType = "SMS";
        BigDecimal minAmount = new BigDecimal("75000");

        Customer mockCustomer = Customer.builder()
                .id(customerId)
                .balance(new BigDecimal("500000"))
                .activeSubscriptions(new HashSet<>())
                .build();

        Fund mockFund = Fund.builder()
                .id(fundId)
                .name("FPV_BTG_PACTUAL_RECAUDADORA")
                .minimumAmount(minAmount)
                .build();

        when(customerRepository.findById(customerId)).thenReturn(Mono.just(mockCustomer));
        when(fundRepository.findById(fundId)).thenReturn(Mono.just(mockFund));
        when(customerRepository.save(any())).thenReturn(Mono.just(mockCustomer));
        when(customerRepository.saveTransaction(any())).thenAnswer(i -> Mono.just(i.getArgument(0)));
        when(notificationPort.send(any(), anyString(), anyString())).thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(useCase.execute(customerId, fundId, notificationType))
                .expectNextMatches(transaction ->
                        // IMPORTANTE: BigDecimal se compara con compareTo o declarando el mismo valor exacto
                        transaction.getAmount().compareTo(minAmount) == 0 &&
                                transaction.getType().equals("OPENING"))
                .verifyComplete();
    }
}
