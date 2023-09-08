package ae.recycler.be.service.repository;

import ae.recycler.be.model.Address;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import java.util.UUID;

public interface AddressRepository extends ReactiveCrudRepository<Address, UUID> {
}
