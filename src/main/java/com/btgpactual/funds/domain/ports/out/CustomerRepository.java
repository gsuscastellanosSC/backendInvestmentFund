package com.btgpactual.funds.domain.ports.out;
import com.btgpactual.funds.domain.model.*;
import reactor.core.publisher.*;

public interface CustomerRepository {
    Mono<Customer> findById(String id);
    Mono<Customer> save(Customer customer);
    Mono<Transaction> saveTransaction(Transaction tx);
    Flux<Transaction> findTransactionsByCustomerId(String id);
}
