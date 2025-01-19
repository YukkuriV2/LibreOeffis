package fhtw.libreoeffis;

import java.io.*;
import java.net.*;

/**
 * Klasse für TCP-Kommunikation mit Bezug zur Wiener Linien API.
 * Der Server verarbeitet Anfragen nach Echtzeitdaten und antwortet mit den Ergebnissen.
 */
public class TCPCommunication {

    private static final int SERVER_PORT = 12345;
    private static WienerLinienAPI api;

    static {
        try {
            api = new WienerLinienAPI();
        } catch (Exception e) {
            System.err.println("Fehler beim Initialisieren der WienerLinienAPI: " + e.getMessage());
        }
    }

    /**
     * Startet einen TCP-Server, der Stop-IDs akzeptiert und Echtzeitdaten zurückgibt.
     */
    public static void startTCPServer() {
        new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(SERVER_PORT)) {
                System.out.println("TCP-Server gestartet. Wartet auf Anfragen...");

                while (true) {
                    try (Socket clientSocket = serverSocket.accept();
                         BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                         PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

                        String stopId = in.readLine();
                        System.out.println("Anfrage erhalten: Stop-ID " + stopId);

                        String response;
                        try {
                            response = api.getFormattedEchtzeitDaten(stopId);
                        } catch (Exception e) {
                            response = "Fehler beim Abrufen der Echtzeitdaten: " + e.getMessage();
                        }

                        out.println(response);
                    } catch (IOException e) {
                        System.err.println("Fehler bei der Verarbeitung eines Clients: " + e.getMessage());
                    }
                }
            } catch (IOException e) {
                System.err.println("Fehler beim Starten des TCP-Servers: " + e.getMessage());
            }
        }).start();
    }

    /**
     * Sendet eine Stop-ID an den TCP-Server und gibt die Antwort zurück.
     * @param stopId Die Stop-ID, die gesendet werden soll.
     * @return Die Antwort vom Server (Echtzeitdaten oder Fehlermeldung).
     */
    public static String sendTCPMessage(String stopId) {
        try (Socket socket = new Socket("localhost", SERVER_PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            out.println(stopId);
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line).append("\n");
            }
            return response.toString().trim();
        } catch (IOException e) {
            return "Fehler beim Senden der Nachricht: " + e.getMessage();
        }
    }

    /**
     * Hauptmethode zum Testen der TCP-Kommunikation.
     * @param args Kommandozeilenargumente.
     */
    public static void main(String[] args) {
        // Starte den Server
        startTCPServer();

        // Sende eine Testnachricht nach einer kurzen Verzögerung
        new Thread(() -> {
            try {
                Thread.sleep(2000);
                String response = sendTCPMessage("4613"); // Beispiel-Stop-ID
                System.out.println("Antwort vom Server: " + response);
            } catch (InterruptedException e) {
                System.err.println("Fehler: " + e.getMessage());
            }
        }).start();
    }
}
