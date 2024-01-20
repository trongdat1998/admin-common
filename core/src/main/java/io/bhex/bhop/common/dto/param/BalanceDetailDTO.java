package io.bhex.bhop.common.dto.param;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.bhex.bhop.common.util.DecimalOutputSerialize;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class BalanceDetailDTO {

    //@JsonSerialize(using = ToStringSerializer.class)
   // private long accountId;
    private String tokenId;
    private String tokenFullName;
    @JsonSerialize(using = DecimalOutputSerialize.class)
    private BigDecimal total;
    @JsonSerialize(using = DecimalOutputSerialize.class)
    private BigDecimal available;
    @JsonSerialize(using = DecimalOutputSerialize.class)
    private BigDecimal locked;

    @JsonSerialize(using = DecimalOutputSerialize.class)
    private BigDecimal totalInUnit;
    @JsonSerialize(using = DecimalOutputSerialize.class)
    private BigDecimal availableInUnit;

    private String unit;

}
