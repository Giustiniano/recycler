package ae.recycler.be.service.repository.here;

import ae.recycler.be.model.Order;
import ae.recycler.be.model.Vehicle;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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

    public Mono<List<ResponseObjects.Stop>> getPickupPath(List<Vehicle> vehicles, List<Order> orders){
        RequestObjects.Plan plan = RequestObjects.Plan.fromOrdersAndVehicles(vehicles, orders);
        return WebClient.create(API_URL_WITH_API_KEY).post().body(Mono.just(plan), RequestObjects.Plan.class)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).retrieve()
                .bodyToMono(ResponseObjects.Tour.class).flatMap(tour -> Mono.just(tour.getStops()));
    }

}
