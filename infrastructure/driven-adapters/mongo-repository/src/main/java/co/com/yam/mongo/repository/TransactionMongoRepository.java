package co.com.yam.mongo.repository;

import co.com.yam.mongo.data.TransactionDocument;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import reactor.core.publisher.Flux;

public interface TransactionMongoRepository extends ReactiveMongoRepository<TransactionDocument, String>,
        ReactiveQueryByExampleExecutor<TransactionDocument> {

    Flux<TransactionDocument> findByClientIdOrderByOccurredAtDesc(String clientId);
}
