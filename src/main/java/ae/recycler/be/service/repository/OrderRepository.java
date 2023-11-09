package ae.recycler.be.service.repository;

import ae.recycler.be.enums.OrderStatusEnum;
import ae.recycler.be.model.Order;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

public interface OrderRepository extends ReactiveCrudRepository<Order, UUID> {


    Flux<Order> findOrdersByOrderStatusOrderByCreatedDateAsc(OrderStatusEnum status);

}
