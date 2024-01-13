package ae.recycler.be.service.repository;

import ae.recycler.be.model.Address;
import ae.recycler.be.model.Customer;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.UUID;

public interface CustomerRepository extends ReactiveCrudRepository<Customer, UUID> {

    @Query("""
            MATCH (c:Customer {id:$customerId})-[:HAS_ADDRESS]->(a:Address {id:$addressId}) RETURN
            a.lat as lat, a.lng as lng, a.id as id,
            a.humanReadableAddress as humanReadableAddress
            """)
    Mono<Address> findCustomerAddress(@Param("customerId") UUID customerId, @Param("addressId") UUID addressId);

    @Query("""
        MATCH (c:Customer {id:$customerId})
        CREATE (c)-[:HAS_ADDRESS]->(newAddress:Address) SET newAddress = $address SET newAddress.id = randomUUID()
        RETURN newAddress.id as id, newAddress.lat as lat, newAddress.lng as lng, newAddress.emirate as emirate,
        newAddress.streetName as streetName, newAddress.area as area, newAddress.houseOrAptNumber as houseOrAptNumber,
        newAddress.floor as floor
    """)
    Mono<Address> saveNewCustomerAddress(@Param("customerId") UUID customerID, @Param("address") Map<String, Object> address);

}
