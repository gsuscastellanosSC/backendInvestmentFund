package com.btgpactual.funds.domain.ports.in;
import com.btgpactual.funds.domain.model.Transaction;
import reactor.core.publisher.Mono;

public interface CancelFundUseCase {
    Mono<Transaction> execute(String customerId, String fundId);
}
