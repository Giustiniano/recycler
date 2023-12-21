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
import org.javatuples.Pair;

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
        Pair<UUID,UUID> parsedUUIDs = parseVehicleAndOrderIds(id, orderId);
        UUID vehicleUUID = parsedUUIDs.getValue0(); UUID orderUUID = parsedUUIDs.getValue1();
        return orderUpdateRequest.flatMap(our -> vehicleService.updateAssignedOrderStatus(vehicleUUID, orderUUID, our));
    }

    @GetMapping(value = "{vehicleId}/orders/{orderId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Mono<OrderResponse> getAssignedOrder(@PathVariable String vehicleId, @PathVariable String orderId){
        Pair<UUID,UUID> parsedUUIDs = parseVehicleAndOrderIds(vehicleId, orderId);
        UUID vehicleUUID = parsedUUIDs.getValue0(); UUID orderUUID = parsedUUIDs.getValue1();
        return vehicleService.getAssignedOrder(vehicleUUID, orderUUID);
    }
    @GetMapping(value = "{vehicleId}/orders/next", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<OrderResponse> getNextOrderToPickup(@PathVariable String vehicleId){
        UUID vehicleUUID = Validators.validateId(vehicleId, String.format("%s is not a valid vehicle id", vehicleId));
        return vehicleService.getNextOrderToPickup(vehicleUUID);
    }

    private Pair<UUID, UUID> parseVehicleAndOrderIds(String vehicleId, String orderId){
        UUID vehicleUUID = Validators.validateId(vehicleId, String.format("%s is not a valid vehicle id", vehicleId));
        UUID orderUUID = Validators.validateId(orderId, String.format("%s is not a valid order id", orderId));
        return Pair.with(vehicleUUID, orderUUID);
    }

}
