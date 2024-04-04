Feature: Hinzufügen des RKI als einziges Ziel : DSC2-2655

  #Story: https://service.gematik.de/browse/DSC2-2504 Als Ziel muss immer das RKI eingetragen werden
  #TODO: es steht noch nicht fest wo und wie das im Report hinterlegt wird

  Scenario: Prüfen das nach erfolgreichen Validierungen das RKI als Ziel eingetragen wird
    Given Ich habe ein Token für Bettenbelegung
    When Ich einen korrekten Report dem RPS uebergeben
    Then Uebergibt der RPS dem Validation-Service den unveraenderten Report
    When Der RPS den Report mit weiteren Informationen angereichert hat
    Then Ich pruefe im veraenderten Report den Empfaenger mit Ziel RKI
