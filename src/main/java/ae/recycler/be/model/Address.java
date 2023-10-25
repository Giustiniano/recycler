package ae.recycler.be.model;

import lombok.*;
import org.springframework.data.neo4j.core.schema.*;

import java.util.List;
import java.util.UUID;


@Node
@Getter
@Setter
@Builder
@EqualsAndHashCode
public class Address{
    @Id
    @GeneratedValue
    private UUID id;
    @Property
    private float lat;
    @Property
    private float lng;
    @Property
    private String humanReadableAddress;

}
