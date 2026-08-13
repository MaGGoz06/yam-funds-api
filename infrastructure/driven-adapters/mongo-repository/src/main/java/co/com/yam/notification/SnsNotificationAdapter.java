package co.com.yam.notification;

import co.com.yam.model.client.Client;
import co.com.yam.model.notification.NotificationChannel;
import co.com.yam.model.notification.gateways.NotificationGateway;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.sns.SnsAsyncClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;

@Slf4j
@Component
@Profile("aws")
public class SnsNotificationAdapter implements NotificationGateway {

    private final SnsAsyncClient snsAsyncClient;
    private final String emailTopicArn;
    private final String smsTopicArn;

    public SnsNotificationAdapter(
            SnsAsyncClient snsAsyncClient,
            @Value("${adapters.sns.email-topic-arn:}") String emailTopicArn,
            @Value("${adapters.sns.sms-topic-arn:}") String smsTopicArn) {
        this.snsAsyncClient = snsAsyncClient;
        this.emailTopicArn = emailTopicArn;
        this.smsTopicArn = smsTopicArn;
    }

    @Override
    public Mono<Void> notifySubscription(Client client, String fundName, String transactionId) {
        String message = "YAM: se registró su suscripción al fondo " + fundName
                + ". Transacción " + transactionId;
        String topicArn = client.getNotificationChannel() == NotificationChannel.SMS
                ? smsTopicArn
                : emailTopicArn;
        PublishRequest request = PublishRequest.builder()
                .topicArn(topicArn)
                .subject("Suscripción a fondo YAM")
                .message(message)
                .build();
        return Mono.fromFuture(snsAsyncClient.publish(request))
                .doOnSuccess(response -> log.info("SNS messageId={} channel={}",
                        response.messageId(), client.getNotificationChannel()))
                .then();
    }
}
