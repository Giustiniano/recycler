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
}
