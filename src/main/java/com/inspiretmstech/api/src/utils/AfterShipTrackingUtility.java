package com.inspiretmstech.api.src.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inspiretmstech.db.tables.records.CustomersRecord;
import com.inspiretmstech.db.tables.records.OrdersRecord;
import com.inspiretmstech.db.tables.records.StopsRecord;
import com.inspiretmstech.db.udt.records.AddressRecord;
import org.apache.http.client.utils.URIBuilder;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

public class AfterShipTrackingUtility {

    private static final String BASE_URL = "https://webhooks.aftership.com/";
    private static final HttpClient httpClient = HttpClient.newHttpClient();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static HttpResponse<?> sendAfterShipTrackingUpdate(AfterShipTrackingUpdate props) throws Exception {
        URI uri = new URIBuilder(BASE_URL + "/couriers/" + props.slug + "/trackings/v1").build();

        Map<String, String> refs = new HashMap<>();
        refs.put("customer_reference_number", props.basic.order.getCustomerReferenceNumber());
        refs.put("stop_number", props.details.stop.getStopNumber().toString());
        refs.put("order_correlation_id", props.basic.order.getId().toString());
        refs.put("stop_correlation_id", props.details.stop.getId().toString());

        Map<String, Object> payload = new HashMap<>();
        payload.put("slug", props.slug);
        payload.put("event", "tracking_update");

        Map<String, String> customer = new HashMap<>();
        customer.put("name", props.basic.customer.getDisplay());
        customer.put("account_number", props.basic.customer.getId().toString());

        Map<String, Object> tracking = new HashMap<>();
        tracking.put("customer", customer);
        tracking.put("tracking_number", props.basic.order.getCustomerReferenceNumber());
        tracking.put("reference_numbers", refs);
        tracking.put("origin_address", Address.from(props.basic.origin));
        tracking.put("destination_address", Address.from(props.basic.destination));

        Map<String, Object> checkpoint = new HashMap<>();
        checkpoint.put("address", Address.from(props.details.stop));
        checkpoint.put("message", props.details.type);
        checkpoint.put("date_time", props.details.at.toString());

        tracking.put("checkpoints", new Map[]{checkpoint});
        payload.put("trackings", new Map[]{tracking});

        String jsonPayload = objectMapper.writeValueAsString(payload);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private static String getAddressDisplay(AddressRecord address, boolean multiline) {
        StringBuilder sb = new StringBuilder();
        sb.append(address.getStreetAddress_1());
        sb.append(", ");

        if (multiline) sb.append("\n");

        if (address.getStreetAddress_2() != null && !address.getStreetAddress_2().isEmpty()) {
            sb.append(address.getStreetAddress_2());
            sb.append(", ");
            if (multiline) sb.append("\n");
        }

        sb.append(address.getCity());
        sb.append(", ");
        if (multiline) sb.append("\n");

        sb.append(address.getState());
        sb.append(" ");
        sb.append(address.getZip());

        return sb.toString();
    }

    public enum AfterShipMessageType {
        PENDING("Pending"),
        APPOINTMENT("Delivery appointment scheduled"),
        IN_TRANSIT("In Transit"),
        ARRIVED("Arrival scan"),
        DEPARTED("Departure Scan");

        private final String message;

        AfterShipMessageType(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }

    public record AfterShipTrackingUpdate(
            @NotNull String slug,
            @NotNull AfterShipTrackingUpdateBasic basic,
            @NotNull AfterShipTrackingUpdateDetails details
    ) {

        public record AfterShipTrackingUpdateBasic(
                @NotNull CustomersRecord customer,
                @NotNull OrdersRecord order,
                @NotNull StopsRecord origin,
                @NotNull StopsRecord destination
        ) {}

        public record AfterShipTrackingUpdateDetails(
                @NotNull String at, // ISO 8601
                @NotNull AfterShipMessageType type,
                @NotNull StopsRecord stop
        ) {}
    }

    private static class Address {
        public String location;
        public String country_iso3;
        public String state;
        public String city;
        public String postal_code;
        public Coordinates coordinates;

        private Address(String location, String country_iso3, String state, String city, String postal_code, Coordinates coordinates) {
            this.location = location;
            this.country_iso3 = country_iso3;
            this.state = state;
            this.city = city;
            this.postal_code = postal_code;
            this.coordinates = coordinates;
        }

        public static Address from(StopsRecord stop) {
            return new Address(
                    getAddressDisplay(stop.getAddress(), false),
                    "USA",
                    stop.getAddress().getState(),
                    stop.getAddress().getCity(),
                    stop.getAddress().getZip(),
                    new Address.Coordinates(stop.getAddress().getLatitude().floatValue(), stop.getAddress().getLongitude().floatValue())
            );
        }

        public static class Coordinates {
            public double latitude;
            public double longitude;

            public Coordinates(double latitude, double longitude) {
                this.latitude = latitude;
                this.longitude = longitude;
            }
        }
    }
}
