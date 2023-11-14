package ae.recycler.be.service;

import ae.recycler.be.service.repository.VehicleRepository;
import ae.recycler.be.service.repository.here.HereAPIRepository;
import ae.recycler.be.service.repository.here.ResponseObjects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Component
@Slf4j
public class VehicleService {
    @Autowired
    private HereAPIRepository hereAPIRepository;
    @Autowired
    private VehicleRepository vehicleRepository;

    public Mono<ResponseObjects.Response> getItinerary(UUID vehicleId){
        return vehicleRepository.findById(vehicleId)
                .switchIfEmpty(Mono.error(new IllegalStateException("Vehicle not found")))
                .flatMap(vehicle -> hereAPIRepository.getPickupPath(List.of(vehicle), vehicle.getAssignedOrders()));
    }
}
