package ae.recycler.be.service.repository.here;

import ae.recycler.be.model.Order;
import ae.recycler.be.model.Vehicle;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class RequestObjects {

    @Data
    public static class Request {
        private Plan plan;
        private FleetItem fleet;

        public static Request fromOrdersAndVehicles(List<Order> orders, List<Vehicle> vehicles){
            Request request = new Request();
            request.plan = Plan.fromOrders(orders);
            request.fleet = FleetItem.fromVehicles(vehicles);
            return request;
        }
    }
    @Data
    public static class Type {
        private String id, profile;
        private Costs costs;
        private Shift[] shifts;
        private int[] capacity;
        private int amount;

        public static Type fromVehicle(Vehicle v){
            Type t = new Type();
            t.shifts = Shift.fromVehicle(v).toArray();
            t.costs = new Costs(v.getCostDistance(), v.getCostTime());
            t.capacity = new int[]{v.getCapacity()};
            t.amount = 1;
            t.id = String.format("%s", v.getPlate());
            t.profile = t.id;
            return t;
        }
    }
    @Data
    public static class FleetItem {
        private Type[] types;
        private Profile[] profiles;
        static FleetItem fromVehicles(List<Vehicle> vehicles){
            FleetItem fi = new FleetItem();
            fi.types = new Type[vehicles.size()];
            fi.profiles = new Profile[vehicles.size()];
            for(int i = 0; i<vehicles.size(); i++){
                Vehicle v = vehicles.get(i);
                fi.types[i] = Type.fromVehicle(v);
                fi.profiles[i] = Profile.fromVehicle(v);
            }
            return fi;
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Costs {
        private double distance = 0.0001;
        private int time = 0;
    }
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Location {
        private double lat, lng;
    }
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Profile {
        private String name, type;

        static Profile fromVehicle(Vehicle v){
            return new Profile(v.getPlate(), v.getVehicleType().toHereAPI());
        }
    }
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShiftInfo{
        private String time;
        private Location location;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Shift{
        private ShiftInfo start;
        private ShiftInfo end;
        static Shift fromVehicle(Vehicle vehicle){
            ShiftInfo start = ShiftInfo.builder().time(DateTimeFormatter.ISO_INSTANT
                    .format(vehicle.getDriver().getShiftStarts())).location(
                            new Location(vehicle.getDepotLat(), vehicle.getDepotLng())).build();
            ShiftInfo end = ShiftInfo.builder().time(DateTimeFormatter.ISO_INSTANT
                    .format(vehicle.getDriver().getShiftEnds())).location(
                    new Location(vehicle.getDepotLat(), vehicle.getDepotLng())).build();
            return new Shift(start, end);
        }
        public Shift[] toArray(){
            return new Shift[]{this};
        }
    }

    @Data
    public static class Plan {
        private List<Job> jobs;


        static Plan fromOrders(List<Order> orders){
            Plan p = new Plan();
            p.jobs = orders.stream().map(Job::fromOrder).toList();
            return p;
        }
    }
// 84x40x34
    @Data
    public static class Job {
        private String id;
        private Task tasks;

        public static Job fromOrder(Order order){
            Job job = new Job();
            job.setId(order.getId().toString());
            Pickup p = new Pickup();
            p.setDemand(List.of(order.getBoxes()));
            p.setPlaces(List.of(new Place(
                    new Location(order.getPickupAddress().getLat(), order.getPickupAddress().getLng()))));
            job.setTasks(new Task(List.of(p)));
            return job;
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Task {
        private List<Pickup> pickups;
    }

    @Data
    public static class Pickup {
        private List<Place> places;
        private List<Integer> demand;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Place {
        private Location location;
//        private List<List<String>> times;
        private int duration;
        Place(Location location){
            this(location, /*new ArrayList<>(),*/ 0);
        }
    }



}
