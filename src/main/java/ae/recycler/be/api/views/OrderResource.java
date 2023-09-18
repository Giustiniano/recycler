package ae.recycler.be.api.views;

import ae.recycler.be.api.exceptions.Exceptions;
import ae.recycler.be.api.views.serializers.OrderRequest;
import ae.recycler.be.api.views.serializers.OrderResponse;
import ae.recycler.be.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;


@RestController
@RequestMapping("/api/v1/order")
public class OrderResource {
    @Autowired
    private OrderService orderService;
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE,consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public Mono<OrderResponse> submitOrder(@RequestBody Mono<OrderRequest> orderBody){
        return orderService.saveOrder(orderBody).switchIfEmpty(Mono.error(new RuntimeException("Unable to save order")))
                .flatMap(OrderResponse::fromOrder);
    }

    @GetMapping(value = "{id}")
    public Mono<OrderResponse> getOrder(@PathVariable String id) {
        UUID orderId = null;
        try {
            orderId = UUID.fromString(id);
            return Mono.just(orderId).flatMap(oid -> orderService.findById(Mono.just(oid))).flatMap(OrderResponse::fromOrder)
                    .switchIfEmpty(Mono.error(new Exceptions.ResourceNotFoundException(
                            String.format("Order with id %s was not found", orderId))));
        }
        catch (IllegalArgumentException ex){
           return Mono.error(new Exceptions.BadRequestException("The order id is not a valid UUID"));
        }

    }
}

