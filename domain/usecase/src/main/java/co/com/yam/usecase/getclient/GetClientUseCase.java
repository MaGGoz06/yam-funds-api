package co.com.yam.usecase.getclient;

import co.com.yam.model.client.Client;
import co.com.yam.model.client.gateways.ClientRepository;
import co.com.yam.model.exception.ClientNotFoundException;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.Objects;

@RequiredArgsConstructor
public class GetClientUseCase {

    private final ClientRepository clientRepository;

    public Mono<Client> getById(String clientId) {
        Objects.requireNonNull(clientId, "clientId es obligatorio");
        return clientRepository.findById(clientId)
                .switchIfEmpty(Mono.error(new ClientNotFoundException(clientId)));
    }
}
