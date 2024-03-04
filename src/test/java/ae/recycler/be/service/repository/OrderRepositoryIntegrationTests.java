package ae.recycler.be.service.repository;

import ae.recycler.be.enums.OrderStatusEnum;
import ae.recycler.be.enums.VehicleStatus;
import ae.recycler.be.factories.DriverFactory;
import ae.recycler.be.factories.OrderFactory;
import ae.recycler.be.factories.VehicleFactory;
import ae.recycler.be.model.Driver;
import ae.recycler.be.model.Order;
import ae.recycler.be.model.Vehicle;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.Neo4jContainer;
import org.testcontainers.containers.Neo4jLabsPlugin;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

@Testcontainers
@SpringBootTest
public class OrderRepositoryIntegrationTests {
    @Container
    static Neo4jContainer<?> neo4j = new Neo4jContainer<>("neo4j:5.11-community")
            .withAdminPassword("verysecret").withExposedPorts(7687,7474).withLabsPlugins(Neo4jLabsPlugin.APOC);
    @Autowired
    private VehicleRepository vehicleRepository;
    @Autowired
    private OrderRepository orderRepository;
    @DynamicPropertySource
    static void neo4jProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.neo4j.uri", neo4j::getBoltUrl);
        registry.add("spring.neo4j.authentication.username", () -> "neo4j");
        registry.add("spring.neo4j.authentication.password", () -> neo4j.getAdminPassword());
    }

    @Test
    public void testGetAssignedOrders(){
        Vehicle vehicle = new VehicleFactory().setStatus(VehicleStatus.PICKING_UP).build();
        Driver driver = new DriverFactory().build();
        List<Order> assignedOrders = List.of(1,2,3,4,5).stream()
                .map(pickupOrder -> new OrderFactory().setPickupOrder(pickupOrder)
                        .setOrderStatus(OrderStatusEnum.PICKING_UP).build()).toList();
        vehicle.setAssignedOrders(assignedOrders);
        vehicle.setDriver(driver);
        vehicleRepository.save(vehicle).block();
        List<Order> orders = orderRepository.findOrdersByAssignedVehicleOrderByPickupOrderAsc(vehicle.getId()).collectList().block();
        orders.stream().forEach(o -> {
            Order expectedOrder = assignedOrders.stream().filter(o1 -> o1.getId().equals(o.getId())).findFirst().get();
            assert o.equals(expectedOrder);
        });
    }
}
