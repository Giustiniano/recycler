package ae.recycler.be.service;

import ae.recycler.be.api.views.serializers.NewOrderRequest;
import ae.recycler.be.api.views.serializers.OrderUpdateRequest;
import ae.recycler.be.enums.OrderStatusEnum;
import ae.recycler.be.enums.VehicleStatus;
import ae.recycler.be.model.Driver;
import ae.recycler.be.model.Order;
import ae.recycler.be.model.Vehicle;
import ae.recycler.be.service.events.serializers.OrderEvent;
import ae.recycler.be.service.events.OrderEventProducer;
import ae.recycler.be.service.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.*;

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
    @Autowired
    private OrderEventProducer producer;



    public Mono<Order> saveOrder(Mono<NewOrderRequest> orderBody) {
        // first make sure the user exists
        return orderBody.flatMap(newOrderRequest1 -> customerRepository.findById(newOrderRequest1.getCustomerId())
                .switchIfEmpty(Mono.error(new IllegalStateException("Customer not found")))
                .flatMap(customer -> customerRepository.findCustomerAddress(
                        newOrderRequest1.getCustomerId(), newOrderRequest1.getPickupAddress()
                ).switchIfEmpty(Mono.error(
                        new IllegalStateException("Cannot find pickup address in customer address list"))
                ).flatMap(address -> orderRepository.save(Order.builder()
                        .orderStatus(OrderStatusEnum.SUBMITTED)
                        .pickupAddress(address).boxes(newOrderRequest1.getBoxes())
                        .submittedBy(customer).build())
                )));
    }

    public Mono<Order> findById(Mono<UUID> orderId){
        return orderRepository.findById(orderId);
    }

    public Mono<Order> updateOrder(UUID orderId, OrderUpdateRequest dataToUpdate) {
        return orderRepository.findById(orderId).switchIfEmpty(
                Mono.error(new IllegalStateException("Order with the given id was not found")))
                .flatMap(this::isOrderUpdatable)
                .flatMap(updateableOrder -> updatePickupAddress(updateableOrder, dataToUpdate.getNewPickupAddress()))
                .flatMap(orderWithUpdatedPickupAddress -> updateStatus(orderWithUpdatedPickupAddress, dataToUpdate.getNewStatus()))
                .flatMap(orderWithUpdatedStatus -> updateBoxes(orderWithUpdatedStatus, dataToUpdate.getNewBoxesCount()))
                .flatMap(fullyUpdatedOrder -> orderRepository.save(fullyUpdatedOrder))
                .flatMap(savedOrder -> producer.sendOrderEvent(OrderEvent.fromOrder(savedOrder)).flatMap(eventSendResult
                        -> {
                                if(eventSendResult.exception() != null){
                                    return Mono.error(eventSendResult.exception());
                                }
                                return Mono.just(savedOrder);
                        }));
    }


    public Mono<List<Order>> assignOrdersToVehicle(UUID driverId, UUID vehicleId){
        return Mono.zip(driverRepository.findById(driverId)
                .switchIfEmpty(Mono.error(
                        new IllegalStateException(String.format("Unable to find driver with id %s", driverId)))),
                vehicleRepository.findById(vehicleId)
                .switchIfEmpty(Mono.error(
                        new IllegalStateException(String.format("Unable to find driver with id %s", driverId))))
        ).flatMap(driverVehicleTuple -> assignOrdersToVehicle(driverVehicleTuple.getT1(), driverVehicleTuple.getT2()));

    }
    public Mono<List<Order>> assignOrdersToVehicle(Driver driver, Vehicle vehicle){
        return orderRepository.findByOrderStatusOrderByCreatedAtAsc(OrderStatusEnum.SUBMITTED).flatMap(orders -> {
            int capacity = 0;
            List<Order> assignedOrders = new ArrayList<>();
            while(orders.iterator().hasNext()){
                Order order = orders.iterator().next();
                if(capacity + order.getBoxes() <= vehicle.getCapacity()){
                    capacity += order.getBoxes();
                    order.setAssignedVehicle(vehicle);
                    order.setOrderStatus(OrderStatusEnum.ASSIGNED);
                    assignedOrders.add(order);
                }
                else {
                    break;
                }
            }
            vehicle.setAssignedOrders(assignedOrders);
            vehicleRepository.save(vehicle);
            return Mono.just(assignedOrders);
        });
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

    private Mono<Order> isOrderUpdatable(Order order){
        return order.isUpdateable() ? Mono.just(order) : Mono.error(
                new IllegalStateException(String.format("Order with status %s cannot be updated",
                        order.getOrderStatus())));
    }
    private Mono<Order> updatePickupAddress(Order order, UUID address){
        if(address == null){
            return Mono.just(order);
        }
        return customerRepository.findCustomerAddress(order.getSubmittedBy().getId(), address)
                .switchIfEmpty(Mono.error(new IllegalStateException("Unable to find address"))).
                flatMap(address1 -> {
                    order.setPickupAddress(address1); return Mono.just(order);});
    }

    private Mono<Order> updateStatus(Order order, OrderStatusEnum newStatus){
        if(newStatus != null){
           order.setOrderStatus(newStatus);
        }

        return Mono.just(order);
    }

    private Mono<Order> updateBoxes(Order order, Integer newBoxesCount){
        if(newBoxesCount != null){
            order.setBoxes(newBoxesCount);
        }
        return Mono.just(order);
    }
}







