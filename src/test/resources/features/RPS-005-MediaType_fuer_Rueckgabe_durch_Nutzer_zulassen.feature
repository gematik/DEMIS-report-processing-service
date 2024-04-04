Feature: MediaType für die Rueckgabe durch den Nutzer zulassen

  #Story: https://service.gematik.de/browse/DSC2-2679
  #Der Nutzer kann dem Endpunkt mitteilen, welchen Mediatype er zurückerwartet,
  #wenn der Nutzer keinen accept im Header hat, dann erhält er den eingesendeten Typ

  Scenario Outline: Ueberprueft MediaType für die Rueckgabeformat
    Given Es existiert ein Endpunkt fuer die Annahme der Fhirmeldung
    When Die Meldung in <requestformat> mit Accept Anfrage-HTTP-Header <accept> gesendet wird
    Then Wird eine Antwort in <responseformat> erwartet
    Examples:
      | accept           | requestformat | responseformat
      |                  | XML           | XML            |
      | application/json | XML           | JSON           |
      |                  | JSON          | JSON           |
      | application/xml  | JSON          | XML            |