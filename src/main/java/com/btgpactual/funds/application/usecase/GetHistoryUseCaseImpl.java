package com.btgpactual.funds.application.usecase;

import com.btgpactual.funds.domain.model.Transaction;
import com.btgpactual.funds.domain.ports.in.GetHistoryUseCase;
import com.btgpactual.funds.domain.ports.out.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.Comparator;

@RequiredArgsConstructor
public class GetHistoryUseCaseImpl implements GetHistoryUseCase {
    private final CustomerRepository customerRepository;

    @Override
    public Flux<Transaction> execute(String customerId) {
        return customerRepository.findTransactionsByCustomerId(customerId)
                .sort(Comparator.comparingLong(Transaction::getTimestamp).reversed())
                .switchIfEmpty(Flux.empty());
    }
}
