package co.com.yam.mongo;

import co.com.yam.model.fund.Fund;
import co.com.yam.model.fund.gateways.FundRepository;
import co.com.yam.mongo.mapper.MongoMapper;
import co.com.yam.mongo.repository.FundMongoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
@RequiredArgsConstructor
public class FundMongoAdapter implements FundRepository {

    private final FundMongoRepository repository;

    @Override
    public Mono<Fund> findById(String id) {
        return repository.findById(id).map(MongoMapper::toDomain);
    }

    @Override
    public Flux<Fund> findAll() {
        return repository.findAll().map(MongoMapper::toDomain);
    }

    @Override
    public Mono<Fund> save(Fund fund) {
        return repository.save(MongoMapper.toDocument(fund)).map(MongoMapper::toDomain);
    }
}
