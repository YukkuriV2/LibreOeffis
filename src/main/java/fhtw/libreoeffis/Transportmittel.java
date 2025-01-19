package fhtw.libreoeffis;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

/**
 * Basisklasse für Transportmittel im öffentlichen Verkehr.
 * Enthält allgemeine Eigenschaften wie Name, Linie und Richtung.
 */
public class Transportmittel {

    private String name;
    private String linie;
    private String richtung;

    /**
     * Konstruktor für ein Transportmittel.
     * @param name Name des Transportmittels.
     * @param linie Linie des Transportmittels.
     * @param richtung Richtung des Transportmittels.
     */
    public Transportmittel(String name, String linie, String richtung) {
        this.name = name;
        this.linie = linie;
        this.richtung = richtung;
    }

    /**
     * Gibt die Details des Transportmittels zurück.
     * @return Eine formatierte Beschreibung des Transportmittels.
     */
    public String getDetails() {
        return "Transportmittel: " + name + ", Linie: " + linie + ", Richtung: " + richtung;
    }

    // Getter und Setter
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLinie() {
        return linie;
    }

    public void setLinie(String linie) {
        this.linie = linie;
    }

    public String getRichtung() {
        return richtung;
    }

    public void setRichtung(String richtung) {
        this.richtung = richtung;
    }
}

/**
 * Klasse für Bus, abgeleitet von Transportmittel.
 * Enthält zusätzliche Eigenschaften wie Barrierefreiheit.
 */
class Bus extends Transportmittel {

    private boolean barrierefrei;

    /**
     * Konstruktor für einen Bus.
     * @param name Name des Busses.
     * @param linie Linie des Busses.
     * @param richtung Richtung des Busses.
     * @param barrierefrei Gibt an, ob der Bus barrierefrei ist.
     */
    public Bus(String name, String linie, String richtung, boolean barrierefrei) {
        super(name, linie, richtung);
        this.barrierefrei = barrierefrei;
    }

    @Override
    public String getDetails() {
        return super.getDetails() + ", Barrierefrei: " + (barrierefrei ? "Ja" : "Nein");
    }

    // Getter und Setter
    public boolean isBarrierefrei() {
        return barrierefrei;
    }

    public void setBarrierefrei(boolean barrierefrei) {
        this.barrierefrei = barrierefrei;
    }
}

/**
 * Klasse für U-Bahn, abgeleitet von Transportmittel.
 * Enthält zusätzliche Eigenschaften wie maximale Kapazität.
 */
class UBahn extends Transportmittel {

    private int maximaleKapazität;

    /**
     * Konstruktor für eine U-Bahn.
     * @param name Name der U-Bahn.
     * @param linie Linie der U-Bahn.
     * @param richtung Richtung der U-Bahn.
     * @param maximaleKapazität Maximale Kapazität der U-Bahn.
     */
    public UBahn(String name, String linie, String richtung, int maximaleKapazität) {
        super(name, linie, richtung);
        this.maximaleKapazität = maximaleKapazität;
    }

    @Override
    public String getDetails() {
        return super.getDetails() + ", Maximale Kapazität: " + maximaleKapazität;
    }

    // Getter und Setter
    public int getMaximaleKapazität() {
        return maximaleKapazität;
    }

    public void setMaximaleKapazität(int maximaleKapazität) {
        this.maximaleKapazität = maximaleKapazität;
    }
}

/**
 * Utility-Klasse zur Verarbeitung von Transportmitteln basierend auf API-Daten.
 */
class TransportmittelHelper {

    /**
     * Konvertiert API-Daten in eine Liste von Transportmitteln.
     * @param apiData Die JSON-Daten aus der Wiener Linien API.
     * @return Eine Liste von Transportmitteln.
     * @throws Exception Wenn die Daten ungültig sind oder keine Transportmittel gefunden wurden.
     */
    public static List<Transportmittel> parseTransportmittel(JSONObject apiData) throws Exception {
        List<Transportmittel> transportmittelList = new ArrayList<>();

        // Prüfen, ob "monitors" existiert
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
                boolean barrierefrei = line.optBoolean("barrierFree", false);

                // Erkennung des Typs und Erstellen entsprechender Objekte
                if (line.optString("type").contains("Bus")) {
                    transportmittelList.add(new Bus("Bus", name, richtung, barrierefrei));
                } else if (line.optString("type").contains("Metro")) {
                    transportmittelList.add(new UBahn("U-Bahn", name, richtung, 800)); // Beispiel für Kapazität
                } else {
                    transportmittelList.add(new Transportmittel("Transportmittel", name, richtung));
                }
            }
        }

        return transportmittelList;
    }

    /**
     * Formatiert die Liste der Transportmittel für die Anzeige.
     * @param transportmittelList Die Liste der Transportmittel.
     * @return Die formatierte Darstellung.
     */
    public static String formatTransportmittel(List<Transportmittel> transportmittelList) {
        StringBuilder formatted = new StringBuilder("Transportmittel an der Haltestelle:\n");

        for (Transportmittel t : transportmittelList) {
            formatted.append("- ").append(t.getDetails()).append("\n");
        }

        return formatted.toString();
    }
}
