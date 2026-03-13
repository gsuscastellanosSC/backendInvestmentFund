#!/bin/bash

# Ajustado a la ruta que veo en tu captura (application.usecase)
PACKAGE_PATH="src/main/java/com/btgpactual/funds"
USECASE_PATH="$PACKAGE_PATH/application/usecase"
HANDLER_PATH="$PACKAGE_PATH/infrastructure/entrypoints/reactive/handler"

echo "🔧 Completando casos de uso faltantes..."

# 1. Crear el Caso de Uso de Historial (Punto 3 del reto)
cat <<EOF > "$USECASE_PATH/GetHistoryUseCaseImpl.java"
package com.btgpactual.funds.application.usecase;

import com.btgpactual.funds.domain.model.Transaction;
import com.btgpactual.funds.domain.ports.in.GetHistoryUseCase;
import com.btgpactual.funds.domain.ports.out.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
public class GetHistoryUseCaseImpl implements GetHistoryUseCase {
    private final CustomerRepository customerRepository;

    @Override
    public Flux<Transaction> execute(String customerId) {
        return customerRepository.findTransactionsByCustomerId(customerId)
                .sort((t1, t2) -> Long.compare(t2.getTimestamp(), t1.getTimestamp()));
    }
}
EOF

# 2. Actualizar el Handler para que reconozca los TRES casos de uso
cat <<EOF > "$HANDLER_PATH/FundHandler.java"
package com.btgpactual.funds.infrastructure.entrypoints.reactive.handler;

import com.btgpactual.funds.domain.model.SubscriptionRequest;
import com.btgpactual.funds.domain.ports.in.CancelFundUseCase;
import com.btgpactual.funds.domain.ports.in.GetHistoryUseCase;
import com.btgpactual.funds.domain.ports.in.SubscribeToFundUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class FundHandler {
    private final SubscribeToFundUseCase subscribeUseCase;
    private final CancelFundUseCase cancelUseCase;
    private final GetHistoryUseCase historyUseCase;

    public Mono<ServerResponse> subscribe(ServerRequest request) {
        String cid = request.pathVariable("customerId");
        return request.bodyToMono(SubscriptionRequest.class)
            .flatMap(r -> subscribeUseCase.execute(cid, r.fundId(), r.notificationType()))
            .flatMap(t -> ServerResponse.ok().bodyValue(t))
            .onErrorResume(e -> ServerResponse.badRequest().bodyValue(Map.of("error", e.getMessage())));
    }

    public Mono<ServerResponse> cancel(ServerRequest request) {
        String cid = request.pathVariable("customerId");
        return request.bodyToMono(Map.class)
            .flatMap(m -> cancelUseCase.execute(cid, m.get("fundId").toString()))
            .flatMap(t -> ServerResponse.ok().bodyValue(t))
            .onErrorResume(e -> ServerResponse.badRequest().bodyValue(Map.of("error", e.getMessage())));
    }

    public Mono<ServerResponse> getTransactions(ServerRequest request) {
        String cid = request.pathVariable("customerId");
        return historyUseCase.execute(cid)
            .collectList()
            .flatMap(list -> ServerResponse.ok().bodyValue(list));
    }
}
EOF

echo "✅ Caso de uso 'GetHistoryUseCaseImpl' creado y Handler actualizado."