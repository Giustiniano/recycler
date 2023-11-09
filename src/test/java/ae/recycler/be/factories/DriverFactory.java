package ae.recycler.be.factories;

import ae.recycler.be.model.Driver;

import java.util.Optional;
import java.util.UUID;

public class DriverFactory {

    private UUID id;
    private String name;
    private boolean available;
    private Integer shiftStartsHour;
    private Integer shiftStartsMinute;
    private Integer shiftEndsHour;
    private Integer shiftEndsMinute;

    public DriverFactory setId(UUID id) {
        this.id = id;
        return this;
    }

    public DriverFactory setName(String name) {
        this.name = name;
        return this;
    }

    public DriverFactory setAvailable(boolean available) {
        this.available = available;
        return this;
    }

    public DriverFactory setShiftStartsHour(int shiftStartsHour) {
        this.shiftStartsHour = shiftStartsHour;
        return this;
    }

    public DriverFactory setShiftStartsMinute(int shiftStartsMinute) {
        this.shiftStartsMinute = shiftStartsMinute;
        return this;
    }

    public DriverFactory setShiftEndsHour(int shiftEndsHour) {
        this.shiftEndsHour = shiftEndsHour;
        return this;
    }

    public DriverFactory setShiftEndsMinute(int shiftEndsMinute) {
        this.shiftEndsMinute = shiftEndsMinute;
        return this;
    }
    public Driver build(){
        return new Driver(Optional.ofNullable(id).orElse(UUID.randomUUID()),
                Optional.ofNullable(name).orElse(FullNameFactory.getFirstName()), Optional.of(available).orElse(true),
                Optional.ofNullable(shiftStartsHour).orElse(8), Optional.ofNullable(shiftStartsMinute).orElse(0),
                Optional.ofNullable(shiftEndsHour).orElse(18), Optional.ofNullable(shiftEndsMinute).orElse(0));
    }
}
