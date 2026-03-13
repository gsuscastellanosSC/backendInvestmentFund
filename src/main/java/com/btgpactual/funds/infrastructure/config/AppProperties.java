package com.btgpactual.funds.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import java.math.BigDecimal;
@Data
@Configuration
@ConfigurationProperties
public class AppProperties {

    private DynamodbAws dynamodbAws = new DynamodbAws();
    private Business business = new Business();
    private Aws aws = new Aws();
    private TwilioProps twilio = new TwilioProps();

    @Data
    public static class Business {
        private Double initialBalance;
        private String fromEmail;
        private String twilioPhone;
    }

    @Data
    public static class DynamodbAws {
        private String customersTable;
        private String fundsTable;
        private String transactionsTable;
    }

    @Data
    public static class Aws {
        private String region;
        private String endpoint;
        private String accessKey;
        private String secretKey;

        @Data
        public static class Dynamodb {
            private String customersTable;
            private String fundsTable;
            private String transactionsTable;
        }
    }

    @Data
    public static class TwilioProps {
        private String sid;
        private String token;
        private String messagingServiceSid;
    }

 }
