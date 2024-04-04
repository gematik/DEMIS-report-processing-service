Feature: Falsche Meldung im RPS: DSC2-2680
  # DSC2-2680
  Scenario: Versenden einer validen FHIR Meldung, die mit Bettenbelegung nichts zu tun hat
  Given Ich habe ein Token für Bettenbelegung
  When Ich eine valide Labormeldung schicke
  Then erwarte ich eine Antwort mit Http Status 422
  Then erwarte ich Operation Outcome with severity  'error' and code 'exception' and diagnose 'keine Bettenbelegung meldung'

