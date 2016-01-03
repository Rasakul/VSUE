alle funktionalitäten umgesetzt:

Auf Server-Seite gibt es für TCP und UDP je einen Listenerthread, die auf eingehende Verbindungen lauschen. Bei TCP wird die Verbindung in einem seperaten TCP-Workerthread verarbeitet und die Kommunikation mit dem Client erfolgt dort. Eingehende UDP Pakete werden ebenfalls in einem eigenen UDP-Workerthread verarbeitet, im Gegensatz zu der TCP Verbindung wird danach der Thread wieder beendet. 
Die Kommunikation beider Protokolle läuft über Objects, die ein command, arguments und response Feld haben, im Fehlerfall stehen außerdem ein Errorhandling zur Verfügung. Im TCP-Workerthread werden die commands über eine Factory einer Operation zugeordnet, die anschließend das Object verarbeitet. Danach wird es vom Workerthread wieder an den Client geschickt. 
Weiters verfügt der Server über eine seperate Userverwaltung (usermodul), in der die bekannten User und ihre Passwörter, offenen Verbindungen sowie der Login-Status der User verwaltet werden.

Auf Client-Seite läuft konstant ein UDP-Listenerthread, der auf eingehende UDP-Pakete wartet und diese verarbeitet. Bei einem Login wird weiters ein TCP-Listenerthread (PublicListener) geöffnet, der alle eingehenden TCP Daten verarbeitet - im Gegensatz zur Serverseite passiert dies jedoch im selben Thread. Gesendet werden TCP und UDP Daten direkt über die Clientklasse ohne in einem Thread ausgelagert zu werden. Bei einem register wird zusätzlich ein neuer TCP Listenerthread (PrivateListener) erstellt, der auf eingehende Verbindungen lauscht, diese verarbeitet und wieder schließt.


----------------------------------------------
offene frage: wozu dienen die vorgegebenen return statements der command methoden? - zerstören den sinn des multithreading, wenn auf die antwort für die rückgabe gewartet werden muss