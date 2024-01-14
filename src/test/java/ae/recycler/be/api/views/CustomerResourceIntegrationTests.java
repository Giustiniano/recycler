package ae.recycler.be.api.views;

import ae.recycler.be.enums.OrderStatusEnum;
import ae.recycler.be.factories.AddressFactory;
import ae.recycler.be.factories.CustomerFactory;
import ae.recycler.be.factories.OrderFactory;
import ae.recycler.be.model.Address;
import ae.recycler.be.model.Customer;
import ae.recycler.be.model.Order;
import ae.recycler.be.service.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.Neo4jContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.publisher.Mono;

import java.util.*;

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
    private static final String CUSTOMER_ORDERS_ENDPOINT = CUSTOMER_ENDPOINT + "/%s/order";
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
        Customer customer = CustomerFactory.buildRandom();
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

    @Test
    public void testGetCustomerAddresses(){
        Customer customer = CustomerFactory.buildRandom();
        customer.getAddresses().add(AddressFactory.build());
        customer = customerRepository.save(customer).block();
        List<HashMap<String, Object>> customerAddresses = webTestClient.get()
                .uri(CUSTOMER_ADDRESS_ENDPOINT.formatted(customer.getId()))
                .accept(MediaType.APPLICATION_JSON).exchange().expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<List<HashMap<String, Object>>>() {}).returnResult()
                .getResponseBody();
        assert Objects.requireNonNull(customerAddresses).size() == 2;
    }
    @Test
    public void testGetCustomerOrders(){
        Customer customer = CustomerFactory.buildRandom();
        Order order = new OrderFactory().setPickupAddress(new Address(null, 1.0, 2.0, null, "Dubai", "Braih Street", "Dubai Marina", "10", "10")).build();
        order.setSubmittedBy(customer);
        order = orderRepository.save(order).block();
        String url = CUSTOMER_ORDERS_ENDPOINT.formatted(order.getSubmittedBy().getId());
        url += """
                ?orderStatuses=SUBMITTED,ASSIGNED,PICKING_UP""";
        List<HashMap<String, Object>> customerOrders = webTestClient.get()
                .uri(url).accept(MediaType.APPLICATION_JSON).exchange().expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<List<HashMap<String, Object>>>() {}).returnResult()
                .getResponseBody();
        assert Objects.requireNonNull(customerOrders).size() == 1;
        Map<String, Object> pickupAddress = (Map<String, Object>) customerOrders.get(0).get("pickupAddress");
        assert pickupAddress.get("lat") != null;
    }

    @Test
    public void testDeleteCustomerOrder(){
        Customer customer = CustomerFactory.buildRandom();
        Order order = new OrderFactory().build();
        order.setSubmittedBy(customer);
        order = orderRepository.save(order).block();
        String url = (CUSTOMER_ORDERS_ENDPOINT + "/%s").formatted(order.getSubmittedBy().getId(), order.getId());
        webTestClient.delete().uri(url).exchange().expectStatus().isOk();
        order = orderRepository.findById(order.getId()).block();
        assert order.getOrderStatus().equals(OrderStatusEnum.CANCELED);
    }

    @Test
    public void testDeleteCustomerOrderDifferentCustomer(){
        Customer customer = CustomerFactory.buildRandom();
        Order order = new OrderFactory().build();
        order.setSubmittedBy(customer);
        order = orderRepository.save(order).block();
        Customer wrongCustomer = CustomerFactory.buildRandom();
        customerRepository.save(wrongCustomer).block();
        String url = (CUSTOMER_ORDERS_ENDPOINT + "/%s").formatted(wrongCustomer.getId(), order.getId());
        HashMap<String, Object> error = webTestClient.delete().uri(url).exchange().expectStatus().isBadRequest()
                .expectBody(new ParameterizedTypeReference<HashMap<String, Object>>() {}).returnResult()
                .getResponseBody();
        assert error.get("status").equals("BAD_REQUEST");
        assert error.get("error").equals("Unable to cancel pickup");
        assert error.get("detail").equals("This order was submitted by another user");
        order = orderRepository.findById(order.getId()).block();
        assert order.getOrderStatus().equals(OrderStatusEnum.SUBMITTED);
    }

    @ParameterizedTest
    @EnumSource(value = OrderStatusEnum.class, names = {"CANCELED", "DELIVERED", "PICKED_UP", "DELIVERING"})
    public void testDeleteCustomerOrderWrongStatus(OrderStatusEnum orderStatus){
        Customer customer = CustomerFactory.buildRandom();
        Order order = new OrderFactory().setOrderStatus(orderStatus).build();
        order.setSubmittedBy(customer);
        order = orderRepository.save(order).block();
        String url = (CUSTOMER_ORDERS_ENDPOINT + "/%s").formatted(customer.getId(), order.getId());
        HashMap<String, Object> error = webTestClient.delete().uri(url).exchange().expectStatus().isBadRequest()
                .expectBody(new ParameterizedTypeReference<HashMap<String, Object>>() {}).returnResult()
                .getResponseBody();
        assert error.get("status").equals("BAD_REQUEST");
        assert error.get("error").equals("Unable to cancel pickup");
        assert error.get("detail").equals("This order cannot be canceled at this point");
        order = orderRepository.findById(order.getId()).block();
        assert order.getOrderStatus().equals(orderStatus);
    }
}
