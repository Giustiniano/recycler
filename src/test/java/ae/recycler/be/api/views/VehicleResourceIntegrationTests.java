package ae.recycler.be.api.views;

import ae.recycler.be.factories.DriverFactory;
import ae.recycler.be.factories.GeocodedPlaces;
import ae.recycler.be.factories.OrderFactory;
import ae.recycler.be.factories.VehicleFactory;
import ae.recycler.be.model.Address;
import ae.recycler.be.model.Order;
import ae.recycler.be.model.Vehicle;
import ae.recycler.be.service.events.serializers.OrderEvent;
import ae.recycler.be.service.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.Neo4jContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
    private ReactiveKafkaConsumerTemplate<String, OrderEvent> reactiveKafkaConsumerTemplate;
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

    private static String assignOrdersToVehicle = "/api/v1/vehicle/%s/assignOrders?driverId=%s";
    private static String getItinerary = "/api/v1/vehicle/%s/getItinerary";

    @Test
    public void assignOrdersToVehicle(){
        Vehicle vehicle = new VehicleFactory().build();
        ae.recycler.be.model.Driver driver = new DriverFactory().build();
        vehicleRepository.save(vehicle).block();
        driverRepository.save(driver).block();
        for(Address address: GeocodedPlaces.PICKUP_LOCATIONS){
            orderRepository.save(new OrderFactory().setPickupAddress(address).build()).block();
        }
        var result = webTestClient.put().uri(String.format(assignOrdersToVehicle, vehicle.getId(), driver.getId()))
                .accept(MediaType.APPLICATION_JSON).exchange().expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<ArrayList<HashMap<String, Object>>>() {}).returnResult();
        System.out.println(result);
    }

    @Test
    public void getItineraryForVehicle(){
        ae.recycler.be.model.Driver driver = new DriverFactory().build();
        Vehicle vehicle = new VehicleFactory().setDriver(driver).build();
        List<Order> orders = new ArrayList<>();
        for(Address address: GeocodedPlaces.PICKUP_LOCATIONS){
            orders.add(new OrderFactory().setPickupAddress(address).build());
        }
        vehicle.setAssignedOrders(orders);
        vehicleRepository.save(vehicle).block();
        driverRepository.save(driver).block();
        var result = webTestClient.put().uri(String.format(getItinerary, vehicle.getId(), driver.getId()))
                .accept(MediaType.APPLICATION_JSON).exchange().expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<ArrayList<HashMap<String, Object>>>() {}).returnResult();
        System.out.println(result);
    }

}
