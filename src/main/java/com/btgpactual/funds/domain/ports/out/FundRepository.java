package com.btgpactual.funds.domain.ports.out;
import com.btgpactual.funds.domain.model.Fund;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface FundRepository {

    Mono<Fund> findById(String id);

    Flux<Fund> findAll();

    Mono<Fund> save(Fund fund);
}