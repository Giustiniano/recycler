package ae.recycler.be.model;

import lombok.*;
import org.springframework.data.neo4j.core.schema.*;

import java.util.List;
import java.util.UUID;


@Node
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class Address{
    @Id
    @GeneratedValue
    private UUID id;
    @Property
    private double lat;
    @Property
    private double lng;
    @Property
    private String humanReadableAddress;

}
