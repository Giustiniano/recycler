package ae.recycler.be.api.views.serializers;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
@Builder
public class OrderRequest {
    @Getter
    private UUID orderId;
    @NotBlank
    private UUID customerId;
    @NotBlank
    private UUID pickupAddress;
    @Min(1)
    @NotNull
    private Integer boxes;


}
