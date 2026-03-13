package com.btgpactual.funds.application.usecase;

import com.btgpactual.funds.domain.model.Customer;
import com.btgpactual.funds.domain.model.Fund;
import com.btgpactual.funds.domain.model.Transaction;
import com.btgpactual.funds.domain.ports.in.SubscribeToFundUseCase;
import com.btgpactual.funds.domain.ports.out.NotificationPort;
import com.btgpactual.funds.domain.service.FundValidationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import java.util.UUID;
import com.btgpactual.funds.domain.ports.out.CustomerRepository;
import com.btgpactual.funds.domain.ports.out.FundRepository;

@Slf4j
@RequiredArgsConstructor
public class SubscribeToFundUseCaseImpl implements SubscribeToFundUseCase {

    private final CustomerRepository customerRepository;
    private final FundRepository fundRepository;
    private final NotificationPort notificationPort;

    @Override
    public Mono<Transaction> execute(String customerId, String fundId, String notificationType) {
        return Mono.zip(
                        customerRepository.findById(customerId),
                        fundRepository.findById(fundId)
                )
                .flatMap(tuple -> {
                    Customer customer = tuple.getT1();
                    Fund fund = tuple.getT2();

                    FundValidationService.validateSubscription(customer, fund);

                    customer.subtractBalance(fund.getMinimumAmount());
                    customer.addSubscription(fund.getId());

                    Transaction transaction = Transaction.builder()
                            .id(UUID.randomUUID().toString())
                            .customerId(customer.getId())
                            .fundId(fund.getId())
                            .fundName(fund.getName())
                            .type("OPENING")
                            .amount(fund.getMinimumAmount())
                            .timestamp(System.currentTimeMillis())
                            .build();

                    return customerRepository.save(customer)
                            .then(customerRepository.saveTransaction(transaction))
                            .flatMap(savedTx ->
                                    notificationPort.send(customer, "Suscripción exitosa", notificationType)
                                            .onErrorResume(e -> {
                                                log.error("Error enviando notificación, pero la transacción se guardó");
                                                return Mono.empty();
                                            })
                                            .thenReturn(savedTx)
                            );
                });
    }
}
