Feature: PDF_abfragen_und_zurückliefern


  #Story: https://service.gematik.de/browse/DSC2-2724
  #Zur Übermittlung einer Meldungsquittung bei einer Bettenbelegungsmeldung muss diese Quittung generiert werden und in der Antwort zurückgegeben werden.
  #Der Report-Processing-Service stößt nach erfolgreicher Verarbeitung einer Bettenbelegungsmeldung die Meldungsquittungsgenerierung im PDF Generation Service an und bettet das PDF in die Antwort ein.


  Scenario Positiv - Der Report wird zum PDFGen-Service geschickt. Erwartet wird eine PDF im Result Parameter/Receipt Bundle
    Given Es existiert ein Endpunkt für die Abfrage des PDF Services
    And Es existiert durch die vorherigen Verarbeitungsschritte eine Meldung
    When Die Meldung im JSON Format an den PDF Service gesendet wird
    And Eine Antwort als PDF-Binary empfangen wird
    Then Wird dieses Binary der Antwort des RPS hinzugefügt

  Scenario Negativ - Der Report wird zum PDFGen-Service geschickt. Es wird kein PDF Empfangen
    Given Es existiert ein Endpunkt für die Abfrage des PDF Services
    And Es existiert durch die vorherigen Verarbeitungsschritte eine Meldung
    When Die Meldung im JSON Format an den PDF Service gesendet wird
    And Eine Antwort als PDF-Binary nicht empfangen wird
    Then Es befindet sich kein Binary im Receipt Bundle der Antwort