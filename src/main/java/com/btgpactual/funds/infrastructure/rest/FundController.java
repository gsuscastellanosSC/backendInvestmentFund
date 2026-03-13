package com.btgpactual.funds.infrastructure.rest;

import com.btgpactual.funds.domain.model.Fund;
import com.btgpactual.funds.domain.ports.out.FundRepositoryPort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/funds")
public class FundController {

    private final FundRepositoryPort fundRepositoryPort;

    public FundController(FundRepositoryPort fundRepositoryPort) {
        this.fundRepositoryPort = fundRepositoryPort;
    }

    @GetMapping
    public Flux<Fund> getAllFunds() {
        return fundRepositoryPort.findAll();
    }
}
