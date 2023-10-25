package ae.recycler.be.api.views.serializers;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class Address {
    private float lat, lng;
    private String humanReadableAddress;
}
