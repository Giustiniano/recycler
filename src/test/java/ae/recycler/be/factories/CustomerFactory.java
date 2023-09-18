package ae.recycler.be.factories;

import ae.recycler.be.model.Customer;

import java.util.List;

public class CustomerFactory {

    public static Customer build(){
        return Customer.builder().email(EmailFactory.personalEmail()).addresses(List.of(AddressFactory.build()))
                .build();
    }
}
