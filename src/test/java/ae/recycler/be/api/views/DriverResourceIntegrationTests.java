package ae.recycler.be.api.views;

import ae.recycler.be.api.views.serializers.OrderResponse;
import ae.recycler.be.enums.OrderStatusEnum;
import ae.recycler.be.enums.VehicleStatus;
import ae.recycler.be.factories.CustomerFactory;
import ae.recycler.be.factories.DriverFactory;
import ae.recycler.be.factories.VehicleFactory;
import ae.recycler.be.model.Driver;
import ae.recycler.be.model.Order;
import ae.recycler.be.model.Vehicle;
import ae.recycler.be.service.events.serializers.OrderEvent;
import ae.recycler.be.service.repository.*;
import ae.recycler.be.service.repository.here.HereAPIRepository;
import ae.recycler.be.service.repository.here.RequestObjects;
import ae.recycler.be.service.repository.here.ResponseObjects;
import ae.recycler.be.service.repository.here.TestDataFactory;
import org.javatuples.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.Neo4jContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import org.testcontainers.utility.DockerImageName;
import reactor.core.publisher.Mono;

import java.io.*;
import java.nio.file.Path;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

import static java.lang.String.format;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)


@Testcontainers
public class DriverResourceIntegrationTests {
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
    @MockBean
    private HereAPIRepository hereAPIRepository;



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

    // /api/v1/driver/%s/startShift?vehicleId=%s
    private static final String driverApiEndpoint = "/api/v1/driver";
    private static final String startShiftEndpoint = driverApiEndpoint + "/%s/startShift?vehicleId=%s";
    private static final String endShiftEndpoint = driverApiEndpoint + "/%s/endShift?vehicleId=%s";
    private static String getItinerary = "/api/v1/vehicle/%s/getItinerary";

    @Test
    public void testDriverStartShift() throws IOException {

        RequestObjects.Request request = new ObjectMapper().readValue(Path.of("src","test","resources",
                "tour_planning_request.json").toFile(), RequestObjects.Request.class);

        ResponseObjects.Response response = new ObjectMapper().readValue(Path.of("src","test", "resources",
                "tour_planning_response.json").toFile(), ResponseObjects.Response.class);

        Mockito.when(hereAPIRepository.getPickupPath(Mockito.any(), Mockito.anyList())).thenReturn(Mono.just(response));
        Driver driver = new DriverFactory().build();
        Pair<List<Order>, Vehicle> ordersVehicle = TestDataFactory.fromRequest(request, CustomerFactory.build());
        orderRepository.saveAll(ordersVehicle.getValue0()).blockLast();
        ordersVehicle.getValue1().setDriver(driver);
        Vehicle vehicle = vehicleRepository.save(ordersVehicle.getValue1()).block();
        Scanner keyboard = new Scanner(System.in);
        keyboard.nextInt();
        List<OrderResponse> actualOrdersItinerary = webTestClient.put().uri(format(startShiftEndpoint, driver.getId(),
                vehicle.getId())).exchange().expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<List<OrderResponse>>() {}).returnResult()
                .getResponseBody();

        // check that the itinerary order is preserved
        List<UUID> actualOrdersItineraryUUID = actualOrdersItinerary.stream().map(OrderResponse::getOrderId).toList();
        List<UUID> expectedOrdersItinerary = new ArrayList<>();
        for(ResponseObjects.Stop stop : response.getTours().get(0).getStops()){
            if(stop.getActivities().get(0).getJobId().equals("departure") ||
                    stop.getActivities().get(0).getJobId().equals("arrival")){
                continue;
            }
            expectedOrdersItinerary.add(UUID.fromString(stop.getActivities().get(0).getJobId()));
        }
        assert actualOrdersItineraryUUID.equals(expectedOrdersItinerary);

        ordersVehicle.getValue0().stream().filter(order -> !order.getId().equals
                (response.getUnassigned().get(0).getJobId())).forEach(order -> {
                    var o = orderRepository.findById(order.getId()).block();
                    assert o.getId().equals(actualOrdersItineraryUUID.get(o.getPickupOrder() - 1));
                });



        // check that the unassigned order has the correct status
        Order unassignedOrder = orderRepository.findById(response.getUnassigned().get(0).getJobId()).block();
        assert unassignedOrder.getOrderStatus().equals(OrderStatusEnum.SUBMITTED);

        // check that the vehicle has the correct status
        Vehicle updatedVehicle = vehicleRepository.findById(vehicle.getId()).block();
        updatedVehicle.getAssignedOrders().stream().forEach(order -> {
            assert !order.getId().equals(unassignedOrder.getId());
            assert order.getOrderStatus().equals(OrderStatusEnum.ASSIGNED);
            assert ordersVehicle.getValue0().stream().anyMatch(existingOrder -> existingOrder.getId()
                    .equals(order.getId()));
            assert updatedVehicle.getStatus().equals(VehicleStatus.PICKING_UP);
        });


    }
    @Test
    public void testDriverEndShift() throws IOException {

        RequestObjects.Request request;
        try (Reader is = new BufferedReader(new FileReader("src/test/resources/tour_planning_request.json"))) {
            request = new ObjectMapper().readValue(is, RequestObjects.Request.class);
        }

        Driver driver = new DriverFactory().build();
        Pair<List<Order>, Vehicle> ordersVehicle = TestDataFactory.fromRequest(request);
        orderRepository.saveAll(ordersVehicle.getValue0()).blockLast();
        ordersVehicle.getValue1().setDriver(driver);
        Vehicle vehicle = vehicleRepository.save(ordersVehicle.getValue1()).block();
        webTestClient.put().uri(format(endShiftEndpoint, driver.getId(), vehicle.getId())).exchange().expectStatus()
                .isNoContent();
        Vehicle updatedVehicle = vehicleRepository.findById(vehicle.getId()).block();
        assert updatedVehicle.getStatus().equals(VehicleStatus.AT_DEPOSIT);
        assert updatedVehicle.getAssignedOrders().isEmpty();
        assert orderRepository.findAll().filter(order -> order.getOrderStatus().equals(OrderStatusEnum.ASSIGNED))
                .count().block() == 0;
        assert orderRepository.findAll().filter(order -> order.getPickupOrder() != null).count().block() == 0;
        assert orderRepository.findAll().filter(order -> order.getOrderStatus().equals(OrderStatusEnum.SUBMITTED))
                .count().block() == ordersVehicle.getValue0().size();

    }

    @ParameterizedTest
    @EnumSource(value = VehicleStatus.class, names ={"OUT_OF_ORDER", "PICKING_UP", "DELIVERING"})
    public void testDriverStartShiftIllegalVehicleState(VehicleStatus status) throws IOException {

        Driver driver = new DriverFactory().build();
        Vehicle vehicle = new VehicleFactory().setStatus(status).build();
        driverRepository.save(driver).block();
        vehicleRepository.save(vehicle).block();
        webTestClient.put().uri(format(startShiftEndpoint, driver.getId(),
                        vehicle.getId())).exchange().expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

}
