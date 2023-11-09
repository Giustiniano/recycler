package ae.recycler.be.model;

import lombok.*;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoField;
import java.util.UUID;

@Getter
@Setter
@Node
@Builder
@AllArgsConstructor
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


    public OffsetDateTime getShiftStarts(){
        return OffsetDateTime.now().withHour(shiftStartsHour).withMinute(shiftStartsMinute);
    }

    public OffsetDateTime getShiftEnds(){
        return OffsetDateTime.now().withHour(shiftEndsHour).withMinute(shiftEndsMinute);
    }

}