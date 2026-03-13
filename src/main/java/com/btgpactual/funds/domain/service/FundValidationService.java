package com.btgpactual.funds.domain.service;

import com.btgpactual.funds.domain.exception.InsufficientBalanceException;
import com.btgpactual.funds.domain.model.Customer;
import com.btgpactual.funds.domain.model.Fund;

public class FundValidationService {

    public static void validateSubscription(Customer customer, Fund fund) {
        if (customer.getBalance().compareTo(fund.getMinimumAmount()) < 0) {
            throw new InsufficientBalanceException(
                    "No tiene saldo disponible para vincularse al fondo " + fund.getName()
            );
        }
    }
}
