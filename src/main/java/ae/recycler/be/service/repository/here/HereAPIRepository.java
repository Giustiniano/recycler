package ae.recycler.be.service.repository;

import ae.recycler.be.model.Order;
import ae.recycler.be.model.Vehicle;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class HereAPIRepository {
    private static String API_URL_WITH_API_KEY;

    @Value("${here.api-key}")
    private String API_KEY;

    public HereAPIRepository(){
        API_URL_WITH_API_KEY = String.format("https://tourplanning.hereapi.com/v3/problems?api_key=%s", API_KEY);
    }

    public void getPickupPath(List<Vehicle> vehicles, List<Order> orders){

    }
    private Map<String, Object> vehicleToFleetItem(Vehicle vehicle){
        Map<String, Object> fleet = new HashMap<>();

        
    }
}
