package fhtw.libreoeffis;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Klasse zur Kommunikation mit der Wiener Linien API.
 * Diese Klasse ermöglicht das Abrufen von Echtzeitdaten, die Berechnung von Routen
 * und die Protokollierung von Abfragen in einer Log-Datei.
 */
public class WienerLinienAPI {

    // Basis-URL der Wiener Linien Monitor-API
    private static final String BASE_URL_MONITOR = "https://www.wienerlinien.at/ogd_realtime/monitor";
    private static final String LOG_FILE = "requests.log";

    // Thread-Pool für Multithreading
    private final ExecutorService executorService = Executors.newCachedThreadPool();

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

        logRequest(urlString);

        return response.toString();
    }

    /**
     * Gibt die Rohdaten der Echtzeitinformationen für eine Haltestelle basierend auf ihrer Stop-ID zurück.
     * @param stopId Die ID der Haltestelle.
     * @return Die Echtzeitdaten als JSON-String.
     * @throws Exception Wenn ein Fehler auftritt.
     */
    public String getEchtzeitDaten(String stopId) throws Exception {
        String url = BASE_URL_MONITOR + "?stopId=" + stopId;
        String response = sendRequest(url);

        if (!response.trim().startsWith("{")) {
            throw new Exception("Ungültige API-Antwort: " + response);
        }

        return response; // Rohdaten zurückgeben
    }

    /**
     * Formatiert die Echtzeitdaten für die Anzeige in der Benutzeroberfläche.
     * @param stopId Die ID der Haltestelle.
     * @return Die formatierten Echtzeitdaten als String.
     * @throws Exception Wenn ein Fehler auftritt.
     */
    public String getFormattedEchtzeitDaten(String stopId) throws Exception {
        String rawData = getEchtzeitDaten(stopId);
        JSONObject jsonResponse = new JSONObject(rawData);
        JSONArray monitors = jsonResponse.getJSONObject("data").getJSONArray("monitors");

        if (monitors.isEmpty()) {
            return "Keine Echtzeitdaten für diese Haltestelle verfügbar.";
        }

        StringBuilder result = new StringBuilder("Echtzeitinformationen:\n");

        for (int i = 0; i < monitors.length(); i++) {
            JSONObject monitor = monitors.getJSONObject(i);
            JSONObject locationStop = monitor.getJSONObject("locationStop").getJSONObject("properties");
            String stopName = locationStop.getString("title");

            result.append("Haltestelle: ").append(stopName).append("\n");

            JSONArray lines = monitor.getJSONArray("lines");
            for (int j = 0; j < lines.length(); j++) {
                JSONObject line = lines.getJSONObject(j);
                String lineName = line.getString("name");
                String direction = line.getString("towards");
                JSONArray departures = line.getJSONObject("departures").getJSONArray("departure");

                result.append("  Linie: ").append(lineName)
                        .append(", Richtung: ").append(direction).append("\n");

                for (int k = 0; k < departures.length(); k++) {
                    JSONObject departure = departures.getJSONObject(k);
                    String timePlanned = departure.getJSONObject("departureTime").getString("timePlanned");
                    int countdown = departure.getJSONObject("departureTime").getInt("countdown");

                    result.append("    Geplante Abfahrt: ").append(timePlanned)
                            .append(" (Countdown: ").append(countdown).append(" Minuten)\n");
                }
            }
        }

        return result.toString();
    }

    /**
     * Berechnet eine Route zwischen zwei Haltestellen basierend auf ihren Stop-IDs.
     * @param startStopId Die Stop-ID der Start-Haltestelle.
     * @param zielStopId Die Stop-ID der Ziel-Haltestelle.
     * @return Die berechnete Route als String.
     * @throws Exception Wenn ein Fehler auftritt.
     */
    public String calculateRoute(String startStopId, String zielStopId) throws Exception {
        JSONObject startData = new JSONObject(getEchtzeitDaten(startStopId)).getJSONObject("data");
        JSONObject zielData = new JSONObject(getEchtzeitDaten(zielStopId)).getJSONObject("data");

        JSONArray startMonitors = startData.getJSONArray("monitors");
        JSONArray zielMonitors = zielData.getJSONArray("monitors");

        if (startMonitors.isEmpty() || zielMonitors.isEmpty()) {
            return "Keine Daten für Start oder Ziel verfügbar.";
        }

        StringBuilder routeDetails = new StringBuilder("Routeninformationen:\n");
        for (int i = 0; i < startMonitors.length(); i++) {
            JSONObject startMonitor = startMonitors.getJSONObject(i);
            JSONArray startLines = startMonitor.getJSONArray("lines");

            for (int j = 0; j < startLines.length(); j++) {
                JSONObject startLine = startLines.getJSONObject(j);
                String startLineName = startLine.getString("name");

                for (int k = 0; k < zielMonitors.length(); k++) {
                    JSONObject zielMonitor = zielMonitors.getJSONObject(k);
                    JSONArray zielLines = zielMonitor.getJSONArray("lines");

                    for (int l = 0; l < zielLines.length(); l++) {
                        JSONObject zielLine = zielLines.getJSONObject(l);
                        String zielLineName = zielLine.getString("name");

                        if (startLineName.equals(zielLineName)) {
                            routeDetails.append("Gemeinsame Linie: ").append(startLineName).append("\n")
                                    .append("  Start: ").append(startMonitor.getJSONObject("locationStop").getJSONObject("properties").getString("title")).append("\n")
                                    .append("  Ziel: ").append(zielMonitor.getJSONObject("locationStop").getJSONObject("properties").getString("title")).append("\n");
                            return routeDetails.toString();
                        }
                    }
                }
            }
        }

        return "Keine direkte Verbindung zwischen Start und Ziel gefunden.";
    }

    /**
     * Protokolliert eine API-Anfrage in der Log-Datei.
     * @param request Die Anfrage-URL.
     */
    private void logRequest(String request) {
        executorService.execute(() -> {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(LOG_FILE, true))) {
                writer.write("Anfrage: " + request);
                writer.newLine();
            } catch (Exception e) {
                System.err.println("Fehler beim Schreiben der Log-Datei: " + e.getMessage());
            }
        });
    }

    /**
     * Hauptmethode zum Testen der API-Klasse.
     * @param args Kommandozeilenargumente.
     */
    public static void main(String[] args) {
        try {
            WienerLinienAPI api = new WienerLinienAPI();

            // Test: Formattierte Echtzeitdaten abrufen
            String daten = api.getFormattedEchtzeitDaten("4613");
            System.out.println(daten);

            // Test: Route berechnen
            String route = api.calculateRoute("4613", "400");
            System.out.println(route);
        } catch (Exception e) {
            System.err.println("Fehler: " + e.getMessage());
        }
    }
}
