package ae.recycler.be.enums;

public enum VehicleType {
    CAR, TRUCK;

    public String toHereAPI(){
        return this.name().toLowerCase();
    }
}
