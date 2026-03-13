package com.btgpactual.funds.infrastructure.config;

import com.btgpactual.funds.domain.model.Customer;
import com.btgpactual.funds.domain.model.Fund;
import com.btgpactual.funds.domain.ports.out.CustomerRepository;
import com.btgpactual.funds.domain.ports.out.FundRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.dynamodb.model.AttributeDefinition;
import software.amazon.awssdk.services.dynamodb.model.CreateTableRequest;
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement;
import software.amazon.awssdk.services.dynamodb.model.KeyType;
import software.amazon.awssdk.services.dynamodb.model.ProvisionedThroughput;
import software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException;
import software.amazon.awssdk.services.dynamodb.model.ScalarAttributeType;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Configuration
@Profile("local")
@RequiredArgsConstructor
public class DataInitializer {

    private final FundRepository fundRepository;
    private final CustomerRepository customerRepository;
    private final DynamoDbAsyncClient dynamoDbAsyncClient;

    @EventListener(ApplicationReadyEvent.class)
    public void loadData() {
        log.info("🚀 Iniciando carga de datos sincronizada...");

        // 1. Crear tablas secuencialmente
        Mono.fromFuture(() -> createTableFunds())
                .then(Mono.fromFuture(() -> createTableCustomers()))
                .then(Mono.fromFuture(() -> createTableTransactions()))
                // 2. SOLO después de que las 3 tablas existan, empezamos a poblar
                .thenMany(Flux.fromIterable(getInitialFunds())
                        .flatMap(fund -> fundRepository.save(fund)
                                .doOnSuccess(f -> log.info("📌 Fondo guardado: {}", f.getName()))))
                // 3. Finalmente, el cliente inicial
                .then(customerRepository.findById("jesus.castellanos@example.com")
                        .switchIfEmpty(Mono.defer(() -> {
                            log.info("👤 Creando cliente inicial...");
                            return customerRepository.save(createInitialCustomer());
                        })))
                .doOnSuccess(v -> log.info("✅ ¡Todo cargado con éxito!"))
                .doOnError(e -> log.error("❌ Error en la inicialización: {}", e.getMessage()))
                .subscribe(); // El suscriptor final que dispara toda la cadena
    }

    private CompletableFuture<Void> createTableFunds() {
        return createGenericTable("funds", "id");
    }

    private CompletableFuture<Void> createTableCustomers() {
        return createGenericTable("customers", "id");
    }

    // AJUSTE CRÍTICO: Definición de Clave Compuesta para el Historial
    private CompletableFuture<Void> createTableTransactions() {
        String tableName = "transactions";
        return dynamoDbAsyncClient.describeTable(t -> t.tableName(tableName))
                .thenAccept(r -> log.info("Tabla {} ya existe.", tableName))
                .exceptionallyCompose(ex -> {
                    log.info("Creando tabla {} con clave compuesta (customerId + timestamp)...", tableName);
                    return dynamoDbAsyncClient.createTable(CreateTableRequest.builder()
                            .tableName(tableName)
                            .attributeDefinitions(
                                    AttributeDefinition.builder().attributeName("customerId").attributeType(ScalarAttributeType.S).build(),
                                    AttributeDefinition.builder().attributeName("timestamp").attributeType(ScalarAttributeType.N).build()
                            )
                            .keySchema(
                                    KeySchemaElement.builder().attributeName("customerId").keyType(KeyType.HASH).build(),
                                    KeySchemaElement.builder().attributeName("timestamp").keyType(KeyType.RANGE).build()
                            )
                            .provisionedThroughput(ProvisionedThroughput.builder().readCapacityUnits(5L).writeCapacityUnits(5L).build())
                            .build()).thenAccept(r -> log.info("Tabla {} lista.", tableName));
                });
    }

    private CompletableFuture<Void> createGenericTable(String tableName, String partitionKey) {
        return dynamoDbAsyncClient.describeTable(t -> t.tableName(tableName))
                .thenAccept(r -> log.info("Tabla {} ya existe.", tableName))
                .exceptionallyCompose(ex -> {
                    return dynamoDbAsyncClient.createTable(CreateTableRequest.builder()
                            .tableName(tableName)
                            .attributeDefinitions(AttributeDefinition.builder().attributeName(partitionKey).attributeType(ScalarAttributeType.S).build())
                            .keySchema(KeySchemaElement.builder().attributeName(partitionKey).keyType(KeyType.HASH).build())
                            .provisionedThroughput(ProvisionedThroughput.builder().readCapacityUnits(5L).writeCapacityUnits(5L).build())
                            .build()).thenAccept(r -> log.info("Tabla {} creada.", tableName));
                });
    }

    private List<Fund> getInitialFunds() {
        return List.of(
                new Fund("1", "FPV_BTG_PACTUAL_RECAUDADORA", new BigDecimal("75000"), "FPV"),
                new Fund("2", "FPV_BTG_PACTUAL_ECOPETROL", new BigDecimal("125000"), "FPV"),
                new Fund("3", "DEUDAPRIVADA", new BigDecimal("50000"), "FIC"),
                new Fund("4", "FDO-ACCIONES", new BigDecimal("250000"), "FIC"),
                new Fund("5", "FPV_BTG_PACTUAL_DINAMICA", new BigDecimal("100000"), "FPV")
        );
    }

    private Customer createInitialCustomer() {
        return Customer.builder()
                .id("jesus.castellanos@example.com")
                .name("Jesus Castellanos")
                .email("jesus.castellanos@example.com")
                .phoneNumber("+573057206275")
                .balance(new BigDecimal("500000"))
                .activeSubscriptions(new HashSet<>())
                .build();
    }
}
