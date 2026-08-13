package co.com.yam.model.client;

import co.com.yam.model.vo.Money;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder(toBuilder = true)
public class Subscription {

    private final String fundId;
    private final String fundName;
    private final Money linkedAmount;
    private final Instant subscribedAt;
}
