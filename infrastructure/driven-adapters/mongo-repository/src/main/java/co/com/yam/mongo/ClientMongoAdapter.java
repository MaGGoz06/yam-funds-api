package co.com.yam.mongo;

import co.com.yam.model.client.Client;
import co.com.yam.model.client.gateways.ClientRepository;
import co.com.yam.model.exception.OptimisticConcurrencyException;
import co.com.yam.mongo.mapper.MongoMapper;
import co.com.yam.mongo.repository.ClientMongoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
@RequiredArgsConstructor
public class ClientMongoAdapter implements ClientRepository {

    private final ClientMongoRepository repository;

    @Override
    public Mono<Client> findById(String id) {
        return repository.findById(id).map(MongoMapper::toDomain);
    }

    @Override
    public Mono<Client> save(Client client) {
        return repository.save(MongoMapper.toDocument(client))
                .map(MongoMapper::toDomain)
                .onErrorMap(OptimisticLockingFailureException.class,
                        error -> new OptimisticConcurrencyException(client.getId()));
    }
}
