package co.com.yam.api.config;

import co.com.yam.api.Handler;
import co.com.yam.api.RouterRest;
import co.com.yam.model.fund.Fund;
import co.com.yam.model.fund.FundCategory;
import co.com.yam.model.vo.Money;
import co.com.yam.usecase.cancelsubscription.CancelSubscriptionUseCase;
import co.com.yam.usecase.getclient.GetClientUseCase;
import co.com.yam.usecase.gettransactionhistory.GetTransactionHistoryUseCase;
import co.com.yam.usecase.listfunds.ListFundsUseCase;
import co.com.yam.usecase.subscribetofund.SubscribeToFundUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ContextConfiguration(classes = {RouterRest.class, Handler.class, ConfigTest.TestConfig.class})
@WebFluxTest
@Import({CorsConfig.class, SecurityHeadersConfig.class})
@TestPropertySource(properties = "cors.allowed-origins=http://localhost:8080")
class ConfigTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void corsConfigurationShouldAllowOrigins() {
        webTestClient.get()
                .uri("/api/v1/funds")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals("Content-Security-Policy",
                        "default-src 'self'; frame-ancestors 'self'; form-action 'self'")
                .expectHeader().valueEquals("Strict-Transport-Security", "max-age=31536000; includeSubDomains; preload")
                .expectHeader().valueEquals("X-Content-Type-Options", "nosniff")
                .expectHeader().valueEquals("Server", "")
                .expectHeader().valueEquals("Cache-Control", "no-store")
                .expectHeader().valueEquals("Pragma", "no-cache")
                .expectHeader().valueEquals("Referrer-Policy", "strict-origin-when-cross-origin");
    }

    @Configuration
    static class TestConfig {
        @Bean
        ListFundsUseCase listFundsUseCase() {
            ListFundsUseCase useCase = mock(ListFundsUseCase.class);
            when(useCase.list()).thenReturn(Flux.just(Fund.builder()
                    .id("1")
                    .name("FPV_YAM_PACTUAL_RECAUDADORA")
                    .minAmount(Money.cop(75_000))
                    .category(FundCategory.FPV)
                    .build()));
            return useCase;
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
