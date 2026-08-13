package co.com.yam.usecase.subscribetofund;

import co.com.yam.model.client.Client;
import co.com.yam.model.client.gateways.ClientRepository;
import co.com.yam.model.exception.ClientNotFoundException;
import co.com.yam.model.exception.FundNotFoundException;
import co.com.yam.model.exception.OptimisticConcurrencyException;
import co.com.yam.model.fund.Fund;
import co.com.yam.model.fund.gateways.FundRepository;
import co.com.yam.model.notification.gateways.NotificationGateway;
import co.com.yam.model.transaction.Transaction;
import co.com.yam.model.transaction.TransactionType;
import co.com.yam.model.transaction.gateways.TransactionIdGenerator;
import co.com.yam.model.transaction.gateways.TransactionRepository;
import co.com.yam.model.vo.Money;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

@RequiredArgsConstructor
public class SubscribeToFundUseCase {

    private static final int MAX_RETRIES = 3;

    private final ClientRepository clientRepository;
    private final FundRepository fundRepository;
    private final TransactionRepository transactionRepository;
    private final NotificationGateway notificationGateway;
    private final TransactionIdGenerator transactionIdGenerator;
    private final Clock clock;

    public Mono<Transaction> subscribe(String clientId, String fundId, Money amount) {
        Objects.requireNonNull(clientId, "clientId es obligatorio");
        Objects.requireNonNull(fundId, "fundId es obligatorio");

        return Mono.defer(() -> subscribeOnce(clientId, fundId, amount))
                .retryWhen(Retry.backoff(MAX_RETRIES, Duration.ofMillis(50))
                        .filter(OptimisticConcurrencyException.class::isInstance)
                        .onRetryExhaustedThrow((spec, signal) -> signal.failure()));
    }

    private Mono<Transaction> subscribeOnce(String clientId, String fundId, Money amount) {
        return Mono.zip(loadClient(clientId), loadFund(fundId))
                .flatMap(tuple -> {
                    Client client = tuple.getT1();
                    Fund fund = tuple.getT2();
                    Money linkingAmount = amount == null ? fund.getMinAmount() : amount;
                    Instant now = Instant.now(clock);
                    Client updated = client.subscribe(fund, linkingAmount, now);
                    Transaction transaction = Transaction.builder()
                            .id(transactionIdGenerator.nextId())
                            .clientId(client.getId())
                            .fundId(fund.getId())
                            .fundName(fund.getName())
                            .type(TransactionType.OPENING)
                            .amount(linkingAmount)
                            .occurredAt(now)
                            .build();
                    return clientRepository.save(updated)
                            .then(transactionRepository.save(transaction))
                            .flatMap(saved -> notificationGateway
                                    .notifySubscription(updated, fund.getName(), saved.getId())
                                    .onErrorResume(error -> Mono.empty())
                                    .thenReturn(saved));
                });
    }

    private Mono<Client> loadClient(String clientId) {
        return clientRepository.findById(clientId)
                .switchIfEmpty(Mono.error(new ClientNotFoundException(clientId)));
    }

    private Mono<Fund> loadFund(String fundId) {
        return fundRepository.findById(fundId)
                .switchIfEmpty(Mono.error(new FundNotFoundException(fundId)));
    }
}
