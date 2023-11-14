package ae.recycler.be.service;

import ae.recycler.be.enums.OrderStatusEnum;
import ae.recycler.be.enums.VehicleStatus;
import ae.recycler.be.model.Driver;
import ae.recycler.be.model.Order;
import ae.recycler.be.model.Vehicle;
import ae.recycler.be.service.repository.DriverRepository;
import ae.recycler.be.service.repository.OrderRepository;
import ae.recycler.be.service.repository.VehicleRepository;
import ae.recycler.be.service.repository.here.HereAPIRepository;
import ae.recycler.be.service.repository.here.ResponseObjects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class DriverService {
    @Autowired
    private VehicleRepository vehicleRepository;
    @Autowired
    private DriverRepository driverRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private HereAPIRepository hereAPIRepository;

    public Mono<List<Order>> startShift(UUID driverId, UUID vehicleId){
        return checkDriverAndVehicle(driverId, vehicleId).flatMap(driverVehicleTuple -> {
            driverVehicleTuple.getT2().setDriver(driverVehicleTuple.getT1());
            driverVehicleTuple.getT2().setStatus(VehicleStatus.PICKING_UP);
            return Mono.zip(vehicleRepository.save(driverVehicleTuple.getT2()),
                            orderRepository.findOrdersByOrderStatusOrderByCreatedDateAsc(OrderStatusEnum.SUBMITTED)
                                    .collectList()).flatMap(vehicleOrders -> {
                                List<Order> assignedOrders = new ArrayList<>();
                                int assignedCapacity = 0;
                                for(Order order: vehicleOrders.getT2()){
                                    if(assignedCapacity == vehicleOrders.getT1().getCapacity()){
                                        break;
                                    }
                                    if(assignedCapacity + order.getBoxes() <= vehicleOrders.getT1().getCapacity()){
                                        assignedCapacity += order.getBoxes();
                                        order.setOrderStatus(OrderStatusEnum.ASSIGNED);
                                        assignedOrders.add(order);
                                    }

                                }
                                return Mono.zip(hereAPIRepository.
                                        getPickupPath(List.of(driverVehicleTuple.getT2()), assignedOrders),
                                        Mono.just(assignedOrders), Mono.just(vehicleOrders.getT1()));

                            }
                    ).flatMap(ordersItineraryVehicle -> {

                        List<UUID> unassignedOrders = Optional.ofNullable(ordersItineraryVehicle.getT1().getUnassigned())
                        .orElse(new ArrayList<>(0)).stream().map(ResponseObjects.Unassigned::getJobId)
                        .toList();
                        List<Order> itinerary = new ArrayList<>();
                        for(ResponseObjects.Stop stop : ordersItineraryVehicle.getT1().getTours().get(0).getStops()){
                            String jobId = stop.getActivities().get(0).getJobId();
                            if(jobId.equals("departure") || jobId.equals("arrival"))
                                continue;
                            UUID jobUUID = UUID.fromString(jobId);
                            if(unassignedOrders.contains(jobUUID)){
                                ordersItineraryVehicle.getT2().stream().filter(order -> order.getId()
                                        .equals(jobUUID)).forEach(unassignedOrder ->
                                        unassignedOrder.setOrderStatus(OrderStatusEnum.SUBMITTED));
                            } else {
                                ordersItineraryVehicle.getT2().stream().filter(order -> order.getId().equals(jobUUID))
                                        .forEach(itinerary::add);
                            }
                        }
                        ordersItineraryVehicle.getT3().setAssignedOrders(itinerary);
                        return Mono.zip(vehicleRepository.save(ordersItineraryVehicle.getT3()), Mono.just(itinerary));

                }).flatMap(vehicleItinerary -> Mono.just(vehicleItinerary.getT2()));
        });
    }

    public void endShift(UUID driverId, UUID vehicleId){
        checkDriverAndVehicle(driverId, vehicleId).flatMap(driverAndVehicle -> {
            driverAndVehicle.getT2().setStatus(VehicleStatus.AT_DEPOSIT);
            for(Order order : driverAndVehicle.getT2().getAssignedOrders()){
                if(order.getOrderStatus().equals(OrderStatusEnum.ASSIGNED))
                    order.setOrderStatus(OrderStatusEnum.SUBMITTED);
            }
            driverAndVehicle.getT2().setAssignedOrders(null);
            return vehicleRepository.save(driverAndVehicle.getT2());
        });
    }
    private Mono<Tuple2<Driver, Vehicle>> checkDriverAndVehicle(UUID driverId, UUID vehicleId){
        return Mono.zip(driverRepository.findById(driverId).switchIfEmpty(Mono.error(
                                new IllegalStateException(String.format("Unable to find driver with id %s", driverId)))),
                vehicleRepository.findById(vehicleId).switchIfEmpty(Mono.error(
                                new IllegalStateException(String.format("Unable to find driver with id %s", driverId))))
        );
    }
}
