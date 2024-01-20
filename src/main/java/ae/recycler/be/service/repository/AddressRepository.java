package ae.recycler.be.service.repository;

import ae.recycler.be.model.Address;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.UUID;

public interface AddressRepository extends ReactiveCrudRepository<Address, UUID> {

    @Query("""
            MATCH (c:Customer {id:$customerId})-[:HAS_ADDRESS]->(a:Address {id:$addressId}) RETURN a
            """)
    Mono<Address> findCustomerAddress(@Param("customerId") UUID customerId, @Param("addressId") UUID addressId);
    @Query("""
        MATCH (c:Customer {id:$customerId})
        CREATE (c)-[:HAS_ADDRESS]->(newAddress:Address) SET newAddress = $address SET newAddress.id = randomUUID()
        RETURN newAddress;
    """)
    Mono<Address> saveNewCustomerAddress(@Param("customerId") UUID customerID,
                                         @Param("address") Map<String, Object> address);

    @Query("""        
        MATCH (c:Customer {id:$customerId})-[:HAS_ADDRESS]->(a:Address {id:$addressId})
        OPTIONAL MATCH (a)<-[:PICKUP_FROM]-(o:Order {orderStatus:"PICKING_UP"})-[:SUBMITTED_BY]->(c)
        WITH count(o) = 0 as addressNotUsed, a
        CALL apoc.do.when(addressNotUsed,
        'MATCH (address) DETACH DELETE address RETURN address',
        'MATCH (address) RETURN address', {address:a})
        YIELD value RETURN a;
    """)
    Mono<Address> deleteAddress(@Param("customerId") UUID customerId, @Param("addressId") UUID addressId);
}
