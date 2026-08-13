package co.com.yam.model.fund;

import co.com.yam.model.vo.Money;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(toBuilder = true)
public class Fund {

    private final String id;
    private final String name;
    private final Money minAmount;
    private final FundCategory category;
}
