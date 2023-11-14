package ae.recycler.be.service;

import ae.recycler.be.api.views.serializers.NewOrderRequest;
import ae.recycler.be.api.views.serializers.OrderUpdateRequest;
import ae.recycler.be.enums.OrderStatusEnum;
import ae.recycler.be.enums.VehicleStatus;
import ae.recycler.be.model.Driver;
import ae.recycler.be.model.Order;
import ae.recycler.be.model.Vehicle;
import ae.recycler.be.service.events.OrderEventProducer;
import ae.recycler.be.service.events.serializers.OrderEvent;
import ae.recycler.be.service.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@Slf4j
public class OrderService {
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private AddressRepository addressRepository;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private VehicleRepository vehicleRepository;
    @Autowired
    private DriverRepository driverRepository;






    public Mono<Order> findById(Mono<UUID> orderId){
        return orderRepository.findById(orderId);
    }




    public Mono<List<Order>> assignOrdersToVehicle(UUID driverId, UUID vehicleId){
        return Mono.zip(driverRepository.findById(driverId)
                .switchIfEmpty(Mono.error(
                        new IllegalStateException(String.format("Unable to find driver with id %s", driverId)))),
                vehicleRepository.findById(vehicleId)
                .switchIfEmpty(Mono.error(
                        new IllegalStateException(String.format("Unable to find driver with id %s", driverId))))
        ).flatMap(driverVehicleTuple -> {
            if(driverVehicleTuple.getT2().getAssignedOrders() != null &&
                    !driverVehicleTuple.getT2().getAssignedOrders().isEmpty())
                return Mono.just(driverVehicleTuple.getT2().getAssignedOrders());
            return assignOrdersToVehicle(driverVehicleTuple.getT1(), driverVehicleTuple.getT2());
        });

    }
    public Mono<List<Order>> assignOrdersToVehicle(Driver driver, Vehicle vehicle){
        vehicle.setDriver(driver);
        return Mono.zip(vehicleRepository.save(vehicle), orderRepository.findOrdersByOrderStatusOrderByCreatedDateAsc(OrderStatusEnum.SUBMITTED).collectList())
                .flatMap(vehicleOrders -> {
                        List<Order> assignedOrders = new ArrayList<>();
                        int assignedCapacity = 0;
                        for(Order order: vehicleOrders.getT2()){
                            if(assignedCapacity == vehicleOrders.getT1().getCapacity()){
                                break;
                            }
                            if(assignedCapacity + order.getBoxes() <= vehicleOrders.getT1().getCapacity()){
                                assignedCapacity += order.getBoxes();
                                order.setAssignedVehicle(vehicleOrders.getT1());
                                order.setOrderStatus(OrderStatusEnum.ASSIGNED);
                                assignedOrders.add(order);
                            }

                        }
                        return orderRepository.saveAll(assignedOrders).collectList();
                    }
                );
    }
    public void assignNewOrderToVehicle(OrderEvent orderEvent){
        orderRepository.findById(orderEvent.getOrderId()).flatMap(order ->{
            OrderStatusEnum orderState =  order.getOrderStatus();
            if(orderState.equals(OrderStatusEnum.SUBMITTED)){
                Vehicle assignedVehicle = vehicleRepository.findClosestVehicle(orderEvent.getOrderId());
                if(assignedVehicle == null){
                    return Mono.error(new IllegalStateException("Unable to find vehicle to assign to this order"));
                }
                assignedVehicle.setStatus(VehicleStatus.PICKING_UP);
                order.setAssignedVehicle(assignedVehicle);
                orderRepository.save(order);
            }
            else {
                log.debug("Order {} has state {}, nothing to do", orderEvent, orderState);
            }
            return Mono.just(order);
        }).switchIfEmpty(Mono.error(new IllegalStateException(String.format("Event %s cannot be " +
                "processed, because order with id %s cannot be found", orderEvent, orderEvent.getOrderId()))));
    }




}







