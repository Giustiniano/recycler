package ae.recycler.be.api.views.serializers;

import ae.recycler.be.enums.OrderStatusEnum;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.UUID;

@AllArgsConstructor
@EqualsAndHashCode
@Getter
public class OrderUpdateRequest {
    private OrderStatusEnum newStatus;
    private UUID newPickupAddress;
    private UUID newDriver;
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  private Integer newBoxesCount;
}
