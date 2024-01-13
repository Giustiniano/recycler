package ae.recycler.be.factories;

import ae.recycler.be.enums.OrderStatusEnum;
import ae.recycler.be.model.*;
import lombok.*;

import java.util.*;

@Getter
@Setter
public class OrderFactory {
    private UUID id;
    private Integer boxes, pickupOrder;
    private Address pickupAddress;
    private Customer submittedBy;
    private OrderStatusEnum orderStatus;
    private Address deliveryAddresses;
    private Vehicle assignedVehicle;

    public OrderFactory setId(UUID id) {
        this.id = id;
        return this;
    }

    public OrderFactory setBoxes(Integer boxes) {
        this.boxes = boxes;
        return this;
    }

    public OrderFactory setPickupAddress(Address pickupAddress) {
        this.pickupAddress = pickupAddress;
        return this;
    }

    public OrderFactory setSubmittedBy(Customer submittedBy) {
        this.submittedBy = submittedBy;
        return this;
    }

    public OrderFactory setOrderStatus(OrderStatusEnum orderStatus) {
        this.orderStatus = orderStatus;
        return this;
    }

    public OrderFactory setDeliveryAddresses(Address deliveryAddresses) {
        this.deliveryAddresses = deliveryAddresses;
        return this;
    }

    public OrderFactory setAssignedVehicle(Vehicle assignedVehicle) {
        this.assignedVehicle = assignedVehicle;
        return this;
    }

    public OrderFactory setPickupOrder(int pickupOrder){
        this.pickupOrder = pickupOrder;
        return this;
    }

    public Order build(){
        return new Order(id, Optional.ofNullable(boxes).orElse(1),
                Optional.ofNullable(pickupAddress).orElse(GeocodedPlaces.BURJ_AL_ARAB),
                Optional.ofNullable(submittedBy).orElse(CustomerFactory.buildRandom()),
                Optional.ofNullable(orderStatus).orElse(OrderStatusEnum.SUBMITTED), pickupOrder, deliveryAddresses,
                assignedVehicle, null, null);}


}
