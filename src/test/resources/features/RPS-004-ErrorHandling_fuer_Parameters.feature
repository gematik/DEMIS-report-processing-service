Feature: ErrorHandling für Parameters und Code Coverage

  #Story: https://service.gematik.de/browse/DSC2-2651
  # Operation Outcome besteht aus severity, code und diagnostics.
  # Da die diagnostic noch nicht spezifiziert ist, kann das auch nicht geprüft werden.
  # Code Coverage muss manuell überprüft werden, sie soll im Sonar bei mindestens 80% liegen

  Scenario: Body-Content fehlt
    Given Es existiert ein Endpunkt fuer die Annahme der Fhirmeldung
    When Eine Fhirmeldung ohne Body-Content gesendet wird
    Then Wird eine Antwort mit folgendem Statuscode und Operation Outcome erwartet
      | 400 | error | exception |

  Scenario: Content-Type-Header fehlt
    Given Es existiert ein Endpunkt fuer die Annahme der Fhirmeldung
    When Eine Fhirmeldung ohne Content-Type-Header gesendet wird
    Then Wird eine Antwort mit folgendem Statuscode und Operation Outcome erwartet
      | 400 | error | exception |

  Scenario: Der Token konnte nicht verarbeitet werden (Authentifizierungsfehler)
    Given Es existiert ein Endpunkt fuer die Annahme der Fhirmeldung
    When Eine Fhirmeldung mit falschem Token gesendet wird
    Then Wird eine Antwort mit folgendem Statuscode und Operation Outcome erwartet
      | 401 | error | exception |

  Scenario: Authorization-Header fehlt
    Given Es existiert ein Endpunkt fuer die Annahme der Fhirmeldung
    When Eine Fhirmeldung ohne Authorization-Header gesendet wird
    Then Wird eine Antwort mit folgendem Statuscode und Operation Outcome erwartet
      | 403 | error | exception |

  Scenario: Role fehlt
    Given Es existiert ein Endpunkt fuer die Annahme der Fhirmeldung
    When Eine Fhirmeldung ohne Role gesendet wird
    Then Wird eine Antwort mit folgendem Statuscode und Operation Outcome erwartet
      | 403 | error | exception |

  Scenario: Falsche HTTP-Methode z.B. GET statt POST
    Given Es existiert ein Endpunkt fuer die Annahme der Fhirmeldung
    When Eine Fhirmeldung mit falscher HTTP-Methode gesendet wird
    Then Wird eine Antwort mit folgendem Statuscode und Operation Outcome erwartet
      | 405 | error | exception |

  Scenario: Falscher Content-Type
    Given Es existiert ein Endpunkt fuer die Annahme der Fhirmeldung
    When Eine Fhirmeldung mit falschem Content-Type gesendet wird
    Then Wird eine Antwort mit folgendem Statuscode und Operation Outcome erwartet
      | 415 | error | exception |

  Scenario: FHIR Validierungsfehler
    Given Es existiert ein Endpunkt fuer die Annahme der Fhirmeldung
    When Eine nicht-valide Fhirmeldung gesendet wird
    Then Wird eine Antwort mit folgendem Statuscode und Operation Outcome erwartet
      | 422 | error | exception |

