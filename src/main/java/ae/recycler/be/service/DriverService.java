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
import reactor.util.function.Tuple3;

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
        return checkDriverAndVehicle(driverId, vehicleId, List.of(VehicleStatus.AT_DEPOSIT)).flatMap(driverVehicleTuple -> {
            driverVehicleTuple.getT2().setDriver(driverVehicleTuple.getT1());
            driverVehicleTuple.getT2().setStatus(VehicleStatus.PICKING_UP);
            return Mono.zip(vehicleRepository.save(driverVehicleTuple.getT2()),
                            orderRepository.findOrdersByOrderStatusOrderByCreatedDateAsc(OrderStatusEnum.SUBMITTED)
                                    .collectList()).flatMap(vehicleOrders -> {
                        List<Order> assignedOrders = getAssignedOrders(vehicleOrders);
                        return Mono.zip(hereAPIRepository.
                                        getPickupPath(List.of(driverVehicleTuple.getT2()), assignedOrders),
                                        Mono.just(assignedOrders), Mono.just(vehicleOrders.getT1()));

                            }
                    ).flatMap(ordersItineraryVehicle -> {

                List<Order> itinerary = processHereApiRoutingResponse(ordersItineraryVehicle);
                return Mono.zip(vehicleRepository.save(ordersItineraryVehicle.getT3()), Mono.just(itinerary));

                }).flatMap(vehicleItinerary -> Mono.just(vehicleItinerary.getT2()));
        });
    }

    private static List<Order> processHereApiRoutingResponse(
            Tuple3<ResponseObjects.Response, List<Order>, Vehicle> ordersItineraryVehicle) {
        List<UUID> unassignedOrders = Optional.ofNullable(ordersItineraryVehicle.getT1().getUnassigned())
        .orElse(new ArrayList<>(0)).stream().map(ResponseObjects.Unassigned::getJobId)
        .toList();
        List<Order> itinerary = new ArrayList<>();
        int i = 1;
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
                Order order = ordersItineraryVehicle.getT2().stream().filter(o -> o.getId().equals(jobUUID))
                        .findFirst().get();
                order.setPickupOrder(i);
                itinerary.add(order);
                i++;
            }
        }
        ordersItineraryVehicle.getT3().setAssignedOrders(itinerary);
        return itinerary;
    }

    private static List<Order> getAssignedOrders(Tuple2<Vehicle, List<Order>> vehicleOrders) {
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
        return assignedOrders;
    }

    public void endShift(UUID driverId, UUID vehicleId){
        checkDriverAndVehicle(driverId, vehicleId, List.of(VehicleStatus.PICKING_UP)).flatMap(driverAndVehicle -> {
            driverAndVehicle.getT2().setStatus(VehicleStatus.AT_DEPOSIT);
            List<Order> updatedOrders = driverAndVehicle.getT2().getAssignedOrders().stream()
                    .filter(order -> order.getOrderStatus().equals(OrderStatusEnum.ASSIGNED)).map(order -> {
                        order.setOrderStatus(OrderStatusEnum.SUBMITTED);
                        order.setPickupOrder(null);
                        return order;
                    }).toList();
            driverAndVehicle.getT2().setAssignedOrders(null);
            return Mono.zip(vehicleRepository.save(driverAndVehicle.getT2()),
                    orderRepository.saveAll(updatedOrders).collectList());
        });
    }
    private Mono<Tuple2<Driver, Vehicle>> checkDriverAndVehicle(UUID driverId, UUID vehicleId,
                                                                List<VehicleStatus> allowedVehicleStatus){
        return Mono.zip(driverRepository.findById(driverId).switchIfEmpty(Mono.error(
                                new IllegalArgumentException(String.format("Unable to find driver with id %s", driverId)))),
                vehicleRepository.findById(vehicleId).switchIfEmpty(Mono.error(
                                new IllegalArgumentException(String.format("Unable to find driver with id %s", driverId))))
                        .flatMap(vehicle -> {
                                if(!allowedVehicleStatus.contains(vehicle.getStatus()))
                                    return Mono.error(new IllegalStateException(
                                            String.format("Vehicle is %s, allowed states: %s",
                                                    vehicle.getStatus(), allowedVehicleStatus)));
                                return Mono.just(vehicle);
                            }));
    }
}
