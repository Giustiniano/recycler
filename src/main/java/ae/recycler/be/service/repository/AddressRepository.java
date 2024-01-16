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
    Mono<Address> saveNewCustomerAddress(@Param("customerId") UUID customerID, @Param("address") Map<String, Object> address);
}
