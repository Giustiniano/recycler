package ae.recycler.be.service.repository;

import ae.recycler.be.enums.OrderStatusEnum;
import ae.recycler.be.model.Customer;
import ae.recycler.be.model.Order;
import ae.recycler.be.model.Vehicle;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

public interface OrderRepository extends ReactiveCrudRepository<Order, UUID> {


    Flux<Order> findOrdersByOrderStatusOrderByCreatedDateAsc(OrderStatusEnum status);
    @Query("""
            MATCH (v:Vehicle {id:$vehicleId})<-[r_pw:PICKUP_WITH]-(`order`:Order)
            MATCH (`order`)-[r_pf:PICKUP_FROM]->(pickupAddress:Address)
            MATCH (`order`)-[r_sb:SUBMITTED_BY]->(submitter:Customer)
            RETURN `order` ORDER BY order.pickupOrder ASC, v, r_pw, r_pf, pickupAddress, submitter
            """)
    Flux<Order> findOrdersByAssignedVehicleOrderByPickupOrderAsc(@Param("vehicleId") UUID vehicleId);

    @Query("""
            MATCH (v:Vehicle {id:$vehicleId})
            OPTIONAL MATCH (assigned_order_address:Address)<-[r_assigned_order_address:PICKUP_FROM]-
            (assigned_order:Order {orderStatus: "ASSIGNED"})-[r_ao_pw:PICKUP_WITH]->(v)
            OPTIONAL MATCH (assigned_order_customer:Customer)<-[r_assigned_order_submitter:SUBMITTED_BY]-(assigned_order)
            OPTIONAL MATCH (current_order_address)<-[r_current_order_address:PICKUP_FROM]-
            (current_order:Order {orderStatus: "PICKING_UP"})-[r_co_pw:PICKUP_WITH]->(v)
            OPTIONAL MATCH (current_order_customer:Customer)<-[r_current_order_submitter:SUBMITTED_BY]-(current_order)
            WITH current_order_address, r_current_order_address, current_order, r_co_pw, assigned_order_address,
            r_assigned_order_address, v, r_ao_pw, current_order_customer, assigned_order_customer,
            r_assigned_order_submitter, r_current_order_submitter,
            assigned_order ORDER BY assigned_order.pickupOrder ASC LIMIT 1
            SET coalesce(current_order, assigned_order).orderStatus = "PICKING_UP"
            return v, coalesce(current_order_address, assigned_order_address),
            coalesce(current_order_customer, assigned_order_customer),
            coalesce(r_current_order_submitter, r_assigned_order_submitter),
            coalesce(r_current_order_address, r_assigned_order_address), coalesce(current_order, assigned_order),
            coalesce(r_co_pw, r_ao_pw)
            """)
    Mono<Order> findOrderBeingPickedUpOrNextToPickup(@Param("vehicleId") UUID vehicleId);
    @Query("""
        MATCH (c:Customer {id:$customerId})<-[r_submitted_by:SUBMITTED_BY]-(o:Order)-[r_pickup_from:PICKUP_FROM]->
        (pickup_from:Address) WHERE o.orderStatus in $orderStatuses
        RETURN c, r_submitted_by, o, r_pickup_from, pickup_from ORDER BY o.createdDate
    """)
    Flux<Order> findOrderBySubmittedByOrderByCreatedDateAsc(UUID customerId, List<OrderStatusEnum> orderStatuses);

    @Query("""
        MATCH (c:Customer {id:$customerId})<-[r_submitted_by:SUBMITTED_BY]-(o:Order)-[r_pickup_from:PICKUP_FROM]->
        (pickup_from:Address {id:$addressId}) WHERE o.orderStatus in $orderStatuses
        RETURN c, r_submitted_by, o, r_pickup_from, pickup_from ORDER BY o.createdDate
    """)
    Flux<Order> findOrdersBySubmittedByAndAddress(@Param("customerId") UUID customerId,
                                                  @Param("addressId") UUID addressId,
                                                  @Param("orderStatuses") List<OrderStatusEnum> orderStatuses);
}
