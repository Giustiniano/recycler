package ae.recycler.be.model;

import lombok.*;



@Getter
@Setter
public class Address extends BaseModel {
    private String geolocation;
    private String human_readable_address;
}
