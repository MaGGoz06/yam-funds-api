package co.com.yam.model.transaction;

import co.com.yam.model.vo.Money;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder(toBuilder = true)
public class Transaction {

    private final String id;
    private final String clientId;
    private final String fundId;
    private final String fundName;
    private final TransactionType type;
    private final Money amount;
    private final Instant occurredAt;
}
