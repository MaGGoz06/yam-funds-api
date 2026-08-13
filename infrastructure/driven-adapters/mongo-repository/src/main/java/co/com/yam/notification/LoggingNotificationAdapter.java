package co.com.yam.notification;

import co.com.yam.model.client.Client;
import co.com.yam.model.notification.gateways.NotificationGateway;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@Profile("!aws")
public class LoggingNotificationAdapter implements NotificationGateway {

    @Override
    public Mono<Void> notifySubscription(Client client, String fundName, String transactionId) {
        return Mono.fromRunnable(() -> log.info(
                "Notificación {} a {} ({}): suscripción al fondo {} registrada. Transacción {}",
                client.getNotificationChannel(),
                client.getName(),
                destination(client),
                fundName,
                transactionId));
    }

    private String destination(Client client) {
        return switch (client.getNotificationChannel()) {
            case EMAIL -> client.getEmail();
            case SMS -> client.getPhone();
        };
    }
}
