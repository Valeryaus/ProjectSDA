package converterTSEtoCSV;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String partNumber;
    private String description;
    private String functionGroup;
    private String supplierProductGroup;
    private String unitOfMeasure;
    private Integer bulkQty;
    private String supplierNumber;
    private String marketingCode;

    @Column(columnDefinition = "Decimal(10,2)")
    private BigDecimal fobNetPrice;
    private String currencyCode;
    private String countryOfOrigin;
    private String blockingCode;
    private Integer weight;
    private String volume;
    private String tariffNo;
    private String companyCode;
    private String environmentalFee1;
    private String environmentalFee2;
    private String environmentalFee3;
    private String environmentalFee4;

    @Column(columnDefinition = "Decimal(10,2)")
    private BigDecimal retailPrice;

    @Column(columnDefinition = "Decimal(10,2)")
    private BigDecimal dealerNetPrice;
    private String passiveFlag;
    private Integer discountCode;


}
