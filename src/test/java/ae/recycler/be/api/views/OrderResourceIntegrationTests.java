package ae.recycler.be.api.views;

import ae.recycler.be.api.views.serializers.NewOrderRequest;
import ae.recycler.be.api.views.serializers.OrderResponse;
import ae.recycler.be.enums.OrderStatusEnum;
import ae.recycler.be.factories.AddressFactory;
import ae.recycler.be.factories.OrderFactory;
import ae.recycler.be.model.Address;
import ae.recycler.be.model.Customer;
import ae.recycler.be.model.Order;
import ae.recycler.be.service.events.serializers.OrderEvent;
import ae.recycler.be.service.repository.AddressRepository;
import ae.recycler.be.service.repository.CustomerRepository;
import ae.recycler.be.service.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
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
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.*;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)

@Testcontainers
public class OrderResourceIntegrationTests {

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
    ReactiveKafkaConsumerTemplate<String, OrderEvent> reactiveKafkaConsumerTemplate;
    @DynamicPropertySource
    static void neo4jProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.neo4j.uri", neo4j::getBoltUrl);
        registry.add("spring.neo4j.authentication.username", () -> "neo4j");
        registry.add("spring.neo4j.authentication.password", () -> neo4j.getAdminPassword());
        registry.add("spring.kafka.producer.bootstrap-servers", () -> kafka.getBootstrapServers());
    }


    @BeforeEach
    public void beforeEach() {
        orderRepository.deleteAll().then(customerRepository.deleteAll()).then(addressRepository.deleteAll()).block();
    }

    static String orderApi = "/api/v1/order";

    // POST
    @Test
    public void testSaveOrder(){
        Address address = addressRepository.save(AddressFactory.build()).block();
        Customer customer = customerRepository.save(Customer.builder().email("some_email@example.com")
                .addresses(List.of(address)).build()).block();

        OrderResponse orderJson = webTestClient.post().uri(orderApi).body(Mono.just(NewOrderRequest.builder()
                        .customerId(customer.getId())
                        .pickupAddress(address.getId()).boxes(10).build()), NewOrderRequest.class)
                .accept(MediaType.APPLICATION_JSON).exchange().expectStatus().isCreated()
                .expectBody(new ParameterizedTypeReference<OrderResponse>() {}).returnResult().getResponseBody();
        assert orderJson != null;
        assert orderJson.getOrderId() != null;
        assert orderJson.getOrderStatus().equals(OrderStatusEnum.SUBMITTED);
        assert orderJson.getBoxes() == 10;
        assert orderJson.getCreatedDate() != null;
        assert orderJson.getLastModified() != null;
        assert orderJson.getPickupAddress().equals(address.getId());
        assert orderJson.getCustomerId().equals(customer.getId());


    }

    // GET
    @Test
    public void testGetOrderById(){
        Order order = OrderFactory.build();
        orderRepository.save(order).block();
        OrderResponse orderJson = webTestClient.get().uri(String.format("%s/%s",orderApi, order.getId()))
                .accept(MediaType.APPLICATION_JSON).exchange().expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<OrderResponse>() {}).returnResult().getResponseBody();
        assert orderJson.getOrderId().equals(order.getId());
    }

    @Test
    public void testGetOrderByIdNotFound(){
        UUID orderId = UUID.randomUUID();
        Map<String, Object> body = webTestClient.get().uri(String.format("%s/%s",orderApi, orderId)).accept(
                MediaType.APPLICATION_JSON).exchange().expectStatus().isNotFound()
                .expectBody(new ParameterizedTypeReference<HashMap<String, Object>>(){}).returnResult()
                .getResponseBody();
        assert body.get("error").equals(String.format("Order with id '%s' was not found",orderId));
        assert body.get("status").equals(HttpStatus.NOT_FOUND.name());
        assert body.get("detail") == null;
        Instant.parse((CharSequence) body.get("created"));
    }

    @Test
    public void testGetOrderByIdIllegalId(){
        Map<String, Object> body = webTestClient.get().uri(String.format("%s/%s",orderApi, "123")).accept(
                MediaType.APPLICATION_JSON).exchange().expectStatus().isBadRequest()
                .expectBody(new ParameterizedTypeReference<HashMap<String, Object>>(){}).returnResult()
                .getResponseBody();
        assert body.get("error").equals(String.format("'123' is not a valid order id"));
        assert body.get("status").equals(HttpStatus.BAD_REQUEST.name());
        assert body.get("detail") == null;

    }

    @Test
    public void testUpdateOrder(){

        Order order = OrderFactory.build();
        orderRepository.save(order).block();
        Customer customer = order.getSubmittedBy();
        Address newAddress = AddressFactory.build();
        addressRepository.save(newAddress).block();
        List<Address> newAddressList = new ArrayList<>();
        newAddressList.addAll(customer.getAddresses());
        newAddressList.add(newAddress);
        customer.setAddresses(newAddressList);
        customerRepository.save(customer).block();
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("newPickupAddress", newAddress.getId().toString());
        updateData.put("newStatus", "ASSIGNED");
        updateData.put("newBoxesCount", 10);
        Map<String, Object> orderData = webTestClient.patch().uri(String.format("%s/%s",orderApi, order.getId()))
                .body(Mono.just(updateData), HashMap.class)
                .accept(MediaType.APPLICATION_JSON).exchange().expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<HashMap<String,Object>>() {}).returnResult()
                .getResponseBody();
        assert orderData != null;
        assert UUID.fromString((String) orderData.get("pickupAddress")).equals(newAddress.getId());
        assert orderData.get("orderStatus").equals("ASSIGNED");
        assert orderData.get("boxes").equals(10);
        String kafkaLogs = kafka.getLogs();
    }

    @ParameterizedTest
    @EnumSource(value = OrderStatusEnum.class, names = {"CANCELED", "DELIVERED", "PICKED_UP", "DELIVERING"})
    public void testUpdateAddressNonUpdatableOrderStatus(OrderStatusEnum status){
        Order order = OrderFactory.build();
        order.getOrderStatuses().last().setOrderStatus(status);
        orderRepository.save(order).block();
        Customer customer = order.getSubmittedBy();
        Address newAddress = AddressFactory.build();
        addressRepository.save(newAddress).block();
        List<Address> newAddressList = new ArrayList<>();
        newAddressList.addAll(customer.getAddresses());
        newAddressList.add(newAddress);
        customer.setAddresses(newAddressList);
        customerRepository.save(customer).block();
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("newPickupAddress", newAddress.getId().toString());
        Map<String, Object> orderData = webTestClient.patch().uri(String.format("%s/%s",orderApi, order.getId()))
                .body(Mono.just(updateData), HashMap.class)
                .accept(MediaType.APPLICATION_JSON).exchange().expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectBody(new ParameterizedTypeReference<HashMap<String,Object>>() {}).returnResult()
                .getResponseBody();
        assert orderData.get("status").equals("UNPROCESSABLE_ENTITY");
        assert orderData.get("error").equals(String.format("Order with status %s cannot be updated", status));
    }


    @ParameterizedTest
    @EnumSource(value = OrderStatusEnum.class, names = {"SUBMITTED", "ASSIGNED", "SCHEDULED", "PICKING_UP"})
    public void testUpdateAddressUpdatableOrderStatus(OrderStatusEnum status){
        Order order = OrderFactory.build();
        order.getOrderStatuses().last().setOrderStatus(status);
        orderRepository.save(order).block();
        Customer customer = order.getSubmittedBy();
        Address newAddress = AddressFactory.build();
        addressRepository.save(newAddress).block();
        List<Address> newAddressList = new ArrayList<>();
        newAddressList.addAll(customer.getAddresses());
        newAddressList.add(newAddress);
        customer.setAddresses(newAddressList);
        customerRepository.save(customer).block();
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("newPickupAddress", newAddress.getId().toString());
        webTestClient.patch().uri(String.format("%s/%s",orderApi, order.getId()))
                .body(Mono.just(updateData), HashMap.class)
                .accept(MediaType.APPLICATION_JSON).exchange().expectStatus().isOk();
        assert order.getOrderStatuses().last().getOrderStatus().equals(status);
    }
}
