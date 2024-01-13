package ae.recycler.be;

import ae.recycler.be.enums.VehicleStatus;
import ae.recycler.be.factories.CustomerFactory;
import ae.recycler.be.factories.DriverFactory;
import ae.recycler.be.model.Customer;
import ae.recycler.be.model.Driver;
import ae.recycler.be.model.Order;
import ae.recycler.be.model.Vehicle;
import ae.recycler.be.service.repository.CustomerRepository;
import ae.recycler.be.service.repository.DriverRepository;
import ae.recycler.be.service.repository.OrderRepository;
import ae.recycler.be.service.repository.VehicleRepository;
import ae.recycler.be.service.repository.here.RequestObjects;
import ae.recycler.be.service.repository.here.TestDataFactory;
import org.javatuples.Pair;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.devtools.restart.RestartScope;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.Neo4jContainer;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.file.Path;
import java.util.List;
import java.util.UUID;


// Testcontainers require a valid docker installation.
// When running the app locally, ensure you have a valid Docker environment
class LocalDevApplication {


    public static void main(String[] args) throws Exception{
        var context = SpringApplication.from(AerecyclerbeApplication::main)
                .with(LocalDevTestcontainersConfig.class)
                .run(args);
        buildTestDataAndStartBackend(context);
        //buildSimpleOrderWithPickupAddressAndTestCustomQuery(context);
        //getOrderBeingPickedUp(context);
    }





    private static void buildTestDataAndStartBackend(SpringApplication.Running context) throws Exception{
        RequestObjects.Request request = new ObjectMapper().readValue(Path.of("src","test","resources",
                "tour_planning_request.json").toFile(), RequestObjects.Request.class);
        var driverRepository = context.getApplicationContext().getBean("driverRepository", DriverRepository.class);
        var orderRepository = context.getApplicationContext().getBean("orderRepository", OrderRepository.class);
        var vehicleRepository = context.getApplicationContext().getBean("vehicleRepository", VehicleRepository.class);
        var customerRepository = context.getApplicationContext().getBean("customerRepository", CustomerRepository.class);
        Driver driver = new DriverFactory().setId(new UUID(0L, 0L)).build();
        Pair<List<Order>, Vehicle> ordersVehicle = TestDataFactory.fromRequest(request, CustomerFactory.buildRandom());
        ordersVehicle.getValue1().setId(new UUID(0L, 0L));
        ordersVehicle.getValue1().setStatus(VehicleStatus.AT_DEPOSIT);
        orderRepository.saveAll(ordersVehicle.getValue0()).blockLast();
        ordersVehicle.getValue1().setDriver(driver);
        vehicleRepository.save(ordersVehicle.getValue1()).block();
        Customer customer = CustomerFactory.buildRandom();
        customer.setId(UUID.fromString("00000000-0000-0000-0000-000000000001"));
        customerRepository.save(customer).block();
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class LocalDevTestcontainersConfig {
        @Bean
        @RestartScope
        @ServiceConnection
        public Neo4jContainer<?> neo4jContainer() {
            return new Neo4jContainer<>("neo4j:5.11-community").withExposedPorts(7687, 7474).
                    withAdminPassword("verysecret");
        }




    }

}
