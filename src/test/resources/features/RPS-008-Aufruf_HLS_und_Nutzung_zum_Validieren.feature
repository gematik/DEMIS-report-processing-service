Feature: Aufruf des HLS und Nutzung der Rückgabe zum Validieren

  #Story: https://service.gematik.de/browse/DSC2-2699
  # Zu Verarbeitung des Reports der Bettenbelegung muss der Report-Processing-Service die Meldung aufbereiten und mithhilfe des Hospital-Location-Service wird die Meldende Einrichtung überprüft. Hierzu wir die IK-Nummer aus der empfangenen Meldung an den Hospital Location Service gesendet. Der Report-Processing-Service erhält eine Liste der Standorte die zu dieser IK-Nummer passen. Anschließend validiert der RPS gegen diese Liste, ob die vom HLS erhaltenen Standortdaten die Daten aus der Meldung enthalten. Bei einem negativen Ergebnis wird ein Validierungsfehler an den Melder gesendet. Bei einem positiven Ergebnis wird mit der Verarbeitung der Meldung fortgefahren.
  # Regeln für die Validierung Standortdaten Meldung und Standortdaten HLS:
  # Alle einzelnen Bestandteile der Standortdaten müssen exakt matchen! Da das Meldeportal diese Daten direkt aus dem Verzeichnis lädt, wird hier kein Problem auftreten! (Es sei denn der Nutzer verändert die Daten). Die Nutzung der KIS Schnittstelle wird geringer sein. Mit einer genauen Rückmeldung welches Feld nicht korrekt ist, ist den Nutzern auch geholfen.


  Scenario: HLS gibt Liste zurück. Diese Liste wird zum Validieren genutzt. Der Report validiert.
    Given Es existiert ein Endpunkt durch den der HLS angefragt werden kann
    When Der HLS mit dem IK aus dem Token der Meldung abgefragt wird
    And Der HLS eine Liste sendet
    And Die Validierung gegen diese Liste positiv verläuft
    Then Wird mit der Verarbeitung fortgefahren


  Scenario Outline: Outline: HLS gibt Liste zurück. Diese Liste wird zum Validieren genutzt. Der Report validiert nicht
    Given Es existiert ein Endpunkt durch den der HLS angefragt werden kann
    When Der HLS mit dem IK aus dem Token der Meldung abgefragt wird
    And Der HLS eine Liste mit <field> und <enteredData> sendet
    And Die Validierung gegen diese Liste negativ verläuft
    Then Wird die Verarbeitung abgebrochen
    And Dem Nutzer wird <Error> mit <ErrorCode> angezeigt
    Examples:
      | field      | enteredData | Error                                                   | ErrorCode |
      | locationid | 987654      | Validation Exception (for id 987654 no InEK data found) | 422       |

  Scenario Outline: Outline: HLS gibt Liste zurück. Diese Liste wird zum Validieren genutzt. Der Report validiert, obwohl HLS Daten sich von der Meldung unterscheiden
    Given Es existiert ein Endpunkt durch den der HLS angefragt werden kann
    When Der HLS mit dem IK aus dem Token der Meldung abgefragt wird
    And Der HLS eine Liste mit <field> und <enteredData> sendet
    And Die Validierung gegen diese Liste negativ verläuft
    Then Wird die Verarbeitung abgebrochen
    And Dem Nutzer wird <StatusCode> angezeigt
    Examples:
      | field       | enteredData                  | StatusCode |
      | name        | Testkrankenhaus gematik GmbH | 200        |
      | line        | Friedrichstr. 136            | 200        |
      | line        | Friedrichstr. 136            | 200        |
      | postal code | 10117                        | 200        |
      | city        | Berlin                       | 200        |