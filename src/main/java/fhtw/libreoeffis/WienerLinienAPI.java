package fhtw.libreoeffis;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Klasse zur Kommunikation mit der Wiener Linien API.
 * Diese Klasse ermöglicht das Abrufen von Echtzeitdaten und die Berechnung von Routen.
 */
public class WienerLinienAPI {

    // Basis-URL der Wiener Linien Monitor-API
    private static final String BASE_URL_MONITOR = "https://www.wienerlinien.at/ogd_realtime/monitor";

    /**
     * Führt eine Anfrage an die API aus und gibt die Antwort als String zurück.
     * @param urlString Der URL-String für die Anfrage.
     * @return Die Antwort der API als JSON-String.
     * @throws Exception Wenn ein Fehler bei der Anfrage auftritt.
     */
    private String sendRequest(String urlString) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        int responseCode = connection.getResponseCode();
        if (responseCode != 200) {
            throw new Exception("Fehler bei der API-Anfrage: HTTP " + responseCode);
        }

        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder response = new StringBuilder();
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        return response.toString();
    }

    /**
     * Gibt die Echtzeitinformationen für eine Haltestelle basierend auf ihrer Stop-ID zurück.
     * @param stopId Die ID der Haltestelle.
     * @return Die Echtzeitdaten als String (formatierte JSON-Daten).
     * @throws Exception Wenn ein Fehler auftritt.
     */
    public String getEchtzeitDaten(String stopId) throws Exception {
        String url = BASE_URL_MONITOR + "?stopId=" + stopId;
        String response = sendRequest(url);

        JSONObject jsonResponse = new JSONObject(response);
        JSONArray monitors = jsonResponse.getJSONObject("data").getJSONArray("monitors");

        if (monitors.isEmpty()) {
            return "Keine Echtzeitdaten für diese Haltestelle verfügbar.";
        }

        StringBuilder result = new StringBuilder();
        for (int i = 0; i < monitors.length(); i++) {
            JSONObject monitor = monitors.getJSONObject(i);

            JSONArray lines = monitor.getJSONArray("lines");
            for (int j = 0; j < lines.length(); j++) {
                JSONObject line = lines.getJSONObject(j);
                String lineName = line.getString("name");
                String towards = line.getString("towards");

                result.append("Linie: ").append(lineName).append(" Richtung ").append(towards).append("\n");

                JSONArray departures = line.getJSONObject("departures").getJSONArray("departure");
                for (int k = 0; k < departures.length(); k++) {
                    String timePlanned = departures.getJSONObject(k).getJSONObject("departureTime").getString("timePlanned");
                    result.append("  Geplante Abfahrt: ").append(timePlanned).append("\n");
                }
            }
        }

        return result.toString();
    }

    /**
     * Berechnet eine Route zwischen zwei Stop-IDs und gibt die Fahrzeiten, Umstiege und Linien zurück.
     * @param startStopId Die Stop-ID der Start-Haltestelle.
     * @param zielStopId Die Stop-ID der Ziel-Haltestelle.
     * @return Die berechnete Route als String.
     * @throws Exception Wenn ein Fehler auftritt.
     */
    public String calculateRoute(String startStopId, String zielStopId) throws Exception {
        JSONObject startData = getEchtzeitDatenJson(startStopId);
        JSONObject zielData = getEchtzeitDatenJson(zielStopId);

        JSONArray startMonitors = startData.getJSONArray("monitors");
        JSONArray zielMonitors = zielData.getJSONArray("monitors");

        if (startMonitors.isEmpty() || zielMonitors.isEmpty()) {
            return "Keine Daten für Start oder Ziel verfügbar.";
        }

        StringBuilder routeDetails = new StringBuilder();
        for (int i = 0; i < startMonitors.length(); i++) {
            JSONObject startMonitor = startMonitors.getJSONObject(i);
            JSONArray startLines = startMonitor.getJSONArray("lines");

            for (int j = 0; j < startLines.length(); j++) {
                JSONObject startLine = startLines.getJSONObject(j);
                String startLineName = startLine.getString("name");
                String startTowards = startLine.getString("towards");

                for (int k = 0; k < zielMonitors.length(); k++) {
                    JSONObject zielMonitor = zielMonitors.getJSONObject(k);
                    JSONArray zielLines = zielMonitor.getJSONArray("lines");

                    for (int l = 0; l < zielLines.length(); l++) {
                        JSONObject zielLine = zielLines.getJSONObject(l);
                        String zielLineName = zielLine.getString("name");
                        String zielTowards = zielLine.getString("towards");

                        if (startLineName.equals(zielLineName)) {
                            routeDetails.append("Linie: ").append(startLineName).append("\n")
                                    .append("  Richtung: ").append(startTowards).append("\n")
                                    .append("  Start: ").append(startMonitor.getJSONObject("locationStop").getJSONObject("properties").getString("title")).append("\n")
                                    .append("  Ziel: ").append(zielMonitor.getJSONObject("locationStop").getJSONObject("properties").getString("title")).append("\n");

                            JSONArray departures = startLine.getJSONObject("departures").getJSONArray("departure");
                            if (!departures.isEmpty()) {
                                String timePlanned = departures.getJSONObject(0).getJSONObject("departureTime").getString("timePlanned");
                                routeDetails.append("  Abfahrt: ").append(timePlanned).append("\n");
                            }
                            return routeDetails.toString();
                        }
                    }
                }
            }
        }

        return "Keine direkte Verbindung zwischen Start und Ziel gefunden.";
    }

    /**
     * Gibt die Echtzeitinformationen für eine Haltestelle basierend auf ihrer Stop-ID zurück.
     * @param stopId Die ID der Haltestelle.
     * @return Die Echtzeitdaten als JSON-Objekt.
     * @throws Exception Wenn ein Fehler auftritt.
     */
    private JSONObject getEchtzeitDatenJson(String stopId) throws Exception {
        String url = BASE_URL_MONITOR + "?stopId=" + stopId;
        String response = sendRequest(url);
        return new JSONObject(response).getJSONObject("data");
    }

    /**
     * Hauptmethode zum Testen der API-Klasse.
     * @param args Kommandozeilenargumente.
     */
    public static void main(String[] args) {
        try {
            WienerLinienAPI api = new WienerLinienAPI();

            // Test: Route berechnen
            String route = api.calculateRoute("147", "400"); // Beispiel-Stop-IDs
            System.out.println(route);
        } catch (Exception e) {
            System.err.println("Fehler: " + e.getMessage());
        }
    }
}

