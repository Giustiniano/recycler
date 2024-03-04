package ae.recycler.be.service;

import ae.recycler.be.enums.OrderStatusEnum;
import ae.recycler.be.enums.VehicleStatus;
import ae.recycler.be.factories.DriverFactory;
import ae.recycler.be.factories.OrderFactory;
import ae.recycler.be.factories.VehicleFactory;
import ae.recycler.be.model.Driver;
import ae.recycler.be.model.Order;
import ae.recycler.be.model.Vehicle;
import ae.recycler.be.service.repository.DriverRepository;
import ae.recycler.be.service.repository.VehicleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.Neo4jContainer;
import org.testcontainers.containers.Neo4jLabsPlugin;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.util.List;


@Testcontainers
@SpringBootTest
public class DriverServiceIntegrationTests {
    @Container
    static Neo4jContainer<?> neo4j = new Neo4jContainer<>("neo4j:5.11-community")
            .withAdminPassword("verysecret").withExposedPorts(7687, 7474).withLabsPlugins(Neo4jLabsPlugin.APOC);

    @DynamicPropertySource
    static void neo4jProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.neo4j.uri", neo4j::getBoltUrl);
        registry.add("spring.neo4j.authentication.username", () -> "neo4j");
        registry.add("spring.neo4j.authentication.password", () -> neo4j.getAdminPassword());
    }

    @Autowired
    private VehicleRepository vehicleRepository;
    @Autowired
    private DriverRepository driverRepository;
    @Autowired
    private DriverService driverService;
    @Test
    public void testContinueShift(){
        Vehicle vehicle = new VehicleFactory().setStatus(VehicleStatus.PICKING_UP).build();
        Driver driver = new DriverFactory().build();
        List<Order> assignedOrders = List.of(1,2,3,4,5).stream()
                .map(pickupOrder -> new OrderFactory().setPickupOrder(pickupOrder)
                        .setOrderStatus(OrderStatusEnum.PICKING_UP).build()).toList();
        vehicle.setAssignedOrders(assignedOrders);
        vehicle.setDriver(driver);
        vehicle = vehicleRepository.save(vehicle).block();
        List<Order> orders = driverService.startShift(vehicle.getDriver().getId(), vehicle.getId()).block();
        orders.stream().forEach(o -> {
            Order expectedOrder = assignedOrders.stream().filter(o1 -> o1.getId().equals(o.getId())).findFirst().get();
            assert o.equals(expectedOrder);
        });

    }
    @Test
    public void testStartShiftNoOrders() {
        Driver driver = new DriverFactory().build();
        Vehicle vehicle = new VehicleFactory().build();
        driverRepository.save(driver).block();
        vehicleRepository.save(vehicle).block();
        assert driverService.startShift(driver.getId(), vehicle.getId()).block() == null;
    }
}
