# LibreOeffis: Eine privacy-orientierte Lösung für den öffentlichen Verkehr in Wien

## Überblick
LibreOeffis ist eine JavaFX-Anwendung, die es ermöglicht, den öffentlichen Verkehr in Wien einfach und datenschutzfreundlich zu nutzen. Mit der Integration der Wiener Linien API bietet die Applikation Echtzeitinformationen zu Haltestellen und Fahrzeiten, unterstützt bei der Routenplanung und erlaubt es, häufig genutzte Haltestellen als Favoriten zu speichern.

## Features
### Must-Have Features
- **Zugriff auf die Wiener Linien API:** Abfrage von Haltestellen und Fahrplandaten. ✅
- **Eingabemaske:** Auswahl von Start- und Zielorten. ✅
- **Routenberechnung:** Ausgabe des schnellsten Wegs inklusive Umstiegsinformationen und Fahrzeiten. ✅

### Should-Have Features
- **Fuzzy Search:** Unterstützung für ungenaue oder unvollständige Haltestellennamen. ✅
- **Fehlerbehandlung:** Hinweise bei ungültigen Eingaben oder fehlenden Verbindungen. ✅
- **Reisedauer:** Anzeige der geschätzten Gesamtreisedauer. ✅

### Nice-to-Have Features
- **Alternative Verbindungen:** Darstellung von mehreren möglichen Routen.❌
- **Favoritenmanagement:** Möglichkeit, häufig genutzte Haltestellen zu speichern und zu verwalten. ✅
- **Zuletzt genutzte Haltestellen:** Anzeige der zuletzt abgefragten Haltestellen. ✅

### Overkill Features
- **Kartenansicht für Routen:** Darstellung der Route auf einer Karte. ❌
- **Einstellung für den bevorzugten Transportmodus:** Auswahl von Bus, Bim oder U-Bahn. 🟡
- **Dynamische Abfahrtszeit (zeigt automatisch alle passenden Verbindungen):** Anzeige von Verbindungen zu einer bestimmten Uhrzeit.❌

## Technische Anforderungen
- **Programmiersprache:** Java (mit JavaFX für das GUI).
- **API-Integration:** Kommunikation mit der Wiener Linien API über HTTP.
- **File I/O:** Speicherung von Favoriten in einer Datei (z. B. JSON oder CSV).
- **Multithreading:** API-Anfragen und komplexe Berechnungen laufen außerhalb des GUI-Threads.
- **Networking:** Direkte TCP/HTTP-Kommunikation ohne zusätzliche Frameworks.


## Nutzung
1. Starten Sie die Applikation über die IDE oder das bereitgestellte JAR-File.
2. Geben Sie Start- und Zielhaltestellen ein.
3. Drücken Sie auf "Route berechnen", um die schnellste Verbindung zu erhalten.


## Autoren/Team
- Mahmut Karakus ic24b018
- Aland Barzan ic24b030
- Nagi Ibrahim ic24b114 
