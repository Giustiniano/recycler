package ae.recycler.be.api.views.filters;

import ae.recycler.be.enums.OrderStatusEnum;
import lombok.*;
import org.javatuples.Pair;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Builder
@EqualsAndHashCode
@Getter
public class OrderFilter{
    private List<OrderStatusEnum> orderStatuses;
    private Pair<Instant, Instant> createdInterval, lastUpdatedInterval;
    private UUID orderId;


    public void createdInterval(Pair<Instant, Instant> createdInterval) {
        Optional.of(createdInterval).ifPresent(ci -> {
            if (ci.getValue0().isAfter(ci.getValue1()))
                throw new IllegalArgumentException("Created date upper bound cannot be smaller than the lower bound");
            this.createdInterval = createdInterval;
        });
    }
    public void lastUpdatedInterval(Pair<Instant, Instant> lastUpdatedInterval) {
        Optional.of(lastUpdatedInterval).ifPresent(ui -> {
            if (ui.getValue0().isAfter(ui.getValue1()))
                throw new IllegalArgumentException("Created date upper bound cannot be smaller than the lower bound");
            this.lastUpdatedInterval = createdInterval;
        });
    }

}


