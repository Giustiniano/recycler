package ae.recycler.be.api.views.serializers;


import java.util.UUID;

public class DriverApiSerializers {
    public static class ItineraryItem {
        private UUID orderId;
        private int boxes;
        private JsonAddress address;

    }
}
