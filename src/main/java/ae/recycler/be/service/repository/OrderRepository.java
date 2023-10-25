package ae.recycler.be.service.repository;

import ae.recycler.be.enums.OrderStatusEnum;
import ae.recycler.be.model.Order;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

public interface OrderRepository extends ReactiveCrudRepository<Order, UUID> {


    Mono<List<Order>> findByOrderStatusOrderByCreatedAtAsc(OrderStatusEnum status);

}
