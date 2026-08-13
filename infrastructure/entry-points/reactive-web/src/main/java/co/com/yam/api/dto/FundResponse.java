package co.com.yam.api.dto;

import java.math.BigDecimal;

public record FundResponse(
        String id,
        String name,
        BigDecimal minAmount,
        String category
) {
}
