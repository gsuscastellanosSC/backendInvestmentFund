package com.btgpactual.funds.infrastructure.config;

import com.btgpactual.funds.infrastructure.adapters.dynamodb.entities.CustomerEntity;
import com.btgpactual.funds.infrastructure.adapters.dynamodb.entities.FundEntity;
import com.btgpactual.funds.infrastructure.adapters.dynamodb.entities.TransactionEntity;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.http.nio.netty.NettyNioAsyncHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.ses.SesAsyncClient;

import java.net.URI;

@Configuration
public class InfrastructureConfig {

    private final AppProperties props;

    public InfrastructureConfig(AppProperties props) {
        this.props = props;
    }

    private StaticCredentialsProvider getCredentials() {
        return StaticCredentialsProvider.create(
                AwsBasicCredentials.create(
                        props.getAws().getAccessKey(),
                        props.getAws().getSecretKey()
                )
        );
    }

    @Bean
    @Primary
    public DynamoDbAsyncClient dynamoDbAsyncClient() {
        return DynamoDbAsyncClient.builder()
                .region(Region.of(props.getAws().getRegion()))
                .endpointOverride(URI.create(props.getAws().getEndpoint()))
                .credentialsProvider(getCredentials())
                .httpClientBuilder(NettyNioAsyncHttpClient.builder())
                .build();
    }

    @Bean
    @Primary
    public DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient(DynamoDbAsyncClient asyncClient) {
        return DynamoDbEnhancedAsyncClient.builder()
                .dynamoDbClient(asyncClient)
                .build();
    }

    @Bean
    @Primary
    public SesAsyncClient sesAsyncClient(AppProperties props) {
        return SesAsyncClient.builder()
                .region(Region.of(props.getAws().getRegion()))
                .endpointOverride(URI.create(props.getAws().getEndpoint()))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(
                                props.getAws().getAccessKey(),
                                props.getAws().getSecretKey()
                        )
                ))
                .build();
    }

    @Bean
    public DynamoDbAsyncTable<FundEntity> fundTable(DynamoDbEnhancedAsyncClient enhancedClient) {
        return enhancedClient.table("funds", TableSchema.fromBean(FundEntity.class));
    }

    @Bean
    public DynamoDbAsyncTable<CustomerEntity> customerTable(DynamoDbEnhancedAsyncClient enhancedClient) {
        return enhancedClient.table("customers", TableSchema.fromBean(CustomerEntity.class));
    }

    @Bean
    public DynamoDbAsyncTable<TransactionEntity> transactionTable(DynamoDbEnhancedAsyncClient enhancedClient) {
        return enhancedClient.table("transactions", TableSchema.fromBean(TransactionEntity.class));
    }
}
