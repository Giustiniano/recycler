package ae.recycler.be.api.views.vehicle;

import ae.recycler.be.api.views.Validators;
import ae.recycler.be.api.views.serializers.OrderResponse;
import ae.recycler.be.api.views.serializers.OrderUpdateRequest;
import ae.recycler.be.service.OrderService;
import ae.recycler.be.service.VehicleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/api/v1/vehicle")
public class VehicleResource {

    @Autowired
    private OrderService orderService;
    @Autowired
    private VehicleService vehicleService;

    @GetMapping(value = "{id}/orders", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Mono<List<OrderResponse>> getAssignedOrders(@PathVariable String id) {
        UUID vehicleId = Validators.validateId(id, String.format("%s is not a valid vehicle id", id));
        return vehicleService.getAssignedOrders(vehicleId);
    }

    @PutMapping(value = "{id}/orders/{orderId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Mono<OrderResponse> updateAssignedOrder(@PathVariable String id, @PathVariable String orderId,
                                           @RequestBody Mono<OrderUpdateRequest> orderUpdateRequest){
        UUID vehicleUUID = Validators.validateId(id, String.format("%s is not a valid vehicle id", id));
        UUID orderUUID = Validators.validateId(orderId, String.format("%s is not a valid order id", orderId));
        return orderUpdateRequest.flatMap(our -> vehicleService.updateAssignedOrderStatus(vehicleUUID, orderUUID, our));
    }

}
