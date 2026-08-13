package co.com.yam.config;

import co.com.yam.model.client.Client;
import co.com.yam.model.client.gateways.ClientRepository;
import co.com.yam.model.fund.Fund;
import co.com.yam.model.fund.FundCategory;
import co.com.yam.model.fund.gateways.FundRepository;
import co.com.yam.model.notification.NotificationChannel;
import co.com.yam.model.vo.Money;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CatalogSeeder implements ApplicationRunner {

    public static final String DEFAULT_CLIENT_ID = "client-001";

    private final FundRepository fundRepository;
    private final ClientRepository clientRepository;

    @Override
    public void run(ApplicationArguments args) {
        seedFunds()
                .then(seedDefaultClient())
                .doOnSuccess(unused -> log.info("Catálogo de fondos y cliente inicial listos"))
                .block();
    }

    private Mono<Void> seedFunds() {
        return Flux.fromIterable(catalog())
                .flatMap(fund -> fundRepository.findById(fund.getId())
                        .switchIfEmpty(fundRepository.save(fund)))
                .then();
    }

    private Mono<Void> seedDefaultClient() {
        return clientRepository.findById(DEFAULT_CLIENT_ID)
                .switchIfEmpty(clientRepository.save(Client.builder()
                        .id(DEFAULT_CLIENT_ID)
                        .name("Cliente Demo YAM")
                        .email("cliente@yam.com")
                        .phone("+573001112233")
                        .notificationChannel(NotificationChannel.EMAIL)
                        .availableBalance(Client.INITIAL_BALANCE)
                        .subscriptions(List.of())
                        .build()))
                .then();
    }

    static List<Fund> catalog() {
        return List.of(
                fund("1", "FPV_YAM_PACTUAL_RECAUDADORA", 75_000, FundCategory.FPV),
                fund("2", "FPV_YAM_PACTUAL_ECOPETROL", 125_000, FundCategory.FPV),
                fund("3", "DEUDAPRIVADA", 50_000, FundCategory.FIC),
                fund("4", "FDO-ACCIONES", 250_000, FundCategory.FIC),
                fund("5", "FPV_YAM_PACTUAL_DINAMICA", 100_000, FundCategory.FPV)
        );
    }

    private static Fund fund(String id, String name, long minAmount, FundCategory category) {
        return Fund.builder()
                .id(id)
                .name(name)
                .minAmount(Money.cop(minAmount))
                .category(category)
                .build();
    }
}
