package ae.recycler.be.api.views;

import ae.recycler.be.api.exceptions.BadRequestException;
import ae.recycler.be.api.views.serializers.JsonAddress;
import ae.recycler.be.api.views.serializers.OrderResponse;
import ae.recycler.be.enums.OrderStatusEnum;
import ae.recycler.be.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/customer")
public class CustomerResource {

    @Autowired
    private CustomerService customerService;
    @PostMapping(value = "{id}/address", produces = MediaType.APPLICATION_JSON_VALUE,consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<JsonAddress> saveNewCustomerAddress(@PathVariable String id, @RequestBody Mono<JsonAddress> customerAddress){

        UUID customerUUID = Validators.validateId(id, "Customer id is not a valid UUID");
        return customerAddress.flatMap(ca -> customerService.saveCustomerAddress(customerUUID, ca));
    }

    @GetMapping(value = "{id}/address", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public Mono<List<JsonAddress>> getCustomerAddresses(@PathVariable String id){

        UUID customerUUID = Validators.validateId(id, "Customer id is not a valid UUID");
        return customerService.getCustomerAddresses(customerUUID);
    }

    @DeleteMapping(value = "{id}/address/{addressId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public Mono<?> getCustomerAddresses(@PathVariable String id, @PathVariable String addressId){

        UUID customerUUID = Validators.validateId(id, "Customer id is not a valid UUID");
        UUID addressUUID = Validators.validateId(id, "Address id is not a valid UUID");
        return customerService.deleteCustomerAddress(customerUUID, addressUUID)
                .onErrorMap(IllegalArgumentException.class, ex ->
                new BadRequestException("Unable to delete address", ex.getMessage(), ex));

    }

    @DeleteMapping(value = "{id}/order/{orderId}")
    @ResponseStatus(HttpStatus.OK)
    public Mono<?> cancelCustomerOrderPickup(@PathVariable String id, @PathVariable String orderId){
        UUID customerUUID = Validators.validateId(id, "Customer id is not a valid UUID");
        UUID orderUUID = Validators.validateId(orderId, "Customer id is not a valid UUID");
        return customerService.cancelCustomerOrderPickup(customerUUID, orderUUID)
                .onErrorMap(IllegalArgumentException.class, ex ->
                new BadRequestException("Unable to cancel pickup", ex.getMessage(), ex));
    }

    @GetMapping(value = "{id}/order", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public Mono<List<OrderResponse>> getCustomerOrders(@PathVariable String id,
                                                       @RequestParam(value = "orderStatuses", required = false)
                                                       List<String> orderStatuses){

        UUID customerUUID = Validators.validateId(id, "Customer id is not a valid UUID");
        List<OrderStatusEnum> orderStatusesEnum;
        if(orderStatuses == null){
            orderStatusesEnum = Arrays.stream(OrderStatusEnum.values()).toList();
        }
        else {
            try{
               orderStatusesEnum = orderStatuses.stream().map(OrderStatusEnum::valueOf)
                       .collect(Collectors.toList());
            }
            catch(IllegalArgumentException ex){
                throw new BadRequestException("Not all the supplied order statuses are valid", ex.getMessage(), ex);
            }
        }
        return customerService.getCustomerOrders(customerUUID, orderStatusesEnum);
    }



}
