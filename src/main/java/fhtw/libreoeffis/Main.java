package fhtw.libreoeffis;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.json.JSONObject;
import java.util.List;

/**
 * Hauptklasse der LibreOeffis-Applikation.
 * Startet die JavaFX-GUI zur Eingabe von Stop-IDs, zeigt die Echtzeitdaten,
 * die Routenplanung, die Übersicht der Transportmittel und die Test-Stop-IDs an.
 */
public class Main extends Application {

    private WienerLinienAPI api;

    @Override
    public void start(Stage primaryStage) {
        try {
            // Initialisierung der WienerLinienAPI
            api = new WienerLinienAPI();
        } catch (Exception e) {
            showErrorDialog("Fehler beim Laden der API: " + e.getMessage());
            return;
        }

        primaryStage.setTitle("LibreOeffis - Öffentliche Verkehrsmittel");

        // Hauptlayout
        BorderPane root = new BorderPane();
        VBox mainBox = new VBox(20);
        mainBox.setPadding(new Insets(10));

        // Bereich 1: Echtzeitinformationen
        VBox realtimeBox = new VBox(10);
        realtimeBox.setStyle("-fx-padding: 10; -fx-border-color: black; -fx-border-width: 1;");
        Label lblRealtime = new Label("Echtzeitinformationen:");
        Label lblStopId = new Label("Stop-ID:");
        TextField txtStopId = new TextField();
        Button btnRealtime = new Button("Echtzeitdaten abrufen");
        TextArea realtimeOutput = new TextArea();
        realtimeOutput.setEditable(false);
        realtimeBox.getChildren().addAll(lblRealtime, lblStopId, txtStopId, btnRealtime, realtimeOutput);

        btnRealtime.setOnAction(event -> {
            String stopId = txtStopId.getText().trim();

            if (stopId.isEmpty()) {
                realtimeOutput.setText("Bitte geben Sie eine Stop-ID ein.");
                return;
            }

            // Abruf der Echtzeitdaten im Hintergrund
            new Thread(() -> {
                try {
                    String formattedData = api.getFormattedEchtzeitDaten(stopId);
                    javafx.application.Platform.runLater(() -> realtimeOutput.setText(formattedData));
                } catch (Exception e) {
                    javafx.application.Platform.runLater(() -> realtimeOutput.setText("Fehler: " + e.getMessage()));
                }
            }).start();
        });

        // Bereich 2: Transportmittel anzeigen
        VBox transportBox = new VBox(10);
        transportBox.setStyle("-fx-padding: 10; -fx-border-color: black; -fx-border-width: 1;");
        Label lblTransport = new Label("Transportmittel Übersicht:");
        Label lblTransportStopId = new Label("Stop-ID:");
        TextField txtTransportStopId = new TextField();
        Button btnTransport = new Button("Transportmittel anzeigen");
        TextArea transportOutput = new TextArea();
        transportOutput.setEditable(false);
        transportBox.getChildren().addAll(lblTransport, lblTransportStopId, txtTransportStopId, btnTransport, transportOutput);

        btnTransport.setOnAction(event -> {
            String stopId = txtTransportStopId.getText().trim();

            if (stopId.isEmpty()) {
                transportOutput.setText("Bitte geben Sie eine Stop-ID ein.");
                return;
            }

            // Abruf der Transportmittel im Hintergrund
            new Thread(() -> {
                try {
                    String rawData = api.getEchtzeitDaten(stopId);
                    JSONObject apiData = new JSONObject(rawData).getJSONObject("data");
                    List<Transportmittel> transportmittelList = TransportmittelHelper.parseTransportmittel(apiData);

                    if (transportmittelList.isEmpty()) {
                        javafx.application.Platform.runLater(() -> transportOutput.setText("Keine Transportmittel verfügbar."));
                        return;
                    }

                    String formattedTransportmittel = TransportmittelHelper.formatTransportmittel(transportmittelList);
                    javafx.application.Platform.runLater(() -> transportOutput.setText(formattedTransportmittel));
                } catch (Exception e) {
                    javafx.application.Platform.runLater(() -> transportOutput.setText("Fehler: " + e.getMessage()));
                }
            }).start();
        });

        // Bereich 3: Routenplanung
        VBox routeBox = new VBox(10);
        routeBox.setStyle("-fx-padding: 10; -fx-border-color: black; -fx-border-width: 1;");
        Label lblRoute = new Label("Routenplanung:");
        Label lblStart = new Label("Start Stop-ID:");
        TextField txtStart = new TextField();
        Label lblZiel = new Label("Ziel Stop-ID:");
        TextField txtZiel = new TextField();
        Button btnRoute = new Button("Route berechnen");
        TextArea routeOutput = new TextArea();
        routeOutput.setEditable(false);
        routeBox.getChildren().addAll(lblRoute, lblStart, txtStart, lblZiel, txtZiel, btnRoute, routeOutput);

        btnRoute.setOnAction(event -> {
            String startStopId = txtStart.getText().trim();
            String zielStopId = txtZiel.getText().trim();

            if (startStopId.isEmpty() || zielStopId.isEmpty()) {
                routeOutput.setText("Bitte geben Sie sowohl eine Start- als auch eine Ziel-Stop-ID ein.");
                return;
            }

            // Routenberechnung im Hintergrund
            new Thread(() -> {
                try {
                    String routeDetails = api.calculateRoute(startStopId, zielStopId);
                    javafx.application.Platform.runLater(() -> routeOutput.setText(routeDetails));
                } catch (Exception e) {
                    javafx.application.Platform.runLater(() -> routeOutput.setText("Fehler: " + e.getMessage()));
                }
            }).start();
        });

        // Bereich 4: Test-Stop-IDs anzeigen
        VBox testStopsBox = new VBox(10);
        testStopsBox.setStyle("-fx-padding: 10; -fx-border-color: black; -fx-border-width: 1;");
        Label lblTestStops = new Label("Muster-Stop-IDs für Tests:");
        TextArea testStopsOutput = new TextArea();
        testStopsOutput.setEditable(false);

        String testStopsData = "Linie U6:\n" +
                "- Siebenhirten: 4600\n" +
                "- Floridsdorf: 4623\n" +
                "- Gumpendorfer Straße: 4609\n" +
                "- Josefstädter Straße: 4613\n" +
                "- Spittelau: 4618\n\n" +
                "Linie 11A:\n" +
                "- Heiligenstadt: 1140\n" +
                "- Michelbeuern-AKH: 1141\n" +
                "- Gersthof: 1142\n" +
                "- Hernals: 1143\n";

        testStopsOutput.setText(testStopsData);
        testStopsBox.getChildren().addAll(lblTestStops, testStopsOutput);

        // Hauptlayout zusammenstellen
        mainBox.getChildren().addAll(realtimeBox, transportBox, routeBox, testStopsBox);
        root.setCenter(mainBox);

        // Erstelle die Scene und zeige das Fenster
        Scene scene = new Scene(root, 800, 800);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Zeigt einen Fehlerdialog mit der gegebenen Nachricht an.
     * @param message Die Fehlermeldung.
     */
    private void showErrorDialog(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Fehler");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Main-Methode, um die Applikation zu starten.
     * @param args Kommandozeilenargumente.
     */
    public static void main(String[] args) {
        launch(args);
    }
}
