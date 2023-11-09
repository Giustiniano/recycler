package ae.recycler.be.api.views.vehicle;

import java.sql.Array;
import java.util.ArrayList;

import ae.recycler.be.api.views.serializers.OrderResponse;
import ae.recycler.be.model.Order;
import ae.recycler.be.service.OrderService;
import ae.recycler.be.service.VehicleService;
import ae.recycler.be.service.repository.here.ResponseObjects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
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

    @PutMapping(value = "{id}/assignOrders", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Mono<List<OrderResponse>> assignOrdersToVehicle(@PathVariable String id,
                                                           @RequestParam("driverId") String driverId) {
        return orderService.assignOrdersToVehicle(UUID.fromString(driverId), UUID.fromString(id)).flatMap(orders -> {
            List<OrderResponse> jsonOrders = new ArrayList<>(orders.size());
            for (Order order : orders) {
                jsonOrders.add(OrderResponse.fromOrder(order));
            }
            return Mono.just(jsonOrders);
        });
    }

    @PutMapping(value = "{id}/getItinerary", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Mono<List<ResponseObjects.Stop>> getVehicleItinerary(@PathVariable String id) {
        return vehicleService.getItinerary(UUID.fromString(id));

    }
}
