Feature: Meldung kann an den Validierungsservice geschickt werden

  #Story: https://service.gematik.de/browse/DSC2-2638
  #Zu Verarbeitung des Reports der Bettenbelegung muss der Report-Processing-Service die Meldung
  # aufbereitet(geparsed) haben und diese an den Validierungsservice schicken können. Der Report-Processing-Service
  # wartet auf eine gültige und valide Antwort des Validation-Service oder gibt die entsprechende Fehlermeldung zurück.

  Scenario Outline: Positiv - Bereitet die Meldung und schickt zum Validation-Service. Erwartet wird hier eine valide Antwort
    Given Die Meldung existiert bereits in <format>
    When Die Meldung zum Validation Service gesendet wird
    Then Wird eine Antwort mit <statuscode> erhalten
    Examples:
      | format | statuscode |
      | json   | 200        |
      | xml    | 200        |

  Scenario Outline: Negativ - Bereitet die Meldung und schickt zum Validation-Service. Erwartet wird hier ein Fehlerauftritt
    Given Die falsche Meldung existiert bereits in <format>
    When Die Meldung zum Validation Service gesendet wird
    Then Wird eine Antwort mit <statuscode> und <severity> und <code> erhalten
    Examples:
      | format | statuscode | severity | code      |
      | json   | 422        | error    | exception |
      | xml    | 422        | error    | exception |
