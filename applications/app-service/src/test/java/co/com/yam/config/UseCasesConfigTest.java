package co.com.yam.config;

import co.com.yam.model.client.gateways.ClientRepository;
import co.com.yam.model.fund.gateways.FundRepository;
import co.com.yam.model.notification.gateways.NotificationGateway;
import co.com.yam.model.transaction.gateways.TransactionIdGenerator;
import co.com.yam.model.transaction.gateways.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.time.Clock;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class UseCasesConfigTest {

    @Test
    void testUseCaseBeansExist() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(TestConfig.class)) {
            boolean useCaseBeanFound = false;
            for (String beanName : context.getBeanDefinitionNames()) {
                if (beanName.endsWith("UseCase")) {
                    useCaseBeanFound = true;
                    break;
                }
            }
            assertTrue(useCaseBeanFound, "No beans ending with 'UseCase' were found");
        }
    }

    @Configuration
    @Import(UseCasesConfig.class)
    static class TestConfig {

        @Bean
        ClientRepository clientRepository() {
            return mock(ClientRepository.class);
        }

        @Bean
        FundRepository fundRepository() {
            return mock(FundRepository.class);
        }

        @Bean
        TransactionRepository transactionRepository() {
            return mock(TransactionRepository.class);
        }

        @Bean
        NotificationGateway notificationGateway() {
            return mock(NotificationGateway.class);
        }

        @Bean
        TransactionIdGenerator transactionIdGenerator() {
            return () -> "tx-test";
        }

        @Bean
        Clock clock() {
            return Clock.systemUTC();
        }
    }
}
