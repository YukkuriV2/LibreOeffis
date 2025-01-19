package fhtw.libreoeffis;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

/**
 * Hauptklasse der LibreOeffis-Applikation.
 * Startet die JavaFX-GUI zur Eingabe von Stop-IDs, Start- und Zielhaltestellen
 * und zeigt die Echtzeitdaten sowie die Routenplanung an.
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

        primaryStage.setTitle("LibreOeffis - Routenplaner");

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

            try {
                String daten = api.getEchtzeitDaten(stopId);
                if (daten.trim().isEmpty()) {
                    realtimeOutput.setText("Keine Daten verfügbar.");
                } else {
                    realtimeOutput.setText("Echtzeitdaten für Stop-ID " + stopId + ":\n" + daten);
                }
            } catch (Exception e) {
                realtimeOutput.setText("Fehler: " + e.getMessage());
            }
        });

        // Bereich 2: Routenplanung
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
            String start = txtStart.getText().trim();
            String ziel = txtZiel.getText().trim();

            if (start.isEmpty() || ziel.isEmpty()) {
                routeOutput.setText("Bitte geben Sie sowohl Start- als auch Ziel-Stop-ID ein.");
                return;
            }

            try {
                // Routenberechnung mit der API
                String route = api.calculateRoute(start, ziel);
                routeOutput.setText(route);
            } catch (Exception e) {
                routeOutput.setText("Fehler: " + e.getMessage());
            }
        });

        // Hauptlayout zusammenstellen
        mainBox.getChildren().addAll(realtimeBox, routeBox);
        root.setCenter(mainBox);

        // Erstelle die Scene und zeige das Fenster
        Scene scene = new Scene(root, 800, 600);
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
