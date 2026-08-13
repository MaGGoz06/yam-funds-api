package co.com.yam.usecase.cancelsubscription;

import co.com.yam.model.client.Client;
import co.com.yam.model.client.Subscription;
import co.com.yam.model.client.gateways.ClientRepository;
import co.com.yam.model.exception.ClientNotFoundException;
import co.com.yam.model.exception.OptimisticConcurrencyException;
import co.com.yam.model.exception.SubscriptionNotFoundException;
import co.com.yam.model.transaction.Transaction;
import co.com.yam.model.transaction.TransactionType;
import co.com.yam.model.transaction.gateways.TransactionIdGenerator;
import co.com.yam.model.transaction.gateways.TransactionRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

@RequiredArgsConstructor
public class CancelSubscriptionUseCase {

    private static final int MAX_RETRIES = 3;

    private final ClientRepository clientRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionIdGenerator transactionIdGenerator;
    private final Clock clock;

    public Mono<Transaction> cancel(String clientId, String fundId) {
        Objects.requireNonNull(clientId, "clientId es obligatorio");
        Objects.requireNonNull(fundId, "fundId es obligatorio");

        return Mono.defer(() -> cancelOnce(clientId, fundId))
                .retryWhen(Retry.backoff(MAX_RETRIES, Duration.ofMillis(50))
                        .filter(OptimisticConcurrencyException.class::isInstance)
                        .onRetryExhaustedThrow((spec, signal) -> signal.failure()));
    }

    private Mono<Transaction> cancelOnce(String clientId, String fundId) {
        return clientRepository.findById(clientId)
                .switchIfEmpty(Mono.error(new ClientNotFoundException(clientId)))
                .flatMap(client -> {
                    Subscription subscription = client.findSubscription(fundId)
                            .orElseThrow(() -> new SubscriptionNotFoundException(fundId));
                    Client updated = client.cancelSubscription(fundId);
                    Instant now = Instant.now(clock);
                    Transaction transaction = Transaction.builder()
                            .id(transactionIdGenerator.nextId())
                            .clientId(client.getId())
                            .fundId(fundId)
                            .fundName(subscription.getFundName())
                            .type(TransactionType.CANCELLATION)
                            .amount(subscription.getLinkedAmount())
                            .occurredAt(now)
                            .build();
                    return clientRepository.save(updated)
                            .then(transactionRepository.save(transaction));
                });
    }
}
