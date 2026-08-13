package co.com.yam.model.client;

import co.com.yam.model.exception.AlreadySubscribedException;
import co.com.yam.model.exception.InsufficientBalanceException;
import co.com.yam.model.exception.InvalidAmountException;
import co.com.yam.model.exception.SubscriptionNotFoundException;
import co.com.yam.model.fund.Fund;
import co.com.yam.model.fund.FundCategory;
import co.com.yam.model.notification.NotificationChannel;
import co.com.yam.model.vo.Money;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClientTest {

    private static final Instant NOW = Instant.parse("2026-08-12T12:00:00Z");

    @Test
    void shouldSubscribeAndDebitBalance() {
        Client updated = client().subscribe(fund("1", "FPV_YAM_PACTUAL_RECAUDADORA", 75_000),
                Money.cop(75_000), NOW);

        assertEquals(Money.cop(425_000), updated.getAvailableBalance());
        assertTrue(updated.isSubscribedTo("1"));
        assertEquals(Money.cop(75_000), updated.findSubscription("1").orElseThrow().getLinkedAmount());
    }

    @Test
    void shouldRejectInsufficientBalanceWithExactBusinessMessage() {
        InsufficientBalanceException error = assertThrows(InsufficientBalanceException.class,
                () -> client().subscribe(fund("4", "FDO-ACCIONES", 250_000), Money.cop(600_000), NOW));

        assertEquals("No tiene saldo disponible para vincularse al fondo FDO-ACCIONES", error.getMessage());
        assertEquals("INSUFFICIENT_BALANCE", error.getCode());
    }

    @Test
    void shouldRejectAmountBelowMinimum() {
        assertThrows(InvalidAmountException.class,
                () -> client().subscribe(fund("2", "FPV_YAM_PACTUAL_ECOPETROL", 125_000),
                        Money.cop(50_000), NOW));
    }

    @Test
    void shouldRejectDuplicateActiveSubscription() {
        Client subscribed = client().subscribe(fund("3", "DEUDAPRIVADA", 50_000), Money.cop(50_000), NOW);
        assertThrows(AlreadySubscribedException.class,
                () -> subscribed.subscribe(fund("3", "DEUDAPRIVADA", 50_000), Money.cop(50_000), NOW));
    }

    @Test
    void shouldCancelAndReturnLinkedAmount() {
        Fund fund = fund("1", "FPV_YAM_PACTUAL_RECAUDADORA", 75_000);
        Client subscribed = client().subscribe(fund, Money.cop(80_000), NOW);
        Client cancelled = subscribed.cancelSubscription("1");

        assertEquals(Client.INITIAL_BALANCE, cancelled.getAvailableBalance());
        assertFalse(cancelled.isSubscribedTo("1"));
    }

    @Test
    void shouldFailWhenCancellingUnknownFund() {
        assertThrows(SubscriptionNotFoundException.class, () -> client().cancelSubscription("99"));
    }

    private Client client() {
        return Client.builder()
                .id("client-001")
                .name("Cliente Demo YAM")
                .email("cliente@yam.com")
                .phone("+573001112233")
                .notificationChannel(NotificationChannel.EMAIL)
                .availableBalance(Client.INITIAL_BALANCE)
                .subscriptions(List.of())
                .version(0L)
                .build();
    }

    private Fund fund(String id, String name, long minAmount) {
        return Fund.builder()
                .id(id)
                .name(name)
                .minAmount(Money.cop(minAmount))
                .category(FundCategory.FPV)
                .build();
    }
}
