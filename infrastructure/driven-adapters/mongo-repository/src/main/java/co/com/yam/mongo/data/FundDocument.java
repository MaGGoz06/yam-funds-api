package co.com.yam.mongo.data;

import co.com.yam.model.fund.FundCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "funds")
public class FundDocument {

    @Id
    private String id;
    private String name;
    private BigDecimal minAmount;
    private FundCategory category;
}
