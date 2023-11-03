package ae.recycler.be.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;

import java.time.Instant;
import java.time.temporal.ChronoField;
import java.util.UUID;

@Getter
@Setter
@Node
@Builder
public class Driver extends BaseModel {
    @GeneratedValue
    @Id
    private UUID id;
    @Property
    private String name;
    @Property
    private boolean available;
    @Property
    private int shiftStartsHour;
    @Property
    private int shiftStartsMinute;
    @Property
    private int shiftEndsHour;
    @Property
    private int shiftEndsMinute;


    public Instant getShiftStarts(){
        return Instant.now().with(ChronoField.HOUR_OF_DAY, shiftStartsHour)
                .with(ChronoField.MINUTE_OF_DAY, shiftStartsMinute);
    }

    public Instant getShiftEnds(){
        return Instant.now().with(ChronoField.HOUR_OF_DAY, shiftEndsHour)
                .with(ChronoField.MINUTE_OF_DAY, shiftEndsMinute);
    }

}