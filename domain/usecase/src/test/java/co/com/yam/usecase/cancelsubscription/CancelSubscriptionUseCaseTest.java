package co.com.yam.usecase.cancelsubscription;

import co.com.yam.model.client.Client;
import co.com.yam.model.client.Subscription;
import co.com.yam.model.client.gateways.ClientRepository;
import co.com.yam.model.exception.SubscriptionNotFoundException;
import co.com.yam.model.notification.NotificationChannel;
import co.com.yam.model.transaction.TransactionType;
import co.com.yam.model.transaction.gateways.TransactionIdGenerator;
import co.com.yam.model.transaction.gateways.TransactionRepository;
import co.com.yam.model.vo.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CancelSubscriptionUseCaseTest {

    @Mock
    private ClientRepository clientRepository;
    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private TransactionIdGenerator transactionIdGenerator;

    private CancelSubscriptionUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new CancelSubscriptionUseCase(
                clientRepository,
                transactionRepository,
                transactionIdGenerator,
                Clock.fixed(Instant.parse("2026-08-12T16:00:00Z"), ZoneOffset.UTC)
        );
    }

    @Test
    void shouldCancelAndReturnLinkedAmountToClient() {
        when(clientRepository.findById("client-001")).thenReturn(Mono.just(subscribedClient()));
        when(clientRepository.save(any())).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        when(transactionIdGenerator.nextId()).thenReturn("tx-cancel");
        when(transactionRepository.save(any())).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(useCase.cancel("client-001", "1"))
                .assertNext(transaction -> {
                    assertEquals(TransactionType.CANCELLATION, transaction.getType());
                    assertEquals(Money.cop(75_000), transaction.getAmount());
                    assertEquals("tx-cancel", transaction.getId());
                })
                .verifyComplete();

        ArgumentCaptor<Client> captor = ArgumentCaptor.forClass(Client.class);
        verify(clientRepository).save(captor.capture());
        assertEquals(Client.INITIAL_BALANCE, captor.getValue().getAvailableBalance());
        assertEquals(0, captor.getValue().getSubscriptions().size());
    }

    @Test
    void shouldFailWhenClientIsNotSubscribed() {
        Client client = subscribedClient().toBuilder().subscriptions(List.of()).build();
        when(clientRepository.findById("client-001")).thenReturn(Mono.just(client));

        StepVerifier.create(useCase.cancel("client-001", "1"))
                .expectError(SubscriptionNotFoundException.class)
                .verify();
    }

    private Client subscribedClient() {
        return Client.builder()
                .id("client-001")
                .name("Cliente Demo YAM")
                .email("cliente@yam.com")
                .phone("+573001112233")
                .notificationChannel(NotificationChannel.EMAIL)
                .availableBalance(Money.cop(425_000))
                .version(1L)
                .subscriptions(List.of(Subscription.builder()
                        .fundId("1")
                        .fundName("FPV_YAM_PACTUAL_RECAUDADORA")
                        .linkedAmount(Money.cop(75_000))
                        .subscribedAt(Instant.parse("2026-08-12T12:00:00Z"))
                        .build()))
                .build();
    }
}
