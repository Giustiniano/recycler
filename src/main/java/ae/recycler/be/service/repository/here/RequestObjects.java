package ae.recycler.be.service.repository.here;

import ae.recycler.be.model.Driver;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.time.temporal.TemporalAdjusters;

class RequestObjects {
    @Data
    static class Type {
        private String id, profile;
        private Costs costs;
        private Shift[] shifts;
        private int[] capacity;
        private int amount;
    }
    @Data
    static class FleetItem {
        private Type[] types;
        private Profile[] profiles;
    }
    @Data
    static class Costs {
        private double distance = 0.0001;
        private int time = 0;
    }
    @Data
    static class Location {
        private double lat, lng;
    }
    @Data
    static class Profile {
        private String name, type;
    }
    @Data
    @Builder
    static class ShiftInfo{
        private String iso8501_datetime;
        private Location location;
    }
    @Data
    static class Shift{
        private ShiftInfo start;
        private ShiftInfo end;
        static Shift fromDriver(Driver driver){
            ShiftInfo start = ShiftInfo.builder().iso8501_datetime(Instant.now().)
        }
    }

}
