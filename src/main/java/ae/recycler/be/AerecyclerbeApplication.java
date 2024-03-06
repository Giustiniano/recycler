package ae.recycler.be;

import ae.recycler.be.enums.VehicleStatus;
import ae.recycler.be.enums.VehicleType;
import ae.recycler.be.model.Address;
import ae.recycler.be.model.Customer;
import ae.recycler.be.model.Vehicle;
import ae.recycler.be.service.events.serializers.OrderEvent;
import ae.recycler.be.service.repository.CustomerRepository;
import ae.recycler.be.service.repository.VehicleRepository;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.neo4j.cypherdsl.core.renderer.Configuration;
import org.neo4j.cypherdsl.core.renderer.Dialect;
import org.neo4j.driver.Driver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.ReactiveAuditorAware;
import org.springframework.data.neo4j.config.EnableReactiveNeo4jAuditing;
import org.springframework.data.neo4j.core.ReactiveDatabaseSelectionProvider;
import org.springframework.data.neo4j.core.transaction.ReactiveNeo4jTransactionManager;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import reactor.core.publisher.Mono;
import reactor.kafka.receiver.ReceiverOptions;
import reactor.kafka.sender.SenderOptions;

import java.util.*;

@SpringBootApplication
@EnableReactiveNeo4jAuditing
public class AerecyclerbeApplication {

	@Value("${spring.kafka.producer.bootstrap-servers}")
	private String kafkaBootstrapServers;
	@Value("${spring.profiles.active:default}")
	private static String activeProfile;
	@Bean
	Configuration cypherDslConfiguration() {
		return Configuration.newConfig()
				.withDialect(Dialect.NEO4J_5).build();
	}
	@Bean
	public ReactiveNeo4jTransactionManager reactiveTransactionManager(Driver driver,
																	  ReactiveDatabaseSelectionProvider databaseNameProvider) {
		return new ReactiveNeo4jTransactionManager(driver, databaseNameProvider);
	}
	@Bean
	public ReactiveAuditorAware<String> reactiveAuditorAware() {
		return () -> Mono.just("hantsy");
	}

	@Bean
	public ReactiveKafkaProducerTemplate<String, OrderEvent> reactiveKafkaProducerTemplate(KafkaProperties properties) {
		Map<String, Object> props = properties.buildProducerProperties();
		props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServers);
		props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);


		return new ReactiveKafkaProducerTemplate<>(SenderOptions.create(props));
	}

//	@Bean
//	public ReactiveKafkaConsumerTemplate<String, OrderEvent> reactiveKafkaConsumerTemplate(
//			@Value("${ae.recycler.be.kafka.consumer.topic}") String topic) {
//		Map<String, Object> config = new HashMap<>();
//		config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServers);
//		config.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);
//		config.put(ConsumerConfig.GROUP_ID_CONFIG, "recycler");
//		config.put(JsonDeserializer.TRUSTED_PACKAGES,"*");
//		config.put(JsonDeserializer.VALUE_DEFAULT_TYPE,"ae.recycler.be.service.events.serializers.OrderEvent");
//		config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
//		config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
//		config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
//		ReceiverOptions<String, OrderEvent> basicReceiverOptions = ReceiverOptions.create(config);
//		var receiverOptions = basicReceiverOptions.subscription(Collections.singletonList(topic));
//		return new ReactiveKafkaConsumerTemplate<>(receiverOptions);
//	}



	public static void main(String[] args) {
		var context = SpringApplication.run(AerecyclerbeApplication.class, args);
		if(Arrays.asList(context.getEnvironment().getActiveProfiles()).contains("docker"))
			buildDefaultCustomerAndDriver(context);
	}
	private static void buildDefaultCustomerAndDriver(ConfigurableApplicationContext context) {
		Customer customer = Customer.builder().id(UUID.fromString("00000000-0000-0000-0000-000000000001"))
				.build();
		customer.setId(UUID.fromString("00000000-0000-0000-0000-000000000001"));
		var customerRepository = context.getBean("customerRepository", CustomerRepository.class);
		customerRepository.save(customer).block();
		Address depot = new Address(
				null, 24.8994493, 55.0066217, "Amazon warehouse Dubai South",
				"Dubai", "Emirates Road", "Dubai South", "1", "Ground", "Warehouse Dubai South");

		ae.recycler.be.model.Driver driver = ae.recycler.be.model.Driver.builder().id(new UUID(0L, 0L)).build();
		Vehicle vehicle = Vehicle.builder().driver(driver).id(new UUID(0L, 0L))
				.vehicleType(VehicleType.CAR).status(VehicleStatus.AT_DEPOSIT).capacity(10).lat(depot.getLat())
				.lng(depot.getLng()).plate("AB 001 DE").build();
		var vehicleRepository = context.getBean("vehicleRepository", VehicleRepository.class);
		vehicleRepository.save(vehicle).block();

	}


}
