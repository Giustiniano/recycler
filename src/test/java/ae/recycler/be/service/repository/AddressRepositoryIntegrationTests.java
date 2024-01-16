package ae.recycler.be.service.repository;

import ae.recycler.be.model.Address;
import ae.recycler.be.model.Customer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.Neo4jContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
@Testcontainers
@SpringBootTest
public class AddressRepositoryIntegrationTests {
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
        Address address = new Address(null, 1.0, 2.0, null,
                "Dubai", "Braih Street", "Dubai Marina",
                "1010",  "10", "Home");

        Customer customer = customerRepository.save(Customer.builder().email("some_email@example.com")
                .addresses(List.of(address)).build()).block();
        var customerAddress = addressRepository.findCustomerAddress(customer.getId(), address.getId()).block();
        assert customerAddress != null;
        assert customerAddress.equals(address);
    }
    @Test
    public void testSaveCustomerAddress(){
        Address address = new Address(null, 1.0, 2.0, null,
                "Dubai", "Braih Street", "Dubai Marina",
                "1010",  "10", "Home");

        Customer customer = customerRepository.save(Customer.builder().email("some_email@example.com").build()).block();
        Address newAddress = addressRepository.saveNewCustomerAddress(customer.getId(), address.toMap()).block();
        assert customerRepository.findById(customer.getId()).block().getAddresses().get(0).equals(newAddress);

    }
}
