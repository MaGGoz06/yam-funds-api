package co.com.yam.mongo.mapper;

import co.com.yam.model.client.Client;
import co.com.yam.model.client.Subscription;
import co.com.yam.model.fund.Fund;
import co.com.yam.model.transaction.Transaction;
import co.com.yam.model.vo.Money;
import co.com.yam.mongo.data.ClientDocument;
import co.com.yam.mongo.data.FundDocument;
import co.com.yam.mongo.data.TransactionDocument;

import java.util.List;

public final class MongoMapper {

    private MongoMapper() {
    }

    public static ClientDocument toDocument(Client client) {
        List<ClientDocument.SubscriptionDocument> subscriptions = client.getSubscriptions().stream()
                .map(MongoMapper::toDocument)
                .toList();
        return ClientDocument.builder()
                .id(client.getId())
                .name(client.getName())
                .email(client.getEmail())
                .phone(client.getPhone())
                .notificationChannel(client.getNotificationChannel())
                .availableBalance(client.getAvailableBalance().amount())
                .subscriptions(subscriptions)
                .version(client.getVersion())
                .build();
    }

    public static Client toDomain(ClientDocument document) {
        List<Subscription> subscriptions = document.getSubscriptions() == null
                ? List.of()
                : document.getSubscriptions().stream().map(MongoMapper::toDomain).toList();
        return Client.builder()
                .id(document.getId())
                .name(document.getName())
                .email(document.getEmail())
                .phone(document.getPhone())
                .notificationChannel(document.getNotificationChannel())
                .availableBalance(Money.of(document.getAvailableBalance()))
                .subscriptions(subscriptions)
                .version(document.getVersion())
                .build();
    }

    public static FundDocument toDocument(Fund fund) {
        return FundDocument.builder()
                .id(fund.getId())
                .name(fund.getName())
                .minAmount(fund.getMinAmount().amount())
                .category(fund.getCategory())
                .build();
    }

    public static Fund toDomain(FundDocument document) {
        return Fund.builder()
                .id(document.getId())
                .name(document.getName())
                .minAmount(Money.of(document.getMinAmount()))
                .category(document.getCategory())
                .build();
    }

    public static TransactionDocument toDocument(Transaction transaction) {
        return TransactionDocument.builder()
                .id(transaction.getId())
                .clientId(transaction.getClientId())
                .fundId(transaction.getFundId())
                .fundName(transaction.getFundName())
                .type(transaction.getType())
                .amount(transaction.getAmount().amount())
                .occurredAt(transaction.getOccurredAt())
                .build();
    }

    public static Transaction toDomain(TransactionDocument document) {
        return Transaction.builder()
                .id(document.getId())
                .clientId(document.getClientId())
                .fundId(document.getFundId())
                .fundName(document.getFundName())
                .type(document.getType())
                .amount(Money.of(document.getAmount()))
                .occurredAt(document.getOccurredAt())
                .build();
    }

    private static ClientDocument.SubscriptionDocument toDocument(Subscription subscription) {
        return ClientDocument.SubscriptionDocument.builder()
                .fundId(subscription.getFundId())
                .fundName(subscription.getFundName())
                .linkedAmount(subscription.getLinkedAmount().amount())
                .subscribedAt(subscription.getSubscribedAt())
                .build();
    }

    private static Subscription toDomain(ClientDocument.SubscriptionDocument document) {
        return Subscription.builder()
                .fundId(document.getFundId())
                .fundName(document.getFundName())
                .linkedAmount(Money.of(document.getLinkedAmount()))
                .subscribedAt(document.getSubscribedAt())
                .build();
    }
}
