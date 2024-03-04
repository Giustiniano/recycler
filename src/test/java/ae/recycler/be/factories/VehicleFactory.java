package ae.recycler.be.factories;

import ae.recycler.be.enums.VehicleStatus;
import ae.recycler.be.enums.VehicleType;
import ae.recycler.be.model.Address;
import ae.recycler.be.model.Driver;
import ae.recycler.be.model.Order;
import ae.recycler.be.model.Vehicle;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

public class VehicleFactory {
    private UUID id;
    private VehicleType vehicleType;
    private String plate;
    private Integer capacity;
    private VehicleStatus status;
    private List<Order> assignedOrders;
    private Double lat;
    private Double lng;
    private Driver driver;
    private Address depotAddress;
    private double costDistance = 0.0001;
    private int costTime = 0;

    public VehicleFactory setId(UUID id) {
        this.id = id;
        return this;
    }

    public VehicleFactory setVehicleType(VehicleType vehicleType) {
        this.vehicleType = vehicleType;
        return this;
    }

    public VehicleFactory setPlate(String plate) {
        this.plate = plate;
        return this;
    }

    public VehicleFactory setCapacity(Integer capacity) {
        this.capacity = capacity;
        return this;
    }

    public VehicleFactory setStatus(VehicleStatus status) {
        this.status = status;
        return this;
    }

    public VehicleFactory setAssignedOrders(List<Order> assignedOrders) {
        this.assignedOrders = assignedOrders;
        return this;
    }

    public VehicleFactory setLat(Double lat) {
        this.lat = lat;
        return this;
    }

    public VehicleFactory setLng(Double lng) {
        this.lng = lng;
        return this;
    }

    public VehicleFactory setDriver(Driver driver) {
        this.driver = driver;
        return this;
    }

    public VehicleFactory setDepotAddress(Address depotAddress) {
        this.depotAddress = depotAddress;
        return this;
    }


    public VehicleFactory setCostDistance(double costDistance) {
        this.costDistance = costDistance;
        return this;
    }

    public VehicleFactory setCostTime(int costTime) {
        this.costTime = costTime;
        return this;
    }

    public Vehicle build(){
        return new Vehicle(id, Optional.ofNullable(vehicleType).orElse(VehicleType.CAR),
                Optional.ofNullable(plate).orElse(PlateFactory.getPlate()), Optional.ofNullable(capacity)
                .orElse(10), Optional.ofNullable(status).orElse(VehicleStatus.AT_DEPOSIT), assignedOrders,
                Optional.ofNullable(lat).orElse(GeocodedPlaces.AMAZON_WAREHOUSE_DUBAI_SOUTH.getLat()),
                Optional.ofNullable(lng).orElse(GeocodedPlaces.AMAZON_WAREHOUSE_DUBAI_SOUTH.getLng()), driver,
                Optional.ofNullable(depotAddress).orElse(GeocodedPlaces.AMAZON_WAREHOUSE_DUBAI_SOUTH),
                costDistance, costTime);
    }

    private class PlateFactory{
        private static Random random = new Random();
        private static String getPlate(){
            String firstLetter = new String(new int[]{random.nextInt(0x41, 0x5A)}, 0, 1);
            return firstLetter + random.ints(5, 0x30, 0x39 + 1).collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append).toString();
        }
    }
}
