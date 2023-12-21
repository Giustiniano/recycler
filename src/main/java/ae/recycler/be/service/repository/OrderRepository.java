package ae.recycler.be.service.repository;

import ae.recycler.be.enums.OrderStatusEnum;
import ae.recycler.be.model.Order;
import ae.recycler.be.model.Vehicle;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface OrderRepository extends ReactiveCrudRepository<Order, UUID> {


    Flux<Order> findOrdersByOrderStatusOrderByCreatedDateAsc(OrderStatusEnum status);
    Flux<Order> findOrdersByAssignedVehicleAndOrderStatusOrderByPickupOrderAsc(Vehicle assignedVehicle,
                                                                               OrderStatusEnum orderStatusEnum);

    @Query("""
            MATCH (v:Vehicle {id:$vehicleId})
            OPTIONAL MATCH (assigned_order_address:Address)<-[r_assigned_order_address:PICKUP_FROM]-
            (assigned_order:Order {orderStatus: "ASSIGNED"})-[r_ao_pw:PICKUP_WITH]->(v)
            OPTIONAL MATCH (current_order_address)<-[r_current_order_address:PICKUP_FROM]-
            (current_order:Order {orderStatus: "PICKING_UP"})-[r_co_pw:PICKUP_WITH]->(v)
            WITH current_order_address, r_current_order_address, current_order, r_co_pw, assigned_order_address,
            r_assigned_order_address, v, r_ao_pw, assigned_order ORDER BY assigned_order.pickupOrder ASC LIMIT 1
            return v, coalesce(current_order_address, assigned_order_address), 
            coalesce(r_current_order_address, r_assigned_order_address), coalesce(current_order, assigned_order),
            coalesce(r_co_pw, r_ao_pw)
            """)
    Mono<Order> findOrderBeingPickedUpOrNextToPickup(@Param("vehicleId") UUID vehicleId);





}
