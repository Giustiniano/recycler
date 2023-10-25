package ae.recycler.be.api.views.vehicle;

import ae.recycler.be.model.Order;
import ae.recycler.be.service.OrderService;
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
    @PutMapping(produces=MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Mono<List<Order>> assignOrdersToVehicle(@PathVariable String id,
                                                   @RequestParam("driverId") String driverId){
        return orderService.assignOrdersToVehicle(UUID.fromString(driverId), UUID.fromString(id));
    }
}
