package ae.recycler.be.api.views.order;

import ae.recycler.be.api.exceptions.ResourceNotFoundException;
import ae.recycler.be.api.exceptions.UnprocessableEntityException;
import ae.recycler.be.api.views.serializers.NewOrderRequest;
import ae.recycler.be.api.views.serializers.OrderResponse;
import ae.recycler.be.api.views.serializers.OrderUpdateRequest;
import ae.recycler.be.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
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
    public Mono<OrderResponse> submitOrder(@RequestBody Mono<NewOrderRequest> orderBody){
        return orderService.saveOrder(orderBody).switchIfEmpty(Mono.error(new RuntimeException("Unable to save order")))
                .flatMap(OrderResponse::fromOrder);
    }

    @GetMapping(value = "{id}")
    public Mono<OrderResponse> getOrder(@PathVariable String id) {
        UUID orderId = Validators.validateOrderId(id);
        return Mono.just(orderId).flatMap(oid -> orderService.findById(Mono.just(oid))).flatMap(OrderResponse::fromOrder)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException(
                        String.format("Order with id '%s' was not found", id))));
    }

    @PatchMapping(value = "{id}", produces = MediaType.APPLICATION_JSON_VALUE,consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<OrderResponse> updateOrder(@PathVariable String id, @RequestBody Mono<OrderUpdateRequest> orderUpdate){
        UUID orderId = Validators.validateOrderId(id);
        return orderUpdate.flatMap(orderUpdate1 -> orderService.updateOrder(orderId, orderUpdate1)).
                onErrorMap(IllegalStateException.class, ex -> new UnprocessableEntityException(ex.getMessage(), null, ex))
                .flatMap(OrderResponse::fromOrder);
    }


}

