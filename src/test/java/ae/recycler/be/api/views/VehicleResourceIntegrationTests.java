package ae.recycler.be.api.views;

import ae.recycler.be.enums.OrderStatusEnum;
import ae.recycler.be.factories.OrderFactory;
import ae.recycler.be.factories.VehicleFactory;
import ae.recycler.be.model.Order;
import ae.recycler.be.model.Vehicle;
import ae.recycler.be.service.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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

import java.util.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)

@Testcontainers
public class VehicleResourceIntegrationTests {
    @Container
    static Neo4jContainer<?> neo4j = new Neo4jContainer<>("neo4j:5.11-community").withAdminPassword("verysecret").withExposedPorts(7687,7474);
    @Container
    static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.5.0"));
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
        registry.add("spring.kafka.producer.bootstrap-servers", () -> kafka.getBootstrapServers());
        registry.add("here.api-key",() -> "r0uRb7i-hy2B-l9t8m-YpzlD_I3JoDrWB9Afm9ZIji8");
    }


    @BeforeEach
    public void beforeEach() {
        try(Driver driver = GraphDatabase.driver(neo4j.getBoltUrl(), AuthTokens
                .basic("neo4j", neo4j.getAdminPassword()))){
            try(Session session = driver.session()){
                session.executeWrite(tx -> {
                    var query = new Query("MATCH (a) DETACH DELETE a");
                    tx.run(query);
                    return null;
                });
            }
        }
    }

    private static String updateAssignedOrderEndpoint = "/api/v1/vehicle/%s/orders/%s";

    @Test
    public void updateAssignedOrder(){

        Vehicle vehicle = new VehicleFactory().build();
        Order order = new OrderFactory().build();
        List<Order> assignedOrders = new ArrayList<>();
        assignedOrders.add(order);
        vehicle.setAssignedOrders(assignedOrders);
        vehicleRepository.save(vehicle).block();
        Map<String, Object> request = Map.ofEntries(Map.entry("newStatus", "PICKING_UP"));
        Map<String, Object> response = webTestClient.put().uri(String.format(updateAssignedOrderEndpoint, vehicle.getId(), order.getId()))
                .body(Mono.just(request), request.getClass()).accept(MediaType.APPLICATION_JSON).exchange().expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<HashMap<String, Object>>() {}).returnResult()
                .getResponseBody();
        assert orderRepository.findById(order.getId()).block().getOrderStatus().equals(OrderStatusEnum.PICKING_UP);
    }

    @Test
    public void findAssignedOrder(){
        Vehicle vehicle = new VehicleFactory().build();
        Order order = new OrderFactory().build();
        List<Order> assignedOrders = new ArrayList<>();
        assignedOrders.add(order);
        vehicle.setAssignedOrders(assignedOrders);
        vehicleRepository.save(vehicle).block();
        order = vehicle.getAssignedOrders().get(0);
        Map<String, Object> response = webTestClient.get()
                .uri(String.format(updateAssignedOrderEndpoint, vehicle.getId(), order.getId()))
                .accept(MediaType.APPLICATION_JSON).exchange().expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<HashMap<String, Object>>() {})
                .returnResult().getResponseBody();
        assert UUID.fromString((String) response.get("orderId")).equals(order.getId());
        assert UUID.fromString((String) response.get("customerId")).equals(order.getSubmittedBy().getId());
        assert response.get("boxes").equals(order.getBoxes());
        Map<String, Object> actualAddress = (Map<String, Object>) response.get("pickupAddress");
        assert actualAddress.get("lat").equals(order.getPickupAddress().getLat());
        assert actualAddress.get("lng").equals(order.getPickupAddress().getLng());
        assert actualAddress.get("humanReadableAddress").equals(order.getPickupAddress().getHumanReadableAddress());

    }
    @Test
    public void findUnassignedOrder(){
        Vehicle vehicle = new VehicleFactory().build();
        vehicleRepository.save(vehicle).block();
        Order order = orderRepository.save(new OrderFactory().build()).block();
        webTestClient.get()
                .uri(String.format(updateAssignedOrderEndpoint, vehicle.getId(), order.getId()))
                .accept(MediaType.APPLICATION_JSON).exchange().expectStatus().isNotFound();
    }
}
