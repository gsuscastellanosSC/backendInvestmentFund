package com.btgpactual.funds.infrastructure.adapters.notification;

import com.btgpactual.funds.domain.model.Customer;
import com.btgpactual.funds.domain.ports.out.NotificationPort;
import com.btgpactual.funds.infrastructure.config.AppProperties;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import software.amazon.awssdk.services.ses.SesAsyncClient;
import software.amazon.awssdk.services.ses.model.SendEmailRequest;
import software.amazon.awssdk.services.ses.model.Destination;
import software.amazon.awssdk.services.ses.model.Content;
import software.amazon.awssdk.services.ses.model.Body;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationAdapter implements NotificationPort {

    private final SesAsyncClient sesAsyncClient;
    private final AppProperties appProps;

    @PostConstruct
    public void initTwilio() {
        Twilio.init(appProps.getTwilio().getSid(), appProps.getTwilio().getToken());
        log.info("Twilio initialized successfully for SMS notifications");
    }

    @Override
    public Mono<Void> send(Customer customer, String message, String notificationType) {
        return switch (notificationType.toUpperCase()) {
            case "SMS" -> validateAndSendSms(customer, message);
            case "EMAIL" -> validateAndSendEmail(customer, message);
            default -> Mono.error(new IllegalArgumentException("Canal no soportado: " + notificationType));
        };
    }

    private Mono<Void> validateAndSendSms(Customer customer, String message) {
        if (customer.getPhoneNumber() == null || customer.getPhoneNumber().isBlank()) {
            log.warn("Suscripción exitosa, pero no se envió SMS: Cliente sin teléfono.");
            return Mono.empty();
        }
        return sendSms(customer.getPhoneNumber(), message);
    }

    private Mono<Void> validateAndSendEmail(Customer customer, String message) {
        if (customer.getEmail() == null || customer.getEmail().isBlank()) {
            return Mono.error(new RuntimeException("Cliente sin correo electrónico registrado"));
        }
        return sendEmail(customer.getEmail(), message);
    }

    private Mono<Void> sendEmail(String email, String bodyText) {
        SendEmailRequest request = SendEmailRequest.builder()
                .destination(Destination.builder().toAddresses(email).build())
                .message(software.amazon.awssdk.services.ses.model.Message.builder()
                        .subject(Content.builder().data("BTG Pactual - Notificación de Movimiento").build())
                        .body(Body.builder().text(Content.builder().data(bodyText).build()).build())
                        .build())
                .source(appProps.getBusiness().getFromEmail())
                .build();

        return Mono.fromFuture(sesAsyncClient.sendEmail(request))
                .doOnSuccess(s -> log.info("📧 Email enviado exitosamente a: {}", email))
                .then();
    }

    private Mono<Void> sendSms(String phone, String bodyText) {
        return Mono.fromRunnable(() -> {
                    // Extraemos el SID del Servicio de Mensajería desde el YAML
                    String messagingServiceSid = appProps.getTwilio().getMessagingServiceSid();

                    log.info("🚀 Enviando SMS a {} usando Messaging Service SID: {}", phone, messagingServiceSid);

                    // IMPORTANTE: El segundo parámetro es el String del SID, no un PhoneNumber
                    Message message = Message.creator(
                            new com.twilio.type.PhoneNumber(phone), // To
                            messagingServiceSid,                    // From (Service SID)
                            bodyText                                // Body
                    ).create();

                    log.info("✅ SMS enviado con éxito. Twilio SID: {}", message.getSid());
                })
                .subscribeOn(Schedulers.boundedElastic())
                .onErrorResume(e -> {
                    // Resiliencia: Si Twilio falla, logueamos el error pero no rompemos la suscripción
                    log.error("⚠️ Falló el envío de SMS de Twilio: {}", e.getMessage());
                    return Mono.empty();
                })
                .then();
    }
}
