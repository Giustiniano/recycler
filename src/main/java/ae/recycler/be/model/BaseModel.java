package ae.recycler.be.model;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

import java.time.Instant;
import java.util.UUID;

@Node
@EqualsAndHashCode
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class BaseModel {
    @GeneratedValue @Id
    private UUID id;
    @CreatedDate @Setter
    private Instant createdDate;
    @LastModifiedDate @Setter
    private Instant lastModified;
}
