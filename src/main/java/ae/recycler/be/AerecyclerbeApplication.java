package ae.recycler.be;

import ae.recycler.be.model.Vehicle;
import org.neo4j.cypherdsl.core.renderer.Configuration;
import org.neo4j.cypherdsl.core.renderer.Dialect;
import org.neo4j.driver.Driver;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.ReactiveAuditorAware;
import org.springframework.data.neo4j.config.EnableReactiveNeo4jAuditing;
import org.springframework.data.neo4j.core.ReactiveDatabaseSelectionProvider;
import org.springframework.data.neo4j.core.transaction.ReactiveNeo4jTransactionManager;
import reactor.core.publisher.Mono;

@SpringBootApplication
@EnableReactiveNeo4jAuditing
public class AerecyclerbeApplication {

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
	public ReactiveKafkaProducerTemplate<String, Employee> reactiveKafkaProducerTemplate(
			KafkaProperties properties) {
		Map<String, Object> props = properties.buildProducerProperties();
		props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
		props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);


		return new ReactiveKafkaProducerTemplate<String, Employee>(SenderOptions.create(props));
	}
	public static void main(String[] args) {
		SpringApplication.run(AerecyclerbeApplication.class, args);
	}

}
