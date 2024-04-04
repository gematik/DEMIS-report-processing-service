Feature: ReceiptBundle enhält BundleID des empfangenen Reports

# Bug: DSC2-2867 Bundle ID in Report Response fehlerhaft

  Scenario Das ReceiptBundle enthält
    Given eine Bettenbelegungsmeldung wird an den RPS gesendet
    And diese Meldung enthält den Wert '5b9a47fa-10c7-4277-b2ca-f12bf2a0a6f7' als Value im IdentifierTag des Bundles
    And diese Meldung hat als System 'https://demis.rki.de/fhir/NamingSystem/NotificationBundleId\' im IdentifierTag
    When die Meldung empfangen und verarbeitet wurde
    Then enthält das ReceiptBundle den Wert '5b9a47fa-10c7-4277-b2ca-f12bf2a0a6f7' als Value in einem ExtensionTag
    And enthält das ReceiptBundle das System 'https://demis.rki.de/fhir/NamingSystem/NotificationBundleId\' im gleichen ExtensionTag