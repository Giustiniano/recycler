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
        return checkDriverAndVehicle(driverId, vehicleId).flatMap(driverVehicleTuple -> {
            Driver driver = driverVehicleTuple.getT1();
            Vehicle vehicle = driverVehicleTuple.getT2();
            if(vehicle.getStatus().equals(VehicleStatus.PICKING_UP)){
                return continueShift(vehicle);
            }

            return orderRepository.findOrdersByOrderStatusOrderByCreatedDateAsc(OrderStatusEnum.SUBMITTED).collectList()
                    .flatMap(ordersToAssign -> {
                if(ordersToAssign.isEmpty()){
                    return Mono.empty();
                }

                List<Order> assignedOrders = getAssignedOrders(vehicle, ordersToAssign);
                if(assignedOrders.isEmpty()){
                    return Mono.empty();
                }
                return hereAPIRepository.getPickupPath(List.of(vehicle), assignedOrders).flatMap(apiResponse -> {
                    vehicle.setAssignedOrders(processHereApiRoutingResponse(apiResponse, assignedOrders, vehicle));
                    vehicle.setDriver(driver);
                    vehicle.setStatus(VehicleStatus.PICKING_UP);
                    return vehicleRepository.save(vehicle).map(Vehicle::getAssignedOrders);
                });

            });

        });
    }

    private Mono<List<Order>> continueShift(Vehicle vehicle){
        return orderRepository.findOrdersByAssignedVehicleOrderByPickupOrderAsc(vehicle.getId()).collectList();
    }

    private static List<Order> processHereApiRoutingResponse(
            ResponseObjects.Response apiResponse, List<Order> orders, Vehicle vehicle) {
        List<UUID> unassignedOrders = Optional.ofNullable(apiResponse.getUnassigned())
        .orElse(new ArrayList<>(0)).stream().map(ResponseObjects.Unassigned::getJobId)
        .toList();
        List<Order> itinerary = new ArrayList<>();
        int i = 1;
        for(ResponseObjects.Stop stop : apiResponse.getTours().get(0).getStops()){
            String jobId = stop.getActivities().get(0).getJobId();
            if(jobId.equals("departure") || jobId.equals("arrival"))
                continue;
            UUID jobUUID = UUID.fromString(jobId);
            if(!unassignedOrders.contains(jobUUID)){
                Order order = orders.stream().filter(o -> o.getId().equals(jobUUID))
                        .findFirst().get();
                order.setPickupOrder(i);
                order.setOrderStatus(OrderStatusEnum.ASSIGNED);
                itinerary.add(order);
                i++;
            }
        }
        return itinerary;
    }

    private static List<Order> getAssignedOrders(Vehicle vehicle, List<Order> ordersToAssign) {
        List<Order> assignedOrders = new ArrayList<>();
        int assignedCapacity = 0;
        for(Order order: ordersToAssign){
            if(assignedCapacity == vehicle.getCapacity()){
                break;
            }
            if(assignedCapacity + order.getBoxes() <= vehicle.getCapacity()){
                assignedCapacity += order.getBoxes();
                assignedOrders.add(order);
            }

        }
        return assignedOrders;
    }

    public void endShift(UUID driverId, UUID vehicleId){
        vehicleRepository.endShift(vehicleId);
    }
    private Mono<Tuple2<Driver, Vehicle>> checkDriverAndVehicle(UUID driverId, UUID vehicleId){
        return Mono.zip(driverRepository.findById(driverId).switchIfEmpty(Mono.error(
                                new IllegalArgumentException(String.format("Unable to find driver with id %s", driverId)))),
                vehicleRepository.findById(vehicleId).switchIfEmpty(Mono.error(
                                new IllegalArgumentException(String.format("Unable to find vehicle with id %s", driverId))))
                        .map(vehicle -> vehicle));
    }
}
