package ae.recycler.be.api.views.serializers;

import ae.recycler.be.enums.VehicleStatus;
import ae.recycler.be.enums.VehicleType;
import ae.recycler.be.model.Vehicle;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class VehicleResponse {
    private UUID id;
    private VehicleType vehicleType;
    private String plate;
    private Integer capacity, costTime;
    private VehicleStatus status;
    private double lat, lng, costDistance;
    private JsonAddress depot;

    public static VehicleResponse fromVehicle(Vehicle vehicle){
        VehicleResponse vr = VehicleResponse.builder().capacity(vehicle.getCapacity()).costDistance(vehicle.getCostDistance())
                .costTime(vehicle.getCostTime())
                .depot(vehicle.getDepotAddress() == null ? null : JsonAddress.fromAddress(vehicle.getDepotAddress()))
                .plate(vehicle.getPlate()).lat(vehicle.getLat()).lng(vehicle.getLng()).id(vehicle.getId())
                .vehicleType(vehicle.getVehicleType()).status(vehicle.getStatus())
                .build();
        return vr;

    }
}
