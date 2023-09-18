package ae.recycler.be.api.views;

import ae.recycler.be.api.views.serializers.OrderRequest;
import ae.recycler.be.api.views.serializers.OrderResponse;
import ae.recycler.be.enums.OrderStatusEnum;
import ae.recycler.be.factories.OrderFactory;
import ae.recycler.be.model.Address;
import ae.recycler.be.model.Customer;
import ae.recycler.be.model.Order;
import ae.recycler.be.service.repository.AddressRepository;
import ae.recycler.be.service.repository.CustomerRepository;
import ae.recycler.be.service.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.UUID;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)

@Testcontainers
public class OrderResourceIntegrationTests {

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
    @DynamicPropertySource
    static void neo4jProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.neo4j.uri", neo4j::getBoltUrl);
        registry.add("spring.neo4j.authentication.username", () -> "neo4j");
        registry.add("spring.neo4j.authentication.password", () -> neo4j.getAdminPassword());
    }


    @BeforeEach
    public void beforeEach() {
        Mono.zip(addressRepository.deleteAll(),
        customerRepository.deleteAll(),
        orderRepository.deleteAll()).block();
    }

    static String orderApi = "/api/v1/order";

    // POST
    @Test
    public void testSaveOrder(){
        Address address = addressRepository.save(Address.builder().humanReadableAddress("Gotham City").geolocation("123").build()).block();
        Customer customer = customerRepository.save(Customer.builder().email("some_email@example.com")
                .addresses(List.of(address)).build()).block();
//

        OrderResponse orderJson = webTestClient.post().uri(orderApi).body(Mono.just(OrderRequest.builder()
                        .customerId(customer.getId())
                        .pickupAddress(address.getId()).boxes(10).build()), OrderRequest.class)
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
        webTestClient.get().uri(String.format("%s/%s",orderApi, UUID.randomUUID())).accept(
                MediaType.APPLICATION_JSON).exchange().expectStatus().isNotFound();
    }

    @Test
    public void TestGetOrderByIdIllegalId(){
        webTestClient.get().uri(String.format("%s/%s",orderApi, "123")).accept(
                MediaType.APPLICATION_JSON).exchange().expectStatus().isBadRequest();
    }

}
