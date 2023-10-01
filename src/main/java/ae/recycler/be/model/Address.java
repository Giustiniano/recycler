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
    @Setter
    private float lat;
    @Property
    @Setter
    private float lng;
    @Setter
    @Property
    private String humanReadableAddress;

}
