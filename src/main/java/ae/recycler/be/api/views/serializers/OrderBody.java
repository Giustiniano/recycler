package ae.recycler.be.api.views.serializers;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class OrderBody {
    @NotBlank
    private UUID userId;
    @NotBlank
    private UUID pickupAddress;
    @Min(1)
    @NotNull
    private Integer boxes;

}
