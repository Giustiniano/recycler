package ae.recycler.be.service.repository.here;

import ae.recycler.be.model.Order;
import ae.recycler.be.model.Vehicle;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.nio.file.Path;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
public class HereAPIRepository {

    @Value("${here.api-key}")
    private String API_KEY;
    @Value("${here.use-canned-response}")
    private boolean USE_CANNED_RESPONSE;


    @SneakyThrows
    public Mono<ResponseObjects.Response> getPickupPath(List<Vehicle> vehicles, List<Order> orders){
        if(USE_CANNED_RESPONSE){
            return Mono.just(new ObjectMapper().readValue(Path.of("src","test", "resources",
                    "tour_planning_response.json").toFile(), ResponseObjects.Response.class));
        }
        String url = String.format("https://tourplanning.hereapi.com/v3/problems?apiKey=%s", API_KEY);
        RequestObjects.Request request = RequestObjects.Request.fromOrdersAndVehicles(orders, vehicles);

        return WebClient.create(url).post().body(Mono.just(request), RequestObjects.Plan.class)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).retrieve()
                .bodyToMono(ResponseObjects.Response.class).flatMap(Mono::just)
                .retryWhen(Retry.fixedDelay(3, Duration.of(3, ChronoUnit.SECONDS)));
    }

}
