alle funktionalit�ten umgesetzt:

Auf Server-Seite gibt es f�r TCP und UDP je einen Listenerthread, die auf eingehende Verbindungen lauschen. Bei TCP wird die Verbindung in einem seperaten TCP-Workerthread verarbeitet und die Kommunikation mit dem Client erfolgt dort. Eingehende UDP Pakete werden ebenfalls in einem eigenen UDP-Workerthread verarbeitet, im Gegensatz zu der TCP Verbindung wird danach der Thread wieder beendet. 
Die Kommunikation beider Protokolle l�uft �ber Objects, die ein command, arguments und response Feld haben, im Fehlerfall stehen au�erdem ein Errorhandling zur Verf�gung. Im TCP-Workerthread werden die commands �ber eine Factory einer Operation zugeordnet, die anschlie�end das Object verarbeitet. Danach wird es vom Workerthread wieder an den Client geschickt. 
Weiters verf�gt der Server �ber eine seperate Userverwaltung (usermodul), in der die bekannten User und ihre Passw�rter, offenen Verbindungen sowie der Login-Status der User verwaltet werden.

Auf Client-Seite l�uft konstant ein UDP-Listenerthread, der auf eingehende UDP-Pakete wartet und diese verarbeitet. Bei einem Login wird weiters ein TCP-Listenerthread (PublicListener) ge�ffnet, der alle eingehenden TCP Daten verarbeitet - im Gegensatz zur Serverseite passiert dies jedoch im selben Thread. Gesendet werden TCP und UDP Daten direkt �ber die Clientklasse ohne in einem Thread ausgelagert zu werden. Bei einem register wird zus�tzlich ein neuer TCP Listenerthread (PrivateListener) erstellt, der auf eingehende Verbindungen lauscht, diese verarbeitet und wieder schlie�t.


----------------------------------------------
offene frage: wozu dienen die vorgegebenen return statements der command methoden? - zerst�ren den sinn des multithreading, wenn auf die antwort f�r die r�ckgabe gewartet werden muss