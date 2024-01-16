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

    private static final String assignedOrderEndpoint = "/api/v1/vehicle/%s/orders/%s";
    private static final String nextOrderToPickupEndpoint = "/api/v1/vehicle/%s/orders/next";

    @Test
    public void updateAssignedOrder(){

        Vehicle vehicle = new VehicleFactory().build();
        Order order = new OrderFactory().build();
        List<Order> assignedOrders = new ArrayList<>();
        assignedOrders.add(order);
        vehicle.setAssignedOrders(assignedOrders);
        vehicleRepository.save(vehicle).block();
        Map<String, Object> request = Map.ofEntries(Map.entry("newStatus", "PICKED_UP"));
        Map<String, Object> response = webTestClient.put().uri(String.format(assignedOrderEndpoint, vehicle.getId(), order.getId()))
                .body(Mono.just(request), request.getClass()).accept(MediaType.APPLICATION_JSON).exchange().expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<HashMap<String, Object>>() {}).returnResult()
                .getResponseBody();
        assert orderRepository.findById(order.getId()).block().getOrderStatus().equals(OrderStatusEnum.PICKED_UP);
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
                .uri(String.format(assignedOrderEndpoint, vehicle.getId(), order.getId()))
                .accept(MediaType.APPLICATION_JSON).exchange().expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<HashMap<String, Object>>() {})
                .returnResult().getResponseBody();
        assert UUID.fromString((String) response.get("orderId")).equals(order.getId());
        assert UUID.fromString((String) response.get("customerId")).equals(order.getSubmittedBy().getId());
        assert response.get("boxes").equals(order.getBoxes());
        Map<String, Object> actualAddress = (Map<String, Object>) response.get("pickupAddress");
        assert actualAddress.get("lat").equals(order.getPickupAddress().getLat());
        assert actualAddress.get("lng").equals(order.getPickupAddress().getLng());
        assert actualAddress.get("humanReadableAddress").equals(order.getPickupAddress().getBuildingOrPlaceName());

    }
    @Test
    public void findUnassignedOrder(){
        Vehicle vehicle = new VehicleFactory().build();
        vehicleRepository.save(vehicle).block();
        Order order = orderRepository.save(new OrderFactory().build()).block();
        webTestClient.get()
                .uri(String.format(assignedOrderEndpoint, vehicle.getId(), order.getId()))
                .accept(MediaType.APPLICATION_JSON).exchange().expectStatus().isNotFound();
    }
    @Test
    public void getNextOrderToPickupNoOrderBeingPickedUp(){
        List<Order> assignedOrder = new ArrayList<>();
        for (int i = 1; i<3; i++){
            assignedOrder.add(new OrderFactory().setPickupOrder(i).setOrderStatus(OrderStatusEnum.ASSIGNED).setBoxes(i).build());
        }
        Vehicle v = new VehicleFactory().setAssignedOrders(assignedOrder).build();
        vehicleRepository.save(v).block();
        Map<String, Object> response = webTestClient.get()
                .uri(String.format(nextOrderToPickupEndpoint, v.getId())).accept(MediaType.APPLICATION_JSON).exchange()
                .expectStatus().isOk().expectBody(new ParameterizedTypeReference<HashMap<String, Object>>() {})
                .returnResult().getResponseBody();
        Order expectedOrder = assignedOrder.stream().filter(o -> o.getPickupOrder().equals(1)).findFirst().get();
        assert UUID.fromString((String) response.get("id")).equals(expectedOrder.getId());
        assert response.get("boxes").equals(expectedOrder.getBoxes());
        assert response.get("orderStatus").equals("PICKING_UP");
        Map<String, Object> responsePickupAddress = ((Map<String, Object>) response.get("pickupAddress"));
        assert responsePickupAddress.get("lat").equals(expectedOrder.getPickupAddress().getLat());
        assert responsePickupAddress.get("lng").equals(expectedOrder.getPickupAddress().getLng());
        assert responsePickupAddress.get("humanReadableAddress").equals(expectedOrder.getPickupAddress()
                .getBuildingOrPlaceName());
        assert UUID.fromString((String) response.get("customerId")).equals(expectedOrder.getSubmittedBy().getId());


        Order returnedOrder = orderRepository.findById(Mono.just(UUID.fromString((String) response.get("id")))).block();
        assert returnedOrder.getPickupOrder() == 1;
        assert returnedOrder.getOrderStatus().equals(OrderStatusEnum.PICKING_UP);
        assert returnedOrder.getAssignedVehicle().equals(v);
        assert returnedOrder.getSubmittedBy().equals(expectedOrder.getSubmittedBy());
        assert returnedOrder.getPickupAddress().equals(expectedOrder.getPickupAddress());

    }

    @Test
    public void getNextOrderToPickupGetOrderBeingPickedUp(){
        List<Order> assignedOrder = new ArrayList<>();
        assignedOrder.add(new OrderFactory().setPickupOrder(1).setOrderStatus(OrderStatusEnum.PICKING_UP).build());
        assignedOrder.add(new OrderFactory().setPickupOrder(2).setOrderStatus(OrderStatusEnum.ASSIGNED).build());
        assignedOrder.add(new OrderFactory().setPickupOrder(3).setOrderStatus(OrderStatusEnum.ASSIGNED).build());

        Vehicle v = new VehicleFactory().setAssignedOrders(assignedOrder).build();
        vehicleRepository.save(v).block();
        Map<String, Object> response = webTestClient.get()
                .uri(String.format(nextOrderToPickupEndpoint, v.getId())).accept(MediaType.APPLICATION_JSON).exchange()
                .expectStatus().isOk().expectBody(new ParameterizedTypeReference<HashMap<String, Object>>() {})
                .returnResult().getResponseBody();
        Order expectedOrder = assignedOrder.stream().filter(o -> o.getOrderStatus().equals(OrderStatusEnum.PICKING_UP)).findFirst().get();
        assert UUID.fromString((String) response.get("id")).equals(expectedOrder.getId());
        assert response.get("boxes").equals(expectedOrder.getBoxes());
        assert response.get("orderStatus").equals("PICKING_UP");
        Map<String, Object> responsePickupAddress = ((Map<String, Object>) response.get("pickupAddress"));
        assert responsePickupAddress.get("lat").equals(expectedOrder.getPickupAddress().getLat());
        assert responsePickupAddress.get("lng").equals(expectedOrder.getPickupAddress().getLng());
        assert responsePickupAddress.get("humanReadableAddress").equals(expectedOrder.getPickupAddress()
                .getBuildingOrPlaceName());
        assert UUID.fromString((String) response.get("customerId")).equals(expectedOrder.getSubmittedBy().getId());


        Order returnedOrder = orderRepository.findById(Mono.just(UUID.fromString((String) response.get("id")))).block();
        assert returnedOrder.getPickupOrder() == 1;
        assert returnedOrder.getOrderStatus().equals(OrderStatusEnum.PICKING_UP);
        assert returnedOrder.getAssignedVehicle().equals(v);
        assert returnedOrder.getSubmittedBy().equals(expectedOrder.getSubmittedBy());
        assert returnedOrder.getPickupAddress().equals(expectedOrder.getPickupAddress());

    }
    @Test
    public void getNextOrderToPickupNoMoreOrders(){
        List<Order> assignedOrder = new ArrayList<>();
        assignedOrder.add(new OrderFactory().setPickupOrder(1).setOrderStatus(OrderStatusEnum.PICKED_UP).build());
        assignedOrder.add(new OrderFactory().setPickupOrder(2).setOrderStatus(OrderStatusEnum.PICKED_UP).build());
        assignedOrder.add(new OrderFactory().setPickupOrder(3).setOrderStatus(OrderStatusEnum.PICKED_UP).build());

        Vehicle v = new VehicleFactory().setAssignedOrders(assignedOrder).build();
        vehicleRepository.save(v).block();
        Map<String, Object> response = webTestClient.get()
                .uri(String.format(nextOrderToPickupEndpoint, v.getId())).accept(MediaType.APPLICATION_JSON).exchange()
                .expectStatus().isOk().expectBody(new ParameterizedTypeReference<HashMap<String, Object>>() {})
                .returnResult().getResponseBody();

        assert response == null;

    }
}
