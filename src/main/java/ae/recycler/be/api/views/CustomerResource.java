package ae.recycler.be.api.views;

import ae.recycler.be.api.views.serializers.Address;
import ae.recycler.be.api.views.serializers.NewOrderRequest;
import ae.recycler.be.api.views.serializers.OrderResponse;
import ae.recycler.be.model.Order;
import ae.recycler.be.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/customer")
public class CustomerResource {

    @Autowired
    private CustomerService customerService;
    @PostMapping(value = "{id}/address", produces = MediaType.APPLICATION_JSON_VALUE,consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Address> saveNewCustomerAddress(@PathVariable String id, @RequestBody Mono<Address> customerAddress){

        UUID customerUUID = Validators.validateId(id, "Customer id is not a valid UUID");
        return customerAddress.flatMap(ca -> customerService.saveCustomerAddress(customerUUID, ca));
    }

    @GetMapping(value = "{id}/address", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public Mono<List<Address>> getCustomerAddresses(@PathVariable String id){

        UUID customerUUID = Validators.validateId(id, "Customer id is not a valid UUID");
        return customerService.getCustomerAddresses(customerUUID);
    }

    @GetMapping(value = "{id}/order", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public Mono<List<OrderResponse>> getCustomerOrders(@PathVariable String id){

        UUID customerUUID = Validators.validateId(id, "Customer id is not a valid UUID");
        return customerService.getCustomerOrders(customerUUID);
    }

}
