package ae.recycler.be.service.repository;

import ae.recycler.be.model.Address;
import ae.recycler.be.model.Customer;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface CustomerRepository extends ReactiveCrudRepository<Customer, UUID> {

    @Query("""
            MATCH (c:Customer {id:$customerId})-[:HAS_ADDRESS]->(a:Address {id:$addressId}) RETURN
            a.geolocation as geolocation, a.id as id,
            a.humanReadableAddress as humanReadableAddress
            """)
    Mono<Address> findCustomerAddress(@Param("customerId") UUID customerId, @Param("addressId") UUID addressId);

}
