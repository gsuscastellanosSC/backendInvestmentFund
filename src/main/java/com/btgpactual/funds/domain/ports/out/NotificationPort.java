package com.btgpactual.funds.domain.ports.out;

import com.btgpactual.funds.domain.model.Customer;
import com.btgpactual.funds.domain.model.Fund;
import reactor.core.publisher.Mono;

public interface NotificationPort {
    Mono<Void> send(Customer customer, String message, String notificationType);
}
