package co.com.yam.api;

import co.com.yam.api.dto.SubscribeRequest;
import co.com.yam.model.client.Client;
import co.com.yam.model.fund.Fund;
import co.com.yam.model.fund.FundCategory;
import co.com.yam.model.notification.NotificationChannel;
import co.com.yam.model.transaction.Transaction;
import co.com.yam.model.transaction.TransactionType;
import co.com.yam.model.vo.Money;
import co.com.yam.usecase.cancelsubscription.CancelSubscriptionUseCase;
import co.com.yam.usecase.getclient.GetClientUseCase;
import co.com.yam.usecase.gettransactionhistory.GetTransactionHistoryUseCase;
import co.com.yam.usecase.listfunds.ListFundsUseCase;
import co.com.yam.usecase.subscribetofund.SubscribeToFundUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ContextConfiguration(classes = {RouterRest.class, Handler.class, RouterRestTest.TestConfig.class})
@WebFluxTest
class RouterRestTest {

    @Autowired
    private WebTestClient webTestClient;
    @Autowired
    private ListFundsUseCase listFundsUseCase;
    @Autowired
    private GetClientUseCase getClientUseCase;
    @Autowired
    private SubscribeToFundUseCase subscribeToFundUseCase;
    @Autowired
    private CancelSubscriptionUseCase cancelSubscriptionUseCase;
    @Autowired
    private GetTransactionHistoryUseCase getTransactionHistoryUseCase;

    @BeforeEach
    void setUp() {
        when(listFundsUseCase.list()).thenReturn(Flux.just(Fund.builder()
                .id("1")
                .name("FPV_YAM_PACTUAL_RECAUDADORA")
                .minAmount(Money.cop(75_000))
                .category(FundCategory.FPV)
                .build()));
        when(getClientUseCase.getById("client-001")).thenReturn(Mono.just(client()));
        when(subscribeToFundUseCase.subscribe(eq("client-001"), eq("1"), isNull()))
                .thenReturn(Mono.just(transaction(TransactionType.OPENING)));
        when(cancelSubscriptionUseCase.cancel("client-001", "1"))
                .thenReturn(Mono.just(transaction(TransactionType.CANCELLATION)));
        when(getTransactionHistoryUseCase.history("client-001"))
                .thenReturn(Flux.just(transaction(TransactionType.OPENING)));
    }

    @Test
    void shouldListFunds() {
        webTestClient.get()
                .uri("/api/v1/funds")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].id").isEqualTo("1")
                .jsonPath("$[0].minAmount").isEqualTo(75000);
    }

    @Test
    void shouldGetClient() {
        webTestClient.get()
                .uri("/api/v1/clients/client-001")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.availableBalance").isEqualTo(500000)
                .jsonPath("$.notificationChannel").isEqualTo("EMAIL");
    }

    @Test
    void shouldSubscribe() {
        webTestClient.post()
                .uri("/api/v1/clients/client-001/subscriptions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new SubscribeRequest("1", null))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.type").isEqualTo("OPENING");
    }

    @Test
    void shouldCancel() {
        webTestClient.delete()
                .uri("/api/v1/clients/client-001/subscriptions/1")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.type").isEqualTo("CANCELLATION");
    }

    @Test
    void shouldGetHistory() {
        webTestClient.get()
                .uri("/api/v1/clients/client-001/transactions")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].id").isEqualTo("tx-1");
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

    private Transaction transaction(TransactionType type) {
        return Transaction.builder()
                .id("tx-1")
                .clientId("client-001")
                .fundId("1")
                .fundName("FPV_YAM_PACTUAL_RECAUDADORA")
                .type(type)
                .amount(Money.cop(75_000))
                .occurredAt(Instant.parse("2026-08-12T12:00:00Z"))
                .build();
    }

    @Configuration
    static class TestConfig {
        @Bean
        ListFundsUseCase listFundsUseCase() {
            return mock(ListFundsUseCase.class);
        }

        @Bean
        GetClientUseCase getClientUseCase() {
            return mock(GetClientUseCase.class);
        }

        @Bean
        SubscribeToFundUseCase subscribeToFundUseCase() {
            return mock(SubscribeToFundUseCase.class);
        }

        @Bean
        CancelSubscriptionUseCase cancelSubscriptionUseCase() {
            return mock(CancelSubscriptionUseCase.class);
        }

        @Bean
        GetTransactionHistoryUseCase getTransactionHistoryUseCase() {
            return mock(GetTransactionHistoryUseCase.class);
        }
    }
}
