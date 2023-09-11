package ae.recycler.be.model;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;

import java.time.Instant;
import java.util.UUID;


@Getter
@Setter
@EqualsAndHashCode
public class BaseModel {
    @CreatedDate
    private Instant createdDate;
    @LastModifiedDate
    private Instant lastModified;
}
