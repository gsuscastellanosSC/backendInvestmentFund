package com.btgpactual.funds.domain.ports.in;
import com.btgpactual.funds.domain.model.Transaction;
import reactor.core.publisher.Flux;

public interface GetHistoryUseCase {
    Flux<Transaction> execute(String customerId);
}
