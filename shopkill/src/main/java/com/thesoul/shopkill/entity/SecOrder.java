package com.thesoul.shopkill.entity;


import com.thesoul.shopkill.util.SnowflakeIdWorker;
import lombok.Data;
import org.springframework.data.annotation.Id;


import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class SecOrder  implements Serializable {

    private static final long serialVersionUID = 1724254862421035876L;

    @Id
    private String id;
    private String userId;
    private String productId;
    private BigDecimal productPrice;
    private BigDecimal amount;

    public SecOrder(String productId) {
        Long utilId = SnowflakeIdWorker.generateId();
        this.id = String.valueOf(utilId);
        this.userId = "userId"+utilId;
        this.productId = productId;
    }

    public SecOrder() {
    }

    @Override
    public String toString() {
        return "SecOrder{" +
                "id='" + id + '\'' +
                ", userId='" + userId + '\'' +
                ", productId='" + productId + '\'' +
                ", productPrice=" + productPrice +
                ", amount=" + amount +
                '}';
    }

}
