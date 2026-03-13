package com.btgpactual.funds.domain.ports.out;

import com.btgpactual.funds.domain.model.Customer;
import com.btgpactual.funds.domain.model.Fund;
import com.btgpactual.funds.domain.model.Transaction;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface FundRepositoryPort {
    Flux<Fund> findAll();
    Mono<Customer> getCustomerById(String id);
    Mono<Fund> getFundById(String id);
    Mono<Void> updateCustomerBalance(Customer customer);
    Mono<Transaction> saveTransaction(Transaction transaction);
    Mono<Fund> saveFund(Fund fund);
}
