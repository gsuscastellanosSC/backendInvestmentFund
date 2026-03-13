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
        // Extraemos el customerId de la URL /{customerId}/subscribe como define el Router
        String customerId = request.pathVariable("customerId");

        return request.bodyToMono(SubscriptionRequest.class)
                .flatMap(r -> subscribeUseCase.execute(customerId, r.fundId(), r.notificationType())
                        // IMPORTANTE: Si el UseCase tiene éxito, devolvemos la transacción
                        .flatMap(transaction -> ServerResponse.ok().bodyValue(transaction))
                        // Si el UseCase devuelve Mono.empty() (ej: cliente no existe), evitamos el body vacío
                        .switchIfEmpty(ServerResponse.status(404)
                                .bodyValue(Map.of("message", "No se pudo procesar la suscripción"))))
                .onErrorResume(e -> ServerResponse.badRequest()
                        .bodyValue(Map.of("message", e.getMessage())));
    }

    public Mono<ServerResponse> cancel(ServerRequest request) {
        // Extraemos el customerId de la URL para ser consistentes con el Router
        String customerId = request.pathVariable("customerId");

        return request.bodyToMono(Map.class)
                .flatMap(m -> {
                    String fundId = m.get("fundId").toString();
                    return cancelUseCase.execute(customerId, fundId)
                            .flatMap(t -> ServerResponse.ok().bodyValue(t))
                            .switchIfEmpty(ServerResponse.status(404)
                                    .bodyValue(Map.of("message", "Suscripción no encontrada para cancelar")));
                })
                .onErrorResume(e -> ServerResponse.badRequest()
                        .bodyValue(Map.of("message", e.getMessage())));
    }

    public Mono<ServerResponse> getTransactions(ServerRequest request) {
        // Obtenemos el ID de la variable de path {customerId}
        String cid = request.pathVariable("customerId");

        return historyUseCase.execute(cid)
                .collectList()
                .flatMap(list -> ServerResponse.ok().bodyValue(list))
                .onErrorResume(e -> ServerResponse.badRequest()
                        .bodyValue(Map.of("message", e.getMessage())));
    }
}
