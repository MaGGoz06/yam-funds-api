package co.com.yam.config;

import co.com.yam.model.transaction.gateways.TransactionIdGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.util.UUID;

@Configuration
public class DomainBeansConfig {

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    public TransactionIdGenerator transactionIdGenerator() {
        return () -> UUID.randomUUID().toString();
    }
}
