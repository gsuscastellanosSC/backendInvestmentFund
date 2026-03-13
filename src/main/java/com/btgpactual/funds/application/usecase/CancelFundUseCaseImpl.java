package com.btgpactual.funds.application.usecase;

import com.btgpactual.funds.domain.model.Transaction;
import com.btgpactual.funds.domain.ports.in.CancelFundUseCase;
import com.btgpactual.funds.domain.ports.out.CustomerRepository;
import com.btgpactual.funds.domain.ports.out.FundRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RequiredArgsConstructor
public class CancelFundUseCaseImpl implements CancelFundUseCase {

    private final CustomerRepository customerRepository;
    private final FundRepository fundRepository;

    @Override
    public Mono<Transaction> execute(String customerId, String fundId) {
        return Mono.zip(
                customerRepository.findById(customerId)
                        .switchIfEmpty(Mono.error(new RuntimeException("Cliente no encontrado"))),
                fundRepository.findById(fundId)
                        .switchIfEmpty(Mono.error(new RuntimeException("Fondo no encontrado")))
        ).flatMap(tuple -> {
            var customer = tuple.getT1();
            var fund = tuple.getT2();

            // Verificamos si realmente tiene la suscripción
            if (!customer.hasSubscription(fundId)) {
                return Mono.error(new RuntimeException("El cliente no está vinculado al fondo: " + fund.getName()));
            }

            // Regla de negocio: El valor se retorna al cliente
            customer.addBalance(fund.getMinimumAmount());
            customer.removeSubscription(fundId);

            Transaction transaction = Transaction.builder()
                    .id(UUID.randomUUID().toString())
                    .customerId(customerId)
                    .fundId(fundId)
                    .fundName(fund.getName())
                    .amount(fund.getMinimumAmount())
                    .type("CANCELLATION") // Sugerencia: Usa un nombre estándar para el historial
                    .timestamp(System.currentTimeMillis())
                    .build();

            return customerRepository.save(customer)
                    .then(customerRepository.saveTransaction(transaction))
                    .thenReturn(transaction);
        });
    }
}
