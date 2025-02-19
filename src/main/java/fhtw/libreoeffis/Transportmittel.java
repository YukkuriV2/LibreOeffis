package fhtw.libreoeffis;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.scene.control.TextArea;

/**
 * Basisklasse für Transportmittel im öffentlichen und alternativen Verkehr.
 */
public class Transportmittel {

    private String name;
    private String linie;
    private String richtung;

    public Transportmittel(String name, String linie, String richtung) {
        this.name = name;
        this.linie = linie;
        this.richtung = richtung;
    }

    public String getDetails() {
        return "Transportmittel: " + name + ", Linie: " + linie + ", Richtung: " + richtung;
    }

    public String getName() {
        return name;
    }

    public String getLinie() {
        return linie;
    }

    public String getRichtung() {
        return richtung;
    }
}

class AlternativeTransportmittel extends Transportmittel {
    private String typ;
    private boolean verfügbar;

    public AlternativeTransportmittel(String name, String typ, boolean verfügbar) {
        super(name, "", "");
        this.typ = typ;
        this.verfügbar = verfügbar;
    }

    @Override
    public String getDetails() {
        return "Alternatives Transportmittel: " + getName() + " (" + typ + "), Verfügbar: " + (verfügbar ? "Ja" : "Nein");
    }

    public String getTyp() {
        return typ;
    }

    public boolean isVerfügbar() {
        return verfügbar;
    }
}

class TransportmittelHelper {
    public static List<Transportmittel> parseTransportmittel(JSONObject apiData) throws Exception {
        List<Transportmittel> transportmittelList = new ArrayList<>();
        if (!apiData.has("monitors")) {
            throw new Exception("Keine Transportmittel-Daten verfügbar.");
        }
        JSONArray monitors = apiData.getJSONArray("monitors");
        for (int i = 0; i < monitors.length(); i++) {
            JSONObject monitor = monitors.getJSONObject(i);
            JSONArray lines = monitor.getJSONArray("lines");
            for (int j = 0; j < lines.length(); j++) {
                JSONObject line = lines.getJSONObject(j);
                String name = line.getString("name");
                String richtung = line.getString("towards");
                transportmittelList.add(new Transportmittel(name, "", richtung));
            }
        }
        return transportmittelList;
    }

    public static String formatTransportmittel(List<Transportmittel> transportmittelList) {
        StringBuilder formatted = new StringBuilder("Transportmittel an der Haltestelle:\n");
        for (Transportmittel t : transportmittelList) {
            formatted.append("- ").append(t.getDetails()).append("\n");
        }
        return formatted.toString();
    }
}

class AlternativeTransportHelper {
    private static WienMobileRadAPI wienMobileRadAPI = new WienMobileRadAPI();

    public static List<AlternativeTransportmittel> getAlternativeTransportmittel() {
        List<AlternativeTransportmittel> alternativeTransportmittel = new ArrayList<>();
        List<BikeStation> bikeStations = wienMobileRadAPI.getBikeStations();
        for (BikeStation station : bikeStations) {
            alternativeTransportmittel.add(new AlternativeTransportmittel(station.getStationId(), "Fahrrad", station.getAvailableBikes() > 0));
        }
        return alternativeTransportmittel;
    }

    public static void displayAlternativeTransport(TextArea outputArea) {
        new Thread(() -> {
            List<AlternativeTransportmittel> transportList = getAlternativeTransportmittel();
            StringBuilder result = new StringBuilder("Alternative Transportmittel:\n");
            for (AlternativeTransportmittel transport : transportList) {
                result.append("- ").append(transport.getDetails()).append("\n");
            }
            result.append("\nDatenquelle: Stadt Wien – data.wien.gv.at\n");
            result.append("Lizenz: CC BY 4.0");
            Platform.runLater(() -> outputArea.setText(result.toString()));
        }).start();
    }

    public static List<String> getAlternativeTransportDetails(String transportType) {
        List<String> details = new ArrayList<>();
        if ("Fahrrad".equalsIgnoreCase(transportType)) {
            List<BikeStation> bikeStations = wienMobileRadAPI.getBikeStations();
            for (BikeStation station : bikeStations) {
                details.add("Station: " + station.getStationId() + " - Verfügbare Fahrräder: " + station.getAvailableBikes());
            }
        } else if ("E-Scooter".equalsIgnoreCase(transportType)) {
            details.add("E-Scooter-Verfügbarkeit ist derzeit nicht verfügbar.");
        } else if ("Carsharing".equalsIgnoreCase(transportType)) {
            details.add("Carsharing-Dienste sind verfügbar in verschiedenen Teilen der Stadt.");
        } else if ("Taxi".equalsIgnoreCase(transportType)) {
            details.add("Taxis sind an wichtigen Knotenpunkten verfügbar.");
        } else if ("Zu Fuß".equalsIgnoreCase(transportType)) {
            details.add("Fußwege sind überall verfügbar.");
        } else {
            details.add("Keine Informationen verfügbar für " + transportType + ".");
        }
        return details;
    }
}
