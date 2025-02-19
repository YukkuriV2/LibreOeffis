
package fhtw.libreoeffis;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class WienMobileRadAPI {

    private static final String BASE_URL = "https://test-api.wstw.at/gateway/WL_WIENMOBIL_API/1";

    public List<BikeStation> getBikeStations() {
        String stationInfoUrl = BASE_URL + "/station_information.json";
        String stationStatusUrl = BASE_URL + "/station_status.json";
        List<BikeStation> stations = new ArrayList<>();

        try {
            JsonNode stationInfoData = fetchJsonData(stationInfoUrl);
            JsonNode stationStatusData = fetchJsonData(stationStatusUrl);

            if (stationInfoData == null || stationStatusData == null) return stations;

            for (JsonNode stationNode : stationInfoData.at("/data/stations")) {
                String stationId = stationNode.get("station_id").asText();
                String name = stationNode.get("name").asText();
                double lat = stationNode.get("lat").asDouble();
                double lon = stationNode.get("lon").asDouble();

                JsonNode statusNode = findStationStatus(stationStatusData, stationId);
                int availableBikes = statusNode != null ? statusNode.get("num_bikes_available").asInt() : 0;

                stations.add(new BikeStation(stationId, name, lat, lon, availableBikes));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stations;
    }

    private JsonNode fetchJsonData(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("User-Agent", "Mozilla/5.0");

        ObjectMapper mapper = new ObjectMapper();
        return mapper.readTree(connection.getInputStream());
    }

    private JsonNode findStationStatus(JsonNode statusData, String stationId) {
        for (JsonNode statusNode : statusData.at("/data/stations")) {
            if (statusNode.get("station_id").asText().equals(stationId)) {
                return statusNode;
            }
        }
        return null;
    }
}

class BikeStation {
    private String stationId;
    private String name;
    private double latitude;
    private double longitude;
    private int availableBikes;

    public BikeStation(String stationId, String name, double latitude, double longitude, int availableBikes) {
        this.stationId = stationId;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.availableBikes = availableBikes;
    }

    public String getStationId() {
        return stationId;
    }

    public String getName() {
        return name;
    }

    public int getAvailableBikes() {
        return availableBikes;
    }
}
