package co.com.yam.model.client;

import co.com.yam.model.exception.AlreadySubscribedException;
import co.com.yam.model.exception.InsufficientBalanceException;
import co.com.yam.model.exception.InvalidAmountException;
import co.com.yam.model.exception.SubscriptionNotFoundException;
import co.com.yam.model.fund.Fund;
import co.com.yam.model.notification.NotificationChannel;
import co.com.yam.model.vo.Money;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Getter
@Builder(toBuilder = true)
public class Client {

    public static final Money INITIAL_BALANCE = Money.cop(500_000);

    private final String id;
    private final String name;
    private final String email;
    private final String phone;
    private final NotificationChannel notificationChannel;
    private final Money availableBalance;
    private final List<Subscription> subscriptions;
    private final Long version;

    public List<Subscription> getSubscriptions() {
        return subscriptions == null ? List.of() : Collections.unmodifiableList(subscriptions);
    }

    public Optional<Subscription> findSubscription(String fundId) {
        return getSubscriptions().stream()
                .filter(subscription -> subscription.getFundId().equals(fundId))
                .findFirst();
    }

    public boolean isSubscribedTo(String fundId) {
        return findSubscription(fundId).isPresent();
    }

    public Client subscribe(Fund fund, Money amount, Instant subscribedAt) {
        if (amount == null || amount.isZeroOrNegative()) {
            throw new InvalidAmountException("El monto de vinculación debe ser mayor a cero");
        }
        if (amount.isLessThan(fund.getMinAmount())) {
            throw new InvalidAmountException(
                    "El monto de vinculación debe ser al menos " + fund.getMinAmount()
                            + " para el fondo " + fund.getName());
        }
        if (isSubscribedTo(fund.getId())) {
            throw new AlreadySubscribedException(fund.getName());
        }
        if (availableBalance.isLessThan(amount)) {
            throw new InsufficientBalanceException(fund.getName());
        }

        List<Subscription> updated = new ArrayList<>(getSubscriptions());
        updated.add(Subscription.builder()
                .fundId(fund.getId())
                .fundName(fund.getName())
                .linkedAmount(amount)
                .subscribedAt(subscribedAt)
                .build());

        return toBuilder()
                .availableBalance(availableBalance.subtract(amount))
                .subscriptions(updated)
                .build();
    }

    public Client cancelSubscription(String fundId) {
        Subscription current = findSubscription(fundId)
                .orElseThrow(() -> new SubscriptionNotFoundException(fundId));

        List<Subscription> updated = getSubscriptions().stream()
                .filter(subscription -> !subscription.getFundId().equals(fundId))
                .toList();

        return toBuilder()
                .availableBalance(availableBalance.add(current.getLinkedAmount()))
                .subscriptions(updated)
                .build();
    }

    public Client changeNotificationChannel(NotificationChannel channel) {
        if (channel == null) {
            throw new IllegalArgumentException("El canal de notificación es obligatorio");
        }
        return toBuilder().notificationChannel(channel).build();
    }
}
