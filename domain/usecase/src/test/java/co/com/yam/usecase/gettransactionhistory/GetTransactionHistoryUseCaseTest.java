package co.com.yam.usecase.gettransactionhistory;

import co.com.yam.model.client.Client;
import co.com.yam.model.client.gateways.ClientRepository;
import co.com.yam.model.exception.ClientNotFoundException;
import co.com.yam.model.notification.NotificationChannel;
import co.com.yam.model.transaction.Transaction;
import co.com.yam.model.transaction.TransactionType;
import co.com.yam.model.transaction.gateways.TransactionRepository;
import co.com.yam.model.vo.Money;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetTransactionHistoryUseCaseTest {

    @Mock
    private ClientRepository clientRepository;
    @Mock
    private TransactionRepository transactionRepository;
    @InjectMocks
    private GetTransactionHistoryUseCase useCase;

    @Test
    void shouldReturnHistorySortedByDateDesc() {
        when(clientRepository.findById("client-001")).thenReturn(Mono.just(client()));
        when(transactionRepository.findByClientId("client-001")).thenReturn(Flux.just(
                transaction("a", Instant.parse("2026-08-01T00:00:00Z")),
                transaction("b", Instant.parse("2026-08-10T00:00:00Z"))
        ));

        StepVerifier.create(useCase.history("client-001"))
                .expectNextMatches(tx -> tx.getId().equals("b"))
                .expectNextMatches(tx -> tx.getId().equals("a"))
                .verifyComplete();
    }

    @Test
    void shouldFailWhenClientDoesNotExist() {
        when(clientRepository.findById("missing")).thenReturn(Mono.empty());

        StepVerifier.create(useCase.history("missing"))
                .expectError(ClientNotFoundException.class)
                .verify();
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
                .build();
    }

    private Transaction transaction(String id, Instant occurredAt) {
        return Transaction.builder()
                .id(id)
                .clientId("client-001")
                .fundId("1")
                .fundName("FPV_YAM_PACTUAL_RECAUDADORA")
                .type(TransactionType.OPENING)
                .amount(Money.cop(75_000))
                .occurredAt(occurredAt)
                .build();
    }
}
