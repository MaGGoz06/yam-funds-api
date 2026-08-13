package co.com.yam.api.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record TransactionResponse(
        String id,
        String clientId,
        String fundId,
        String fundName,
        String type,
        BigDecimal amount,
        Instant occurredAt
) {
}
