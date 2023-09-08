package ae.recycler.be.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Driver extends BaseModel{
    private String name;
    private boolean available;
}
