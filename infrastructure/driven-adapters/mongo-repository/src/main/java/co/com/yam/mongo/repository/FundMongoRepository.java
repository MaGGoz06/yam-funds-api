package co.com.yam.mongo.repository;

import co.com.yam.mongo.data.FundDocument;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;

public interface FundMongoRepository extends ReactiveMongoRepository<FundDocument, String>,
        ReactiveQueryByExampleExecutor<FundDocument> {
}
