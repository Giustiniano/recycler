package ae.recycler.be.service.repository.here;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

public class ResponseObjects {

    @Data
    public static class Response {
        private Statistic statistic;
        private List<Tour> tours;
        private List<Unassigned> unassigned;
    }
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    static class Statistic {
        private double cost;
        private int distance;
        private int duration;
        private Times times;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    static class Times {
        private int driving;
        private int serving;
        private int waiting;
        private int stopping;
        @JsonProperty("break")
        private int break_;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Tour {
        private String vehicleId;
        private String typeId;
        private List<Stop> stops;
        private Statistic statistic;
        private int shiftIndex;
    }

    @Data
    public static class Stop {
        private Location location;
        private Time time;
        private List<Integer> load;
        private List<Activity> activities;
        private int distance;
    }

    @Data
    static class Location {
        private double lat;
        private double lng;
    }

    @Data
    static class Time {
        private String arrival;
        private String departure;
    }

    @Data
    public static class Activity {
        private String jobId;
        private String type;
        private ResponseObjects.Location location;
        private TimeWindow time;
    }
    @Data
    public static class Reason {
        private String code;
        private String description;
    }
    @Data
    public static class Unassigned{
        private UUID jobId;
        private List<Reason> reasons;
    }
    @Data
    private static class TimeWindow {
        private String start, end;
    }
}
