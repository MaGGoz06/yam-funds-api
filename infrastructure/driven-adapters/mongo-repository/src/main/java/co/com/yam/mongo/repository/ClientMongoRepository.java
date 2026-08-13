package co.com.yam.mongo.repository;

import co.com.yam.mongo.data.ClientDocument;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;

public interface ClientMongoRepository extends ReactiveMongoRepository<ClientDocument, String>,
        ReactiveQueryByExampleExecutor<ClientDocument> {
}
