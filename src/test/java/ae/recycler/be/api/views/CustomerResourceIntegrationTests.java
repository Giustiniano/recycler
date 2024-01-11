package ae.recycler.be.api.views;

import ae.recycler.be.api.views.serializers.NewOrderRequest;
import ae.recycler.be.api.views.serializers.OrderResponse;
import ae.recycler.be.factories.AddressFactory;
import ae.recycler.be.factories.CustomerFactory;
import ae.recycler.be.model.Address;
import ae.recycler.be.model.Customer;
import ae.recycler.be.service.repository.*;
import ae.recycler.be.service.repository.here.HereAPIRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.Neo4jContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public class CustomerResourceIntegrationTests {
    @Container
    static Neo4jContainer<?> neo4j = new Neo4jContainer<>("neo4j:5.11-community").withAdminPassword("verysecret").withExposedPorts(7687,7474);
    @Autowired
    private WebTestClient webTestClient;
    @Autowired
    private AddressRepository addressRepository;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private VehicleRepository vehicleRepository;
    @Autowired
    private DriverRepository driverRepository;
    @DynamicPropertySource
    static void neo4jProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.neo4j.uri", neo4j::getBoltUrl);
        registry.add("spring.neo4j.authentication.username", () -> "neo4j");
        registry.add("spring.neo4j.authentication.password", () -> neo4j.getAdminPassword());
//      registry.add("spring.kafka.producer.bootstrap-servers", () -> kafka.getBootstrapServers());
        registry.add("here.api-key",() -> "r0uRb7i-hy2B-l9t8m-YpzlD_I3JoDrWB9Afm9ZIji8");
    }


    private static final String CUSTOMER_ENDPOINT = "/api/v1/customer";
    private static final String CUSTOMER_ADDRESS_ENDPOINT = CUSTOMER_ENDPOINT + "/%s/address";
    @BeforeEach
    public void beforeEach() {
        try(var driver = GraphDatabase.driver(neo4j.getBoltUrl(), AuthTokens
                .basic("neo4j", neo4j.getAdminPassword()))){
            try(var session = driver.session()){
                session.executeWrite(tx -> {
                    var query = new Query("MATCH (a) DETACH DELETE a");
                    tx.run(query);
                    return null;
                });
            }
        }
    }

    @Test
    public void testSaveCustomerAddress(){
        Customer customer = CustomerFactory.build();
        customer = customerRepository.save(customer).block();
        Map<String, Object> newAddress = new HashMap<>();
        newAddress.put("lat", 1);
        newAddress.put("lng", 2);
        newAddress.put("emirate", "Dubai");
        newAddress.put("streetName", "Braih street");
        newAddress.put("area", "Dubai Marina");

        Map<String, Object> savedAddress = webTestClient.post()
                .uri(CUSTOMER_ADDRESS_ENDPOINT.formatted(customer.getId())).contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(newAddress), HashMap.class).accept(MediaType.APPLICATION_JSON).exchange().expectStatus()
                .isCreated()
                .expectBody(new ParameterizedTypeReference<HashMap<String, Object>>() {}).returnResult()
                .getResponseBody();
        assert savedAddress.get("id") != null;
        customer = customerRepository.findById(customer.getId()).block();
        assert customer.getAddresses().get(0).getId().equals(UUID.fromString((String) savedAddress.get("id")));
    }

    @Test
    public void testSaveCustomerAddressCustomerDoesNotExist(){
        Map<String, Object> newAddress = new HashMap<>();
        newAddress.put("lat", 1);
        newAddress.put("lng", 2);
        newAddress.put("emirate", "Dubai");
        newAddress.put("streetName", "Braih street");
        newAddress.put("area", "Dubai Marina");

        Map<String, Object> savedAddress = webTestClient.post()
                .uri(CUSTOMER_ADDRESS_ENDPOINT.formatted(UUID.randomUUID())).contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(newAddress), HashMap.class).accept(MediaType.APPLICATION_JSON).exchange().expectStatus()
                .isCreated()
                .expectBody(new ParameterizedTypeReference<HashMap<String, Object>>() {}).returnResult()
                .getResponseBody();
        assert savedAddress.isEmpty();
    }
}
