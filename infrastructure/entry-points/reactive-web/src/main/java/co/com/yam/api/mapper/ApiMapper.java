package co.com.yam.api.mapper;

import co.com.yam.api.dto.ClientResponse;
import co.com.yam.api.dto.FundResponse;
import co.com.yam.api.dto.TransactionResponse;
import co.com.yam.model.client.Client;
import co.com.yam.model.fund.Fund;
import co.com.yam.model.transaction.Transaction;

public final class ApiMapper {

    private ApiMapper() {
    }

    public static ClientResponse toResponse(Client client) {
        return new ClientResponse(
                client.getId(),
                client.getName(),
                client.getEmail(),
                client.getPhone(),
                client.getNotificationChannel().name(),
                client.getAvailableBalance().amount(),
                client.getSubscriptions().stream()
                        .map(subscription -> new ClientResponse.SubscriptionResponse(
                                subscription.getFundId(),
                                subscription.getFundName(),
                                subscription.getLinkedAmount().amount(),
                                subscription.getSubscribedAt()))
                        .toList(),
                client.getVersion()
        );
    }

    public static FundResponse toResponse(Fund fund) {
        return new FundResponse(
                fund.getId(),
                fund.getName(),
                fund.getMinAmount().amount(),
                fund.getCategory().name()
        );
    }

    public static TransactionResponse toResponse(Transaction transaction) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getClientId(),
                transaction.getFundId(),
                transaction.getFundName(),
                transaction.getType().name(),
                transaction.getAmount().amount(),
                transaction.getOccurredAt()
        );
    }
}
