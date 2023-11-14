package ae.recycler.be.service.repository.here;

import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;


public class HereApiSerializersTests {

    @Test
    public void testDeserializeTourApiResponse() throws IOException {
        ResponseObjects.Response response;
        try(Reader is = new BufferedReader(new FileReader("src/test/resources/tour_planning_response.json"))) {
            response = new ObjectMapper().readValue(is, ResponseObjects.Response.class);
        }
        var expectedStatistics = new ResponseObjects.Statistic(15.9806, 159806, 10825,
                new ResponseObjects.Times(10825, 0,0,0,0));
        assert response.getStatistic().equals(expectedStatistics);
        
    }
}
