package co.com.yam.usecase.subscribetofund;

import co.com.yam.model.client.Client;
import co.com.yam.model.client.gateways.ClientRepository;
import co.com.yam.model.exception.ClientNotFoundException;
import co.com.yam.model.exception.FundNotFoundException;
import co.com.yam.model.exception.InsufficientBalanceException;
import co.com.yam.model.exception.OptimisticConcurrencyException;
import co.com.yam.model.fund.Fund;
import co.com.yam.model.fund.FundCategory;
import co.com.yam.model.fund.gateways.FundRepository;
import co.com.yam.model.notification.NotificationChannel;
import co.com.yam.model.notification.gateways.NotificationGateway;
import co.com.yam.model.transaction.Transaction;
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
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubscribeToFundUseCaseTest {

    private static final Instant NOW = Instant.parse("2026-08-12T15:00:00Z");

    @Mock
    private ClientRepository clientRepository;
    @Mock
    private FundRepository fundRepository;
    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private NotificationGateway notificationGateway;
    @Mock
    private TransactionIdGenerator transactionIdGenerator;

    private SubscribeToFundUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new SubscribeToFundUseCase(
                clientRepository,
                fundRepository,
                transactionRepository,
                notificationGateway,
                transactionIdGenerator,
                Clock.fixed(NOW, ZoneOffset.UTC)
        );
    }

    @Test
    void shouldSubscribeUsingFundMinimumWhenAmountIsOmitted() {
        when(clientRepository.findById("client-001")).thenReturn(Mono.just(client()));
        when(fundRepository.findById("1")).thenReturn(Mono.just(fund()));
        when(clientRepository.save(any())).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        when(transactionIdGenerator.nextId()).thenReturn("tx-1");
        when(transactionRepository.save(any())).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        when(notificationGateway.notifySubscription(any(), any(), any())).thenReturn(Mono.empty());

        StepVerifier.create(useCase.subscribe("client-001", "1", null))
                .assertNext(transaction -> {
                    assertEquals("tx-1", transaction.getId());
                    assertEquals(TransactionType.OPENING, transaction.getType());
                    assertEquals(Money.cop(75_000), transaction.getAmount());
                    assertEquals(NOW, transaction.getOccurredAt());
                })
                .verifyComplete();

        ArgumentCaptor<Client> clientCaptor = ArgumentCaptor.forClass(Client.class);
        verify(clientRepository).save(clientCaptor.capture());
        assertEquals(Money.cop(425_000), clientCaptor.getValue().getAvailableBalance());
        verify(notificationGateway).notifySubscription(any(), any(), any());
    }

    @Test
    void shouldFailWhenClientDoesNotExist() {
        when(clientRepository.findById("missing")).thenReturn(Mono.empty());
        when(fundRepository.findById("1")).thenReturn(Mono.just(fund()));

        StepVerifier.create(useCase.subscribe("missing", "1", null))
                .expectError(ClientNotFoundException.class)
                .verify();
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void shouldFailWhenFundDoesNotExist() {
        when(clientRepository.findById("client-001")).thenReturn(Mono.just(client()));
        when(fundRepository.findById("99")).thenReturn(Mono.empty());

        StepVerifier.create(useCase.subscribe("client-001", "99", null))
                .expectError(FundNotFoundException.class)
                .verify();
    }

    @Test
    void shouldFailWhenBalanceIsNotEnough() {
        when(clientRepository.findById("client-001")).thenReturn(Mono.just(client()));
        when(fundRepository.findById("4")).thenReturn(Mono.just(Fund.builder()
                .id("4")
                .name("FDO-ACCIONES")
                .minAmount(Money.cop(250_000))
                .category(FundCategory.FIC)
                .build()));

        StepVerifier.create(useCase.subscribe("client-001", "4", Money.cop(600_000)))
                .expectErrorSatisfies(error -> {
                    assertEquals(InsufficientBalanceException.class, error.getClass());
                    assertEquals("No tiene saldo disponible para vincularse al fondo FDO-ACCIONES",
                            error.getMessage());
                })
                .verify();
    }

    @Test
    void shouldRetryOnOptimisticLockAndSucceed() {
        AtomicInteger attempts = new AtomicInteger();
        when(clientRepository.findById("client-001")).thenReturn(Mono.just(client()));
        when(fundRepository.findById("1")).thenReturn(Mono.just(fund()));
        when(transactionIdGenerator.nextId()).thenReturn("tx-retry");
        when(clientRepository.save(any())).thenAnswer(invocation -> {
            if (attempts.getAndIncrement() == 0) {
                return Mono.error(new OptimisticConcurrencyException("client-001"));
            }
            return Mono.just(invocation.getArgument(0));
        });
        when(transactionRepository.save(any())).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        when(notificationGateway.notifySubscription(any(), any(), any())).thenReturn(Mono.empty());

        StepVerifier.create(useCase.subscribe("client-001", "1", Money.cop(75_000)))
                .assertNext(transaction -> assertEquals("tx-retry", transaction.getId()))
                .verifyComplete();

        verify(clientRepository, times(2)).save(any());
    }

    @Test
    void shouldKeepSubscriptionWhenNotificationFails() {
        when(clientRepository.findById("client-001")).thenReturn(Mono.just(client()));
        when(fundRepository.findById("1")).thenReturn(Mono.just(fund()));
        when(clientRepository.save(any())).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        when(transactionIdGenerator.nextId()).thenReturn("tx-2");
        when(transactionRepository.save(any())).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        when(notificationGateway.notifySubscription(any(), any(), any()))
                .thenReturn(Mono.error(new IllegalStateException("SNS down")));

        StepVerifier.create(useCase.subscribe("client-001", "1", null))
                .assertNext(transaction -> assertEquals("tx-2", transaction.getId()))
                .verifyComplete();
    }

    private Client client() {
        return Client.builder()
                .id("client-001")
                .name("Cliente Demo YAM")
                .email("cliente@yam.com")
                .phone("+573001112233")
                .notificationChannel(NotificationChannel.EMAIL)
                .availableBalance(Client.INITIAL_BALANCE)
                .subscriptions(List.of())
                .version(0L)
                .build();
    }

    private Fund fund() {
        return Fund.builder()
                .id("1")
                .name("FPV_YAM_PACTUAL_RECAUDADORA")
                .minAmount(Money.cop(75_000))
                .category(FundCategory.FPV)
                .build();
    }
}
