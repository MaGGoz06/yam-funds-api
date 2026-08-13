package co.com.yam.mongo;

import co.com.yam.model.client.Client;
import co.com.yam.model.exception.OptimisticConcurrencyException;
import co.com.yam.model.notification.NotificationChannel;
import co.com.yam.model.vo.Money;
import co.com.yam.mongo.data.ClientDocument;
import co.com.yam.mongo.repository.ClientMongoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.OptimisticLockingFailureException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClientMongoAdapterTest {

    @Mock
    private ClientMongoRepository repository;
    @InjectMocks
    private ClientMongoAdapter adapter;

    @Test
    void shouldMapOptimisticLockToDomainException() {
        Client client = Client.builder()
                .id("client-001")
                .name("Cliente Demo YAM")
                .email("cliente@yam.com")
                .phone("+573001112233")
                .notificationChannel(NotificationChannel.EMAIL)
                .availableBalance(Money.cop(500_000))
                .subscriptions(List.of())
                .version(1L)
                .build();
        when(repository.save(any(ClientDocument.class)))
                .thenReturn(Mono.error(new OptimisticLockingFailureException("stale version")));

        StepVerifier.create(adapter.save(client))
                .expectError(OptimisticConcurrencyException.class)
                .verify();
    }

    @Test
    void shouldFindById() {
        when(repository.findById("client-001")).thenReturn(Mono.just(ClientDocument.builder()
                .id("client-001")
                .name("Cliente Demo YAM")
                .email("cliente@yam.com")
                .phone("+573001112233")
                .notificationChannel(NotificationChannel.EMAIL)
                .availableBalance(BigDecimal.valueOf(500_000))
                .subscriptions(List.of())
                .version(0L)
                .build()));

        StepVerifier.create(adapter.findById("client-001"))
                .expectNextMatches(client -> client.getAvailableBalance().equals(Money.cop(500_000)))
                .verifyComplete();
    }
}
