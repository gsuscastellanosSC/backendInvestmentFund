package com.btgpactual.funds.domain.ports.in;
import com.btgpactual.funds.domain.model.*;
import reactor.core.publisher.Mono;

public interface SubscribeToFundUseCase {
    Mono<Transaction> execute(String customerId, String fundId, String notificationType);
}
