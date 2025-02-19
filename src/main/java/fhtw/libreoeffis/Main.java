package fhtw.libreoeffis;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.json.JSONObject;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * Hauptklasse der LibreOeffis-Applikation.
 * Startet die JavaFX-GUI zur Eingabe von Stop-IDs, zeigt die Echtzeitdaten,
 * die Routenplanung, die Übersicht der Transportmittel und die Muster-Stop-IDs an.
 */
public class Main extends Application {

    private WienerLinienAPI api;
    private final String favoritesFilePath = "src/main/java/fhtw/libreoeffis/files/favorites.txt";
    private final String frequentedFilePath = "src/main/java/fhtw/libreoeffis/files/frequented.txt";

    private final String preferredTransportFilePath = "src/main/java/fhtw/libreoeffis/files/preferredTransport.txt";

    private final ListView<String> favoriteStopsList = new ListView<>();
    private final ListView<String> frequentedStopsList = new ListView<>();

    private final Set<String> favoriteStops = new HashSet<>();
    private final Set<String> frequentedStops = new HashSet<>();

    private final Map<String, Integer> stopUsageCount = new HashMap<>();
    /**
     * Startet die JavaFX-Anwendung.
     *
     * @param primaryStage Das Hauptfenster der Anwendung.
     * @author GitHub: ic24b018 & YukkuriV2
     */
    @Override
    public void start(Stage primaryStage) {
        try {
            // Initialisierung der WienerLinienAPI
            api = new WienerLinienAPI();
        } catch (Exception e) {
            showErrorDialog("Fehler beim Laden der API: " + e.getMessage());
            return;
        }

        // Starte den TCP-Server für Echtzeitkommunikation
        TCPCommunication.startTCPServer();

        primaryStage.setTitle("LibreOeffis - Öffentliche Verkehrsmittel");

        // Hauptlayout
        BorderPane root = new BorderPane();
        VBox leftBox = new VBox(20);
        leftBox.setPadding(new Insets(10));

        /**
         * Bereich 1: Echtzeitinformationen
         * Ermöglicht dem Benutzer, Echtzeitdaten für eine bestimmte Stop-ID abzurufen.
         */
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



        /**
         * Bereich 2: Transportmittel anzeigen
         * Zeigt die verfügbaren Transportmittel für eine bestimmte Stop-ID an.
         */
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



        /**
         * Bereich 3: Routenplanung
         * Ermöglicht dem Benutzer, eine Route zwischen zwei Stop-IDs zu berechnen.
         */
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

        /**
         * Bereich 4: TCP-Kommunikation
         * Ermöglicht dem Benutzer, Echtzeitdaten über eine TCP-Verbindung abzurufen.
         */
        VBox tcpBox = new VBox(10);
        tcpBox.setStyle("-fx-padding: 10; -fx-border-color: black; -fx-border-width: 1;");
        Label lblTCP = new Label("TCP-Kommunikation mit Wiener Linien API:");
        Label lblTCPStopId = new Label("Stop-ID:");
        TextField txtTCPStopId = new TextField();
        Button btnTCP = new Button("Daten über TCP abrufen");
        TextArea tcpOutput = new TextArea();
        tcpOutput.setEditable(false);
        tcpBox.getChildren().addAll(lblTCP, lblTCPStopId, txtTCPStopId, btnTCP, tcpOutput);

        btnTCP.setOnAction(event -> {
            String stopId = txtTCPStopId.getText().trim();

            if (stopId.isEmpty()) {
                tcpOutput.setText("Bitte geben Sie eine Stop-ID ein.");
                return;
            }

            // Abruf der Daten über TCP im Hintergrund
            new Thread(() -> {
                try {
                    String response = TCPCommunication.sendTCPMessage(stopId);
                    javafx.application.Platform.runLater(() -> tcpOutput.setText(response));
                } catch (Exception e) {
                    javafx.application.Platform.runLater(() -> tcpOutput.setText("Fehler: " + e.getMessage()));
                }
            }).start();
        });

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

                    StringBuilder outputText = new StringBuilder();

                    if (transportmittelList.isEmpty()) {
                        outputText.append("Keine Transportmittel verfügbar.\n");
                    } else {
                        outputText.append(TransportmittelHelper.formatTransportmittel(transportmittelList)).append("\n");
                    }

                    // Abruf der alternativen Transportmittel
                    List<AlternativeTransportmittel> alternativeList = AlternativeTransportHelper.getAlternativeTransportmittel();
                    if (!alternativeList.isEmpty()) {
                        outputText.append("\nAlternative Transportmittel:\n");
                        for (AlternativeTransportmittel transport : alternativeList) {
                            outputText.append("- ").append(transport.getDetails()).append("\n");
                        }
                    } else {
                        outputText.append("\nKeine alternativen Transportmittel verfügbar.\n");
                    }

                    javafx.application.Platform.runLater(() -> transportOutput.setText(outputText.toString()));

                } catch (Exception e) {
                    javafx.application.Platform.runLater(() -> transportOutput.setText("Fehler: " + e.getMessage()));
                }
            }).start();
        });




        /**
         * Bereich 5: Muster-Stop-IDs als Dropdown anzeigen
         * Zeigt eine Liste von Muster-Stop-IDs an, die der Benutzer durchsuchen und auswählen kann.
         */
        VBox testStopsBox = new VBox(10);
        testStopsBox.setStyle("-fx-padding: 10; -fx-border-color: black; -fx-border-width: 1;");

        Label lblTestStops = new Label("Muster-Stop-IDs für Tests:");
        ComboBox<String> testStopsDropdown = new ComboBox<>();
        TextField searchField = new TextField();
        searchField.setPromptText("Suche nach Haltestelle...");
        Button btnSearch = new Button("Suchen");
        Button btnAddFavorite = new Button("Zu Favoriten hinzufügen");
        TextArea selectedStopOutput = new TextArea();
        selectedStopOutput.setEditable(false);
        List<String> dropdownItems = new ArrayList<>();
        String csvPath = "src/main/java/fhtw/libreoeffis/files/wienerlinien-ogd-haltepunkte.csv";

        try (BufferedReader reader = new BufferedReader(new FileReader(csvPath))) {
            String line;
            boolean isHeader = true;
            while ((line = reader.readLine()) != null) {
                if (isHeader) {
                    isHeader = false;
                    continue;
                }
                String[] parts = line.split(";");
                if (parts.length >= 2) {
                    String stopId = parts[0].trim();
                    String stopText = parts[1].trim();
                    dropdownItems.add(stopText + " (ID: " + stopId + ")");
                }
            }
            dropdownItems.sort(String::compareTo);
            testStopsDropdown.getItems().addAll(dropdownItems);
        } catch (Exception e) {
            testStopsDropdown.getItems().add("Fehler beim Laden der Stop-IDs aus der CSV: " + e.getMessage());
        }

        btnSearch.setOnAction(event -> {
            String searchText = searchField.getText().toLowerCase().trim();
            testStopsDropdown.getItems().clear();
            if (searchText.isEmpty()) {
                testStopsDropdown.getItems().addAll(dropdownItems);
            } else {
                dropdownItems.stream()
                        .filter(item -> fuzzyMatch(item.toLowerCase(), searchText))
                        .forEach(filteredItem -> testStopsDropdown.getItems().add(filteredItem));
            }
        });

        btnAddFavorite.setOnAction(event -> {
            String selectedStop = testStopsDropdown.getValue();
            if (selectedStop != null && !favoriteStops.contains(selectedStop)) {
                favoriteStops.add(selectedStop);
                favoriteStopsList.getItems().add(selectedStop);
                saveFavorites();
            }
        });

        testStopsDropdown.setOnAction(event -> {
            String selectedStop = testStopsDropdown.getValue();
            if (selectedStop != null) {
                selectedStopOutput.setText("Ausgewählte Haltestelle:\n" + selectedStop);
                stopUsageCount.put(selectedStop, stopUsageCount.getOrDefault(selectedStop, 0) + 1);
                updateFrequentedStops();
            }
        });

        testStopsBox.getChildren().addAll(lblTestStops, searchField, btnSearch, testStopsDropdown, btnAddFavorite, selectedStopOutput, favoriteStopsList, frequentedStopsList);




        // Load favorites and frequented stops from file
        loadFavorites();
        loadFrequented();

        // Linke und rechte Sektionen in ein GridPane aufteilen
        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(10));
        gridPane.setHgap(20);
        gridPane.add(leftBox, 0, 0);

        VBox rightBox = new VBox(20);
        rightBox.getChildren().addAll(testStopsBox, tcpBox);
        gridPane.add(rightBox, 1, 0);

        leftBox.getChildren().addAll(realtimeBox, transportBox, routeBox);


        // Bevorzugten Transportmodus speichern und laden
        VBox settingsBox = new VBox(10);
        settingsBox.setStyle("-fx-padding: 10; -fx-border-color: black; -fx-border-width: 1;");
        Label lblPreferredTransport = new Label("Bevorzugter Transportmodus:");
        ComboBox<String> preferredTransportDropdown = new ComboBox<>();
        preferredTransportDropdown.getItems().addAll("Bus", "Bahn", "U-Bahn", "Straßenbahn","Rad");
        Button btnSavePreferredTransport = new Button("Speichern");
        settingsBox.getChildren().addAll(lblPreferredTransport, preferredTransportDropdown, btnSavePreferredTransport);

        rightBox.getChildren().add(settingsBox);

        btnSavePreferredTransport.setOnAction(event -> {
            String selectedTransport = preferredTransportDropdown.getValue();
            if (selectedTransport != null) {
                savePreferredTransport(selectedTransport);
            }
        });


        String preferredTransport = loadPreferredTransport();
        if (preferredTransport != null) {
            preferredTransportDropdown.setValue(preferredTransport);
        }

        root.setCenter(gridPane);

        // Erstelle die Scene und zeige das Fenster
        Scene scene = new Scene(root, 1000, 800);
        primaryStage.setScene(scene);
        primaryStage.show();

    }

    /**
     * Zeigt einen Fehlerdialog mit der gegebenen Nachricht an.
     *
     * @param message Die Fehlermeldung.
     * @author GitHub: YukkuriV2
     */
    private void showErrorDialog(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Fehler");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Speichert die Favoriten in einer Datei.
     * @author GitHub: YukkuriV2
     */
    private void saveFavorites() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(favoritesFilePath))) {
            for (String stop : favoriteStops) {
                writer.write(stop);
                writer.newLine();
            }
            System.out.println("Favorites saved successfully.");
        } catch (IOException e) {
            System.err.println("Error saving favorites: " + e.getMessage());
            e.printStackTrace(); // Log the exception
        }
    }

    /**
     * Lädt die Favoriten aus einer Datei.
     * @author GitHub: YukkuriV2
     */
    private void loadFavorites() {
        try {
            List<String> lines = Files.readAllLines(Paths.get(favoritesFilePath));
            favoriteStops.addAll(lines);
            favoriteStopsList.getItems().addAll(lines);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Speichert die häufig besuchten Haltestellen in einer Datei.
     * @author GitHub: YukkuriV2
     */
    private void saveFrequented() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(frequentedFilePath))) {
            for (String stop : frequentedStops) {
                writer.write(stop);
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Lädt die häufig besuchten Haltestellen aus einer Datei.
     * @author GitHub: YukkuriV2
     */
    private void loadFrequented() {
        try {
            List<String> lines = Files.readAllLines(Paths.get(frequentedFilePath));
            frequentedStops.addAll(lines);
            frequentedStopsList.getItems().addAll(lines);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Aktualisiert die Liste der häufig besuchten Haltestellen.
     * @author GitHub: YukkuriV2
     */
    private void updateFrequentedStops() {
        frequentedStops.clear();
        frequentedStopsList.getItems().clear();
        stopUsageCount.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(5) // Adjust the limit as needed
                .forEach(entry -> {
                    frequentedStops.add(entry.getKey());
                    frequentedStopsList.getItems().add(entry.getKey());
                });
        saveFrequented(); // Save the frequented stops
    }


    /**
     * Speichert den bevorzugten Transportmodus in einer Datei.
     *
     * @param transportMode Der bevorzugte Transportmodus.
     * @author GitHub: YukkuriV2
     */
    private void savePreferredTransport(String transportMode) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(preferredTransportFilePath))) {
            writer.write(transportMode);
            System.out.println("Preferred transport mode saved successfully.");
        } catch (IOException e) {
            System.err.println("Error saving preferred transport mode: " + e.getMessage());
            e.printStackTrace();
        }
    }


    /**
     * Lädt den bevorzugten Transportmodus aus einer Datei.
     *
     * @return Der bevorzugte Transportmodus.
     * @author GitHub: YukkuriV2
     */
    private String loadPreferredTransport() {
        try {
            List<String> lines = Files.readAllLines(Paths.get(preferredTransportFilePath));
            if (!lines.isEmpty()) {
                return lines.get(0);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Fuzzy-Match-Funktion zum Filtern von Suchergebnissen.
     *
     * @param item Der zu durchsuchende Text.
     * @param searchText Der Suchtext.
     * @return true, wenn der Suchtext im Text gefunden wurde, sonst false.
     * @author GitHub: YukkuriV2
     */
    private boolean fuzzyMatch(String item, String searchText) {
        int searchIndex = 0;
        for (int i = 0; item.length() > i && searchText.length() > searchIndex; i++) {
            if (item.charAt(i) == searchText.charAt(searchIndex)) {
                searchIndex++;
            }
        }
        return searchIndex == searchText.length();
    }
    /**
     * Main-Methode, um die Applikation zu starten.
     *
     * @param args Kommandozeilenargumente.
     */
    public static void main(String[] args) {
        launch(args);
    }
}