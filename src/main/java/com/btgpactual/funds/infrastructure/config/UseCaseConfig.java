package com.btgpactual.funds.infrastructure.config;

import com.btgpactual.funds.application.usecase.CancelFundUseCaseImpl;
import com.btgpactual.funds.application.usecase.GetHistoryUseCaseImpl;
import com.btgpactual.funds.application.usecase.SubscribeToFundUseCaseImpl;
import com.btgpactual.funds.domain.ports.in.CancelFundUseCase;
import com.btgpactual.funds.domain.ports.in.GetHistoryUseCase;
import com.btgpactual.funds.domain.ports.in.SubscribeToFundUseCase;
import com.btgpactual.funds.domain.ports.out.CustomerRepository;
import com.btgpactual.funds.domain.ports.out.FundRepository;
import com.btgpactual.funds.domain.ports.out.NotificationPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {

    @Bean
    public SubscribeToFundUseCase subscribeToFundUseCase(
            CustomerRepository customerRepository,
            FundRepository fundRepository,
            NotificationPort notificationPort) {

        return new SubscribeToFundUseCaseImpl(
                customerRepository,
                fundRepository,
                notificationPort
        );
    }

    @Bean
    public CancelFundUseCase cancelFundUseCase(
            CustomerRepository customerRepository,
            FundRepository fundRepository) {
        return new CancelFundUseCaseImpl(customerRepository, fundRepository);
    }

    @Bean
    public GetHistoryUseCase getHistoryUseCase(CustomerRepository customerRepository) {
        return new GetHistoryUseCaseImpl(customerRepository);
    }
}
