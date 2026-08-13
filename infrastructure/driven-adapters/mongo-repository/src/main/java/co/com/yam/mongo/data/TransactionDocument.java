package co.com.yam.mongo.data;

import co.com.yam.model.transaction.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "transactions")
public class TransactionDocument {

    @Id
    private String id;
    @Indexed
    private String clientId;
    private String fundId;
    private String fundName;
    private TransactionType type;
    private BigDecimal amount;
    private Instant occurredAt;
}
