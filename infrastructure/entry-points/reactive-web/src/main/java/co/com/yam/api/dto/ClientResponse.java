package co.com.yam.api.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record ClientResponse(
        String id,
        String name,
        String email,
        String phone,
        String notificationChannel,
        BigDecimal availableBalance,
        List<SubscriptionResponse> subscriptions,
        Long version
) {
    public record SubscriptionResponse(
            String fundId,
            String fundName,
            BigDecimal linkedAmount,
            Instant subscribedAt
    ) {
    }
}
