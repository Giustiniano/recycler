package ae.recycler.be.api.views;

import ae.recycler.be.api.views.serializers.OrderRequest;
import ae.recycler.be.api.views.serializers.OrderResponse;
import ae.recycler.be.model.Order;
import ae.recycler.be.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.net.URI;


@RestController
@RequestMapping("/api/v1/order")
public class OrderResource {
    @Autowired
    private OrderService orderService;
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE,consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public Mono<OrderResponse> post(@RequestBody Mono<OrderRequest> orderBody){
        return orderService.saveOrder(orderBody).switchIfEmpty(Mono.error(new RuntimeException("Unable to save order"))).flatMap(savedOrder -> {
            return Mono.just(OrderResponse.fromOrder(savedOrder));
        });

    }

}

