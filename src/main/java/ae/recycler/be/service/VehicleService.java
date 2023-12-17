package ae.recycler.be.service;

import ae.recycler.be.api.exceptions.ResourceNotFoundException;
import ae.recycler.be.api.views.serializers.OrderResponse;
import ae.recycler.be.api.views.serializers.OrderUpdateRequest;
import ae.recycler.be.model.Vehicle;
import ae.recycler.be.service.repository.OrderRepository;
import ae.recycler.be.service.repository.VehicleRepository;
import ae.recycler.be.service.repository.here.HereAPIRepository;
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
    @Autowired
    private OrderRepository orderRepository;

    public Mono<List<OrderResponse>> getAssignedOrders(UUID vehicleId) {
        return getVehicleOrError(vehicleId).flatMap(vehicle ->
            Mono.just(OrderResponse.fromOrders(vehicle.getAssignedOrders())));
    }

    public Mono<OrderResponse> updateAssignedOrderStatus(UUID vehicleUUID, UUID orderUUID, OrderUpdateRequest our) {
        return vehicleRepository.findById(vehicleUUID).switchIfEmpty(
                Mono.error(new IllegalStateException("Vehicle not found")))
                .flatMap(vehicle -> orderRepository.findById(orderUUID))
                .switchIfEmpty(Mono.error(
                        new IllegalStateException(String.format("Order with id %s not found", orderUUID))))
                .flatMap(order -> {
                    order.setOrderStatus(our.getNewStatus());
                    return orderRepository.save(order);
                }).flatMap(savedOrder -> Mono.just(OrderResponse.fromOrder(savedOrder)));
    }

    private Mono<Vehicle> getVehicleOrError(UUID vehicleId){
        return vehicleRepository.findById(vehicleId)
                .switchIfEmpty(Mono.error(new IllegalStateException("Vehicle not found")));
    }


    public Mono<OrderResponse> getAssignedOrder(UUID vehicleUUID, UUID orderUUID) {
        return getAssignedOrders(vehicleUUID).flatMap(assignedOrders -> Mono.just(assignedOrders.stream()
                .filter(o -> o.getOrderId().equals(orderUUID)).findFirst()
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "This order was not found among those assigned to this vehicle"))));
    }

}