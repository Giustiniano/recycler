package ae.recycler.be.service.repository.here;

import ae.recycler.be.enums.OrderStatusEnum;
import ae.recycler.be.enums.VehicleStatus;
import ae.recycler.be.enums.VehicleType;
import ae.recycler.be.factories.GeocodedPlaces;
import ae.recycler.be.model.Address;
import ae.recycler.be.model.Customer;
import ae.recycler.be.model.Order;
import ae.recycler.be.model.Vehicle;
import org.javatuples.Pair;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class TestDataFactory {

    public static Pair<List<Order>, Vehicle> fromRequest(RequestObjects.Request request, Customer customer){
        List<Vehicle> vehicleStream = Arrays.stream(request.getFleet().getTypes()).map(type -> {
            Vehicle v = new Vehicle();
            v.setPlate(type.getId());
            v.setCapacity(type.getCapacity()[0]);
            v.setLat(type.getShifts()[0].getStart().getLocation().getLat());
            v.setLng(type.getShifts()[0].getStart().getLocation().getLng());
            v.setDepotLat(v.getLat());
            v.setDepotLng(v.getLng());
            v.setStatus(VehicleStatus.AT_DEPOSIT);
            return v;
        }).toList();
        Arrays.stream(request.getFleet().getProfiles()).forEach(profile -> {vehicleStream.stream().filter(
                vehicle -> vehicle.getPlate().equals(profile.getName())).forEach(vehicle -> vehicle.setVehicleType(VehicleType.valueOf(profile.getType().toUpperCase())));});
        List<Order> orders = request.getPlan().getJobs().stream().map(job -> {
            Order order = new Order();
            if(customer != null){
                order.setSubmittedBy(customer);
            }
            order.setId(UUID.fromString(job.getId()));
            job.getTasks().getPickups().stream().forEach(pickup -> {
                pickup.getPlaces().stream().forEach(place -> {
                    Address pickupAddress = GeocodedPlaces.PICKUP_LOCATIONS.stream().filter(
                            address -> address.getLat() == place.getLocation().getLat() && address.getLng() ==
                                    place.getLocation().getLng()).findFirst().get();
                    order.setPickupAddress(pickupAddress);
                    order.setBoxes(pickup.getDemand().get(0));
                    order.setOrderStatus(OrderStatusEnum.SUBMITTED);
                });
            });
            return order;
        }).toList();
        return Pair.with(orders, vehicleStream.stream().findFirst().get());
    }
    public static Pair<List<Order>, Vehicle> fromRequest(RequestObjects.Request request){
        return TestDataFactory.fromRequest(request, null);
    }
}
