package com.btgpactual.funds.infrastructure.entrypoints.reactive.router;
import com.btgpactual.funds.infrastructure.entrypoints.reactive.handler.FundHandler;
import org.springframework.context.annotation.*;
import org.springframework.web.reactive.function.server.*;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class FundRouter {
    @Bean
    public RouterFunction<ServerResponse> fundRoutes(FundHandler handler) {
        return route(POST("/api/customers/{customerId}/subscribe"), handler::subscribe)
                .andRoute(POST("/api/customers/{customerId}/cancel"), handler::cancel)
                .andRoute(GET("/api/customers/{customerId}/transactions"), handler::getTransactions);
    }
}
