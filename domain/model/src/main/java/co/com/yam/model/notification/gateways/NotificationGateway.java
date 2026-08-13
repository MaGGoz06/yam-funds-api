package co.com.yam.model.notification.gateways;

import co.com.yam.model.client.Client;
import reactor.core.publisher.Mono;

public interface NotificationGateway {

    Mono<Void> notifySubscription(Client client, String fundName, String transactionId);
}
