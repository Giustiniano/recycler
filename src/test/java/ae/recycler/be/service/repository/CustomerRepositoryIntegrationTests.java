package ae.recycler.be.service.repository;

import ae.recycler.be.model.Address;
import ae.recycler.be.model.Customer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.Neo4jContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
@Testcontainers
public class CustomerRepositoryIntegrationTests {
    @Container
    static Neo4jContainer<?> neo4j = new Neo4jContainer<>("neo4j:5.11-community").withAdminPassword("verysecret").withExposedPorts(7687,7474);
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private AddressRepository addressRepository;
    @DynamicPropertySource
    static void neo4jProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.neo4j.uri", neo4j::getBoltUrl);
        registry.add("spring.neo4j.authentication.username", () -> "neo4j");
        registry.add("spring.neo4j.authentication.password", () -> neo4j.getAdminPassword());
    }


    @BeforeEach
    public void beforeEach() {
        customerRepository.deleteAll().block();
    }


    @Test
    public void testFindUserAddress(){
        Address address = addressRepository.save(Address.builder().humanReadableAddress("Gotham City").geolocation("123").build()).block();
        Customer customer = customerRepository.save(Customer.builder().email("some_email@example.com")
                .addresses(List.of(address)).build()).block();
        var customerAddress = customerRepository.findCustomerAddress(customer.getId(), address.getId()).block();
        assert customerAddress != null;
        assert customerAddress.equals(address);
    }
}
