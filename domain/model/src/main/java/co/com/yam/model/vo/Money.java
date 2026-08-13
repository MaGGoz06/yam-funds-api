package co.com.yam.model.vo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public final class Money {

    public static final Money ZERO = of(BigDecimal.ZERO);

    private final BigDecimal amount;

    private Money(BigDecimal amount) {
        this.amount = amount.setScale(2, RoundingMode.HALF_UP);
    }

    public static Money of(BigDecimal amount) {
        if (amount == null) {
            throw new IllegalArgumentException("El monto no puede ser nulo");
        }
        return new Money(amount);
    }

    public static Money cop(long amount) {
        return of(BigDecimal.valueOf(amount));
    }

    public BigDecimal amount() {
        return amount;
    }

    public Money add(Money other) {
        return of(this.amount.add(other.amount));
    }

    public Money subtract(Money other) {
        return of(this.amount.subtract(other.amount));
    }

    public boolean isLessThan(Money other) {
        return this.amount.compareTo(other.amount) < 0;
    }

    public boolean isGreaterThan(Money other) {
        return this.amount.compareTo(other.amount) > 0;
    }

    public boolean isNegative() {
        return amount.signum() < 0;
    }

    public boolean isZeroOrNegative() {
        return amount.signum() <= 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Money money)) {
            return false;
        }
        return amount.compareTo(money.amount) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount.stripTrailingZeros());
    }

    @Override
    public String toString() {
        return "COP $" + amount.toPlainString();
    }
}
