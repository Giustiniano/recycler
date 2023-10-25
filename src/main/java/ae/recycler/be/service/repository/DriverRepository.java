package ae.recycler.be.service.repository;

import ae.recycler.be.model.Driver;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import java.util.UUID;

public interface DriverRepository extends ReactiveCrudRepository<Driver, UUID> {

}
