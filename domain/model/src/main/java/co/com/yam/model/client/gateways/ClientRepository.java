package co.com.yam.model.client.gateways;

import co.com.yam.model.client.Client;
import reactor.core.publisher.Mono;

public interface ClientRepository {

    Mono<Client> findById(String id);

    Mono<Client> save(Client client);
}
