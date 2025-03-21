package de.gematik.demis.reportprocessingservice.internal;

/*-
 * #%L
 * report-processing-service
 * %%
 * Copyright (C) 2025 gematik GmbH
 * %%
 * Licensed under the EUPL, Version 1.2 or - as soon they will be approved by the
 * European Commission – subsequent versions of the EUPL (the "Licence").
 * You may not use this work except in compliance with the Licence.
 *
 * You find a copy of the Licence in the "Licence" file or at
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either expressed or implied.
 * In case of changes by gematik find details in the "Readme" file.
 *
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 * #L%
 */

import static de.gematik.demis.reportprocessingservice.utils.Constants.ALL_OK;

import java.util.Comparator;
import lombok.experimental.UtilityClass;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.Narrative;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.utilities.xhtml.XhtmlNode;

@UtilityClass
public class OperationOutcomeUtils {

  public static final String PROCESS_NOTIFICATION_RESPONSE_PROFILE =
      "https://demis.rki.de/fhir/StructureDefinition/ProcessNotificationResponse";

  private static final SeverityComparator severityComparator = new SeverityComparator();

  public static OperationOutcome exceptionOperationOutcome(String message) {
    return operationOutcome(
        message, OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.EXCEPTION);
  }

  public static OperationOutcome operationOutcome(
      String message, OperationOutcome.IssueSeverity severity, OperationOutcome.IssueType type) {
    return new OperationOutcome()
        .addIssue(
            new OperationOutcome.OperationOutcomeIssueComponent()
                .setSeverity(severity)
                .setDiagnostics(message)
                .setCode(type));
  }

  public static OperationOutcome producePositiveOutcomeWithNoErrors() {
    OperationOutcome operationOutcome = new OperationOutcome();

    operationOutcome.setMeta(new Meta().addProfile(PROCESS_NOTIFICATION_RESPONSE_PROFILE));

    Narrative text = new Narrative();
    text.setStatus(Narrative.NarrativeStatus.GENERATED);
    XhtmlNode value = new XhtmlNode();
    value.setValue("http://www.w3.org/1999/xhtml");
    text.setDiv(value);
    operationOutcome.setText(text);

    OperationOutcome.OperationOutcomeIssueComponent issue =
        new OperationOutcome.OperationOutcomeIssueComponent();
    issue.setSeverity(OperationOutcome.IssueSeverity.INFORMATION);
    issue.setCode(OperationOutcome.IssueType.INFORMATIONAL);
    issue.getDetails().setText(ALL_OK);

    operationOutcome.addIssue(issue);
    return operationOutcome;
  }

  public static void reduceIssuesSeverityToWarn(final OperationOutcome operationOutcome) {
    operationOutcome.getIssue().stream()
        .filter(
            issue ->
                issue.getSeverity() == OperationOutcome.IssueSeverity.FATAL
                    || issue.getSeverity() == OperationOutcome.IssueSeverity.ERROR)
        .forEach(issue -> issue.setSeverity(OperationOutcome.IssueSeverity.WARNING));
  }

  public static void filterOutcomeIssues(
      final OperationOutcome outcome, final OperationOutcome.IssueSeverity outcomeIssueThreshold) {
    outcome
        .getIssue()
        .removeIf(
            issue -> severityComparator.compare(issue.getSeverity(), outcomeIssueThreshold) < 0);
  }

  public static void orderOutcomeIssues(final OperationOutcome outcome) {
    outcome
        .getIssue()
        .sort(
            Comparator.comparing(
                OperationOutcome.OperationOutcomeIssueComponent::getSeverity,
                severityComparator.reversed()));
  }
}
