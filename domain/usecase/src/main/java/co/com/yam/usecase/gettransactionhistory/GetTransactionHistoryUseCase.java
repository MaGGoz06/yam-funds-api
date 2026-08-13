package co.com.yam.usecase.gettransactionhistory;

import co.com.yam.model.exception.ClientNotFoundException;
import co.com.yam.model.client.gateways.ClientRepository;
import co.com.yam.model.transaction.Transaction;
import co.com.yam.model.transaction.gateways.TransactionRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Comparator;
import java.util.Objects;

@RequiredArgsConstructor
public class GetTransactionHistoryUseCase {

    private final ClientRepository clientRepository;
    private final TransactionRepository transactionRepository;

    public Flux<Transaction> history(String clientId) {
        Objects.requireNonNull(clientId, "clientId es obligatorio");
        return clientRepository.findById(clientId)
                .switchIfEmpty(Mono.error(new ClientNotFoundException(clientId)))
                .flatMapMany(client -> transactionRepository.findByClientId(client.getId()))
                .sort(Comparator.comparing(Transaction::getOccurredAt).reversed());
    }
}
