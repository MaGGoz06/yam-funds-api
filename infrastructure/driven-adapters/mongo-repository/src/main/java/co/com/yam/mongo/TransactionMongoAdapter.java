package co.com.yam.mongo;

import co.com.yam.model.transaction.Transaction;
import co.com.yam.model.transaction.gateways.TransactionRepository;
import co.com.yam.mongo.mapper.MongoMapper;
import co.com.yam.mongo.repository.TransactionMongoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
@RequiredArgsConstructor
public class TransactionMongoAdapter implements TransactionRepository {

    private final TransactionMongoRepository repository;

    @Override
    public Mono<Transaction> save(Transaction transaction) {
        return repository.save(MongoMapper.toDocument(transaction)).map(MongoMapper::toDomain);
    }

    @Override
    public Flux<Transaction> findByClientId(String clientId) {
        return repository.findByClientIdOrderByOccurredAtDesc(clientId).map(MongoMapper::toDomain);
    }
}
