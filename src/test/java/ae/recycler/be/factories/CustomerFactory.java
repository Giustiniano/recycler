package ae.recycler.be.factories;

import ae.recycler.be.model.Address;
import ae.recycler.be.model.Customer;
import lombok.Builder;

import java.util.ArrayList;
import java.util.List;

@Builder
public class CustomerFactory {
    private String personalEmail;
    private List<Address> personalAddresses;
    public static Customer buildRandom(){
        List<Address> addresses = new ArrayList<>(1);
        addresses.add(AddressFactory.build());
        return Customer.builder().email(EmailFactory.personalEmail()).addresses(addresses).build();
    }

}
