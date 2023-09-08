package ae.recycler.be.api.views;

import ae.recycler.be.api.views.serializers.OrderBody;
import ae.recycler.be.model.Order;
import ae.recycler.be.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.UUID;


@RestController
@RequestMapping("/order")
public class OrderResource {
    @Autowired
    private OrderService orderService;
    @PostMapping
    public Mono<ServerResponse> post(@RequestBody Mono<OrderBody> orderBody){
        Mono<Order> saved = orderService.saveOrder(orderBody);
        return saved.doOnError(c -> {
            System.out.println(c.getMessage());
        }).flatMap(saved1 -> {
            System.out.println(saved1);
            return ServerResponse.ok().body(BodyInserters.fromValue(saved1.getBoxes()));
        });
    }

}

