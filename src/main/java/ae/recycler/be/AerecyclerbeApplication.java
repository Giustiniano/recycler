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
	public static void main(String[] args) {
		System.out.println(new Vehicle());
		SpringApplication.run(AerecyclerbeApplication.class, args);
	}

}
