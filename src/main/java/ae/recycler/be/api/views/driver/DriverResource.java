package ae.recycler.be.api.views.driver;

import ae.recycler.be.api.views.Validators;
import ae.recycler.be.api.views.serializers.OrderResponse;
import ae.recycler.be.model.Order;
import ae.recycler.be.service.DriverService;
import ae.recycler.be.service.repository.here.ResponseObjects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/driver")
public class DriverResource {

    @Autowired
    private DriverService driverService;
    @PutMapping(value = "{id}/startShift",  produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Mono<List<OrderResponse>> startShift(@PathVariable String id,
                                                     @RequestParam("vehicleId") String vehicleId){
        UUID driverUUID = Validators.validateId(id, String.format("%s is not a valid driver id", id));
        UUID vehicleUUID = Validators.validateId(vehicleId, String.format("%s is not a valid vehicle id", vehicleId));

        return driverService.startShift(driverUUID, vehicleUUID)
                .flatMap(itinerary -> Mono.just(OrderResponse.fromOrders(itinerary)));
    }
    @PutMapping(value = "{id}/endShift",  produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Mono<ResponseEntity<Object>> endShift(@PathVariable String id,
                                                 @RequestParam("vehicleId") String vehicleId){
        UUID driverUUID = Validators.validateId(id, String.format("%s is not a valid driver id", id));
        UUID vehicleUUID = Validators.validateId(vehicleId, String.format("%s is not a valid vehicle id", vehicleId));
        driverService.endShift(driverUUID, vehicleUUID);
        return Mono.just(ResponseEntity.status(HttpStatus.NO_CONTENT).build());
    }

}
