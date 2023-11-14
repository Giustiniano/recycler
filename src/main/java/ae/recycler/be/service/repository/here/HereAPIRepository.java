package ae.recycler.be.service.repository.here;

import ae.recycler.be.model.Order;
import ae.recycler.be.model.Vehicle;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class HereAPIRepository {

    @Value("${here.api-key}")
    private String API_KEY;


    public Mono<ResponseObjects.Response> getPickupPath(List<Vehicle> vehicles, List<Order> orders){
        String url = String.format("https://tourplanning.hereapi.com/v3/problems?apiKey=%s", API_KEY);
        RequestObjects.Request request = RequestObjects.Request.fromOrdersAndVehicles(orders, vehicles);

        return WebClient.create(url).post().body(Mono.just(request), RequestObjects.Plan.class)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).retrieve()
                .bodyToMono(ResponseObjects.Response.class).flatMap(Mono::just)
                .retryWhen(Retry.fixedDelay(3, Duration.of(3, ChronoUnit.SECONDS)));
    }

}
