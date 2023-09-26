package ae.recycler.be.api.views.serializers;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
@Builder
public class NewOrderRequest {
    @Getter
    private UUID orderId;
    @NotNull
    private UUID customerId;
    @NotNull
    private UUID pickupAddress;
    @Min(1)
    @NotNull
    private Integer boxes;


}
