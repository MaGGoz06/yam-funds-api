package co.com.yam.mongo.data;

import co.com.yam.model.notification.NotificationChannel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "clients")
public class ClientDocument {

    @Id
    private String id;
    private String name;
    private String email;
    private String phone;
    private NotificationChannel notificationChannel;
    private BigDecimal availableBalance;
    @Builder.Default
    private List<SubscriptionDocument> subscriptions = new ArrayList<>();
    @Version
    private Long version;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubscriptionDocument {
        private String fundId;
        private String fundName;
        private BigDecimal linkedAmount;
        private Instant subscribedAt;
    }
}
