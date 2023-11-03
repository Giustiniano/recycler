package ae.recycler.be.service.repository.here;
import lombok.Data;

import java.util.List;

public class ResponseObjects {

    @Data
    static class Statistic {
        private double cost;
        private int distance;
        private int duration;
        private Times times;
    }

    @Data
    static class Times {
        private int driving;
        private int serving;
        private int waiting;
        private int break_;
    }

    @Data
    public class Tour {
        private String vehicleId;
        private String typeId;
        private List<Stop> stops;
        private Statistic statistic;
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
    }

}
