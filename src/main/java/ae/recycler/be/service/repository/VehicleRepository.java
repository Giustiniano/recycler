package ae.recycler.be.service.repository;

import ae.recycler.be.model.Order;
import ae.recycler.be.model.Vehicle;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface VehicleRepository extends ReactiveCrudRepository<Vehicle, UUID> {
    @Query("""
            MATCH (newOrder {id:$newOrder})-[:PICKUP_FROM]->(pf:Address)
            MATCH (v:Vehicle)
            OPTIONAL MATCH (o:Order)-[:PICKUP_WITH]->(v)
            WHERE v.status IN ["AT_DEPOSIT", "PICKING_UP"] AND
            reduce(newOrder.boxes, collect(o.boxes)) <= v.capacity
            RETURN v
            ORDER BY distance(
                    point({latitude:newOrder.pickupAddress.lat, longitude:newOrder.pickupAddress.lng}),
                    point({latitude:v.lat, longitude: v.lng})
                ) ASC LIMIT 1
            """)
    Vehicle findClosestVehicle(@Param("newOrder") UUID newOrder);
    @Query("""
    MATCH (v:Vehicle {id:$vehicleId})
    SET v.status = "AT_DEPOSIT"
    OPTIONAL MATCH (pendingOrders:Order)-[pw:PICKUP_WITH]->(v) WHERE pendingOrders.orderStatus = "PICKING_UP" OR pendingOrders.orderStatus = "ASSIGNED"
    SET pendingOrders.orderStatus = "SUBMITTED";
    DELETE pw
    WITH v return v;
    """)
    Mono endShift(@Param("vehicleId") UUID vehicleUUID);
}
