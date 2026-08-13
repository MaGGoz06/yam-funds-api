package co.com.yam.model.fund.gateways;

import co.com.yam.model.fund.Fund;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface FundRepository {

    Mono<Fund> findById(String id);

    Flux<Fund> findAll();

    Mono<Fund> save(Fund fund);
}
