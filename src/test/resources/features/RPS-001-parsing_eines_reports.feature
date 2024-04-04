Feature: Parsing eines Reports: DSC2-2490

# Story: DSC2-2490 Zu Verarbeitung des Reports der Bettenbelegung muss der Report-Processing-Sercive einen Report parsen können.
# Story: https://service.gematik.de/browse/DSC2-2933


  Scenario Outline: Versenden eines Reports mit unterschiedlichen Body
    Given Ich habe ein Token für Bettenbelegung
    When Ich einen Report verschicke, dessen Body einen bundle Tag <bundle> und parameter Tag <parameter> enthält
    Then erwarte ich eine Antwort mit Http Status <statuscode>
    Then erwarte ich Operation Outcome with severity  <severity> and code <code>

    Examples:
      | bundle | parameter | statuscode | severity    | code          |
      | true   | true      | 200        | information | informational |
      | true   | false     | 200        | information | informational |
      | false  | true      | 200        | information | informational |
      | false  | false     | 422        | error       | exception     |


  Scenario Outline: Versenden eines validen Reports sowohl als JSON als auch als XML
    Given Ich habe ein Token für Bettenbelegung
    When  Ich einen validen Report schicke mit format <content-type>
    Then Erwarte ich eine Antwort mit Http Status <statuscode> und <content-type-response>

    Examples:
      | content-type     | statuscode | content-type-response |
      | application/json | 200        | application/json      |
      | application/xml  | 200        | application/xml       |
      | text/xml         | 415        |                       |


