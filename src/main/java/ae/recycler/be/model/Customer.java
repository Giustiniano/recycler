package ae.recycler.be.model;

import jakarta.validation.constraints.NotNull;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.neo4j.core.schema.*;

import java.util.List;
import java.util.UUID;


@Getter
@Setter
@Builder
@Node
@EqualsAndHashCode
public class Customer{
    @Id
    @GeneratedValue
    @Property
    private UUID id;
    @Property
    @NotNull
    private String email;
    @Relationship(type = "HAS_ADDRESS", direction = Relationship.Direction.OUTGOING)
    @EqualsAndHashCode.Exclude
    private List<Address> addresses;

}

