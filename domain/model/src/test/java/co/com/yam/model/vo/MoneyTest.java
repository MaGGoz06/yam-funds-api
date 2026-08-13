package co.com.yam.model.vo;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MoneyTest {

    @Test
    void shouldAddAndSubtract() {
        Money result = Money.cop(500_000).subtract(Money.cop(75_000)).add(Money.cop(10_000));
        assertEquals(Money.cop(435_000), result);
    }

    @Test
    void shouldCompareAmounts() {
        assertTrue(Money.cop(50_000).isLessThan(Money.cop(75_000)));
        assertFalse(Money.cop(125_000).isLessThan(Money.cop(100_000)));
        assertTrue(Money.cop(-1).isNegative());
    }

    @Test
    void shouldRejectNullAmount() {
        assertThrows(IllegalArgumentException.class, () -> Money.of(null));
    }

    @Test
    void shouldTreatScaleAsEqual() {
        assertEquals(Money.of(new BigDecimal("75000.00")), Money.cop(75_000));
    }
}
