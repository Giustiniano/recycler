package ae.recycler.be.service.repository;

import ae.recycler.be.model.Order;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface OrderRepository extends ReactiveCrudRepository<Order, UUID> {

}
