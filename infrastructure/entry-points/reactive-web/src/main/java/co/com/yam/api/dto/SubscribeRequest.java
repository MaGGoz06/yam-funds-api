package co.com.yam.api.dto;

import java.math.BigDecimal;

public record SubscribeRequest(String fundId, BigDecimal amount) {
}
