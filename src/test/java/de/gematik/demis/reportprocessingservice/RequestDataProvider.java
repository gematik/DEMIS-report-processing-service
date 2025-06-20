package de.gematik.demis.reportprocessingservice;

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
 *
 * *******
 *
 * For additional notes and disclaimer from gematik and in case of changes by gematik find details in the "Readme" file.
 * #L%
 */

import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.springframework.http.MediaType.*;

import java.util.stream.Stream;
import org.junit.jupiter.params.provider.Arguments;

public class RequestDataProvider {
  public static Stream<Arguments> okCombinations() {
    return Stream.of(
        arguments(
            "<Bundle xmlns=\"http://hl7.org/fhir\"><id value=\"7a5af172-0d72-4395-8c60-c33ece5eaad2\"></id><meta><lastUpdated value=\"2022-09-05T19:06:15.141+02:00\"></lastUpdated><profile value=\"https://demis.rki.de/fhir/StructureDefinition/ReportBundle\"></profile></meta><identifier><system value=\"https://demis.rki.de/fhir/NamingSystem/NotificationBundleId\"></system><value value=\"58ab4c30-71a2-431f-a776-0febb51969f4\"></value></identifier><type value=\"document\"></type><timestamp value=\"2022-09-05T19:06:15.141+02:00\"></timestamp><entry><fullUrl value=\"https://demis.rki.de/fhir/Composition/fee47acd-4608-43f6-8b39-2fc48ce29057\"></fullUrl><resource><Composition xmlns=\"http://hl7.org/fhir\"><id value=\"fee47acd-4608-43f6-8b39-2fc48ce29057\"></id><meta><profile value=\"https://demis.rki.de/fhir/StructureDefinition/ReportBedOccupancy\"></profile></meta><identifier><system value=\"https://demis.rki.de/fhir/NamingSystem/NotificationId\"></system><value value=\"24919c8c-68c9-429c-9d92-2c5bce70715b\"></value></identifier><status value=\"final\"></status><type><coding><system value=\"http://loinc.org\"></system><code value=\"80563-0\"></code><display value=\"Report\"></display></coding></type><category><coding><system value=\"https://demis.rki.de/fhir/CodeSystem/reportCategory\"></system><code value=\"bedOccupancyReport\"></code><display value=\"Bettenbelegungsstatistik\"></display></coding></category><subject><identifier><system value=\"https://demis.rki.de/fhir/NamingSystem/InekStandortId\"></system><value value=\"987654\"></value></identifier></subject><date value=\"2022-09-05T19:06:15+02:00\"></date><author><reference value=\"PractitionerRole/ae4ff942-9db0-4d34-b6b5-1e2363c81621\"></reference></author><title value=\"Bericht (Krankenhausbettenbelegungsstatistik)\"></title><section><code><coding><system value=\"https://demis.rki.de/fhir/CodeSystem/reportSection\"></system><code value=\"statisticInformationBedOccupancySection\"></code><display value=\"Abschnitt 'Statistische Informationen zur Krankenhausbettenbelegung'\"></display></coding></code><entry><reference value=\"QuestionnaireResponse/1627a582-c3b1-44ac-a5fd-8970fc6a6289\"></reference></entry></section></Composition></resource></entry><entry><fullUrl value=\"https://demis.rki.de/fhir/Organization/7c4a1bc4-b31e-4833-ba50-ba43fd7c1926\"></fullUrl><resource><Organization xmlns=\"http://hl7.org/fhir\"><id value=\"7c4a1bc4-b31e-4833-ba50-ba43fd7c1926\"></id><meta><profile value=\"https://demis.rki.de/fhir/StructureDefinition/NotifierFacility\"></profile></meta><type><coding><system value=\"https://demis.rki.de/fhir/CodeSystem/organizationType\"></system><code value=\"hospital\"></code><display value=\"Krankenhaus\"></display></coding></type><name value=\"Testkrankenhaus - gematik GmbH\"></name><telecom><system value=\"phone\"></system><value value=\"0123456789\"></value><use value=\"work\"></use></telecom><address><line value=\"Friedrichstr. 136\"></line><city value=\"Berlin\"></city><postalCode value=\"10117\"></postalCode></address></Organization></resource></entry><entry><fullUrl value=\"https://demis.rki.de/fhir/PractitionerRole/ae4ff942-9db0-4d34-b6b5-1e2363c81621\"></fullUrl><resource><PractitionerRole xmlns=\"http://hl7.org/fhir\"><id value=\"ae4ff942-9db0-4d34-b6b5-1e2363c81621\"></id><meta><profile value=\"https://demis.rki.de/fhir/StructureDefinition/NotifierRole\"></profile></meta><organization><reference value=\"Organization/7c4a1bc4-b31e-4833-ba50-ba43fd7c1926\"></reference></organization></PractitionerRole></resource></entry><entry><fullUrl value=\"https://demis.rki.de/fhir/QuestionnaireResponse/1627a582-c3b1-44ac-a5fd-8970fc6a6289\"></fullUrl><resource><QuestionnaireResponse xmlns=\"http://hl7.org/fhir\"><id value=\"1627a582-c3b1-44ac-a5fd-8970fc6a6289\"></id><meta><profile value=\"https://demis.rki.de/fhir/StructureDefinition/StatisticInformationBedOccupancy\"></profile></meta><questionnaire value=\"https://demis.rki.de/fhir/Questionnaire/StatisticQuestionsBedOccupancy\"></questionnaire><status value=\"completed\"></status><item><linkId value=\"numberOperableBedsGeneralWardAdults\"></linkId><answer><valueInteger value=\"250\"></valueInteger></answer></item><item><linkId value=\"numberOccupiedBedsGeneralWardAdults\"></linkId><answer><valueInteger value=\"221\"></valueInteger></answer></item><item><linkId value=\"numberOperableBedsGeneralWardChildren\"></linkId><answer><valueInteger value=\"50\"></valueInteger></answer></item><item><linkId value=\"numberOccupiedBedsGeneralWardChildren\"></linkId><answer><valueInteger value=\"37\"></valueInteger></answer></item></QuestionnaireResponse></resource></entry></Bundle>",
            APPLICATION_XML,
            ALL),
        arguments(
            "<Bundle xmlns=\"http://hl7.org/fhir\"><id value=\"7a5af172-0d72-4395-8c60-c33ece5eaad2\"></id><meta><lastUpdated value=\"2022-09-05T19:06:15.141+02:00\"></lastUpdated><profile value=\"https://demis.rki.de/fhir/StructureDefinition/ReportBundle\"></profile></meta><identifier><system value=\"https://demis.rki.de/fhir/NamingSystem/NotificationBundleId\"></system><value value=\"58ab4c30-71a2-431f-a776-0febb51969f4\"></value></identifier><type value=\"document\"></type><timestamp value=\"2022-09-05T19:06:15.141+02:00\"></timestamp><entry><fullUrl value=\"https://demis.rki.de/fhir/Composition/fee47acd-4608-43f6-8b39-2fc48ce29057\"></fullUrl><resource><Composition xmlns=\"http://hl7.org/fhir\"><id value=\"fee47acd-4608-43f6-8b39-2fc48ce29057\"></id><meta><profile value=\"https://demis.rki.de/fhir/StructureDefinition/ReportBedOccupancy\"></profile></meta><identifier><system value=\"https://demis.rki.de/fhir/NamingSystem/NotificationId\"></system><value value=\"24919c8c-68c9-429c-9d92-2c5bce70715b\"></value></identifier><status value=\"final\"></status><type><coding><system value=\"http://loinc.org\"></system><code value=\"80563-0\"></code><display value=\"Report\"></display></coding></type><category><coding><system value=\"https://demis.rki.de/fhir/CodeSystem/reportCategory\"></system><code value=\"bedOccupancyReport\"></code><display value=\"Bettenbelegungsstatistik\"></display></coding></category><subject><identifier><system value=\"https://demis.rki.de/fhir/NamingSystem/InekStandortId\"></system><value value=\"987654\"></value></identifier></subject><date value=\"2022-09-05T19:06:15+02:00\"></date><author><reference value=\"PractitionerRole/ae4ff942-9db0-4d34-b6b5-1e2363c81621\"></reference></author><title value=\"Bericht (Krankenhausbettenbelegungsstatistik)\"></title><section><code><coding><system value=\"https://demis.rki.de/fhir/CodeSystem/reportSection\"></system><code value=\"statisticInformationBedOccupancySection\"></code><display value=\"Abschnitt 'Statistische Informationen zur Krankenhausbettenbelegung'\"></display></coding></code><entry><reference value=\"QuestionnaireResponse/1627a582-c3b1-44ac-a5fd-8970fc6a6289\"></reference></entry></section></Composition></resource></entry><entry><fullUrl value=\"https://demis.rki.de/fhir/Organization/7c4a1bc4-b31e-4833-ba50-ba43fd7c1926\"></fullUrl><resource><Organization xmlns=\"http://hl7.org/fhir\"><id value=\"7c4a1bc4-b31e-4833-ba50-ba43fd7c1926\"></id><meta><profile value=\"https://demis.rki.de/fhir/StructureDefinition/NotifierFacility\"></profile></meta><type><coding><system value=\"https://demis.rki.de/fhir/CodeSystem/organizationType\"></system><code value=\"hospital\"></code><display value=\"Krankenhaus\"></display></coding></type><name value=\"Testkrankenhaus - gematik GmbH\"></name><telecom><system value=\"phone\"></system><value value=\"0123456789\"></value><use value=\"work\"></use></telecom><address><line value=\"Friedrichstr. 136\"></line><city value=\"Berlin\"></city><postalCode value=\"10117\"></postalCode></address></Organization></resource></entry><entry><fullUrl value=\"https://demis.rki.de/fhir/PractitionerRole/ae4ff942-9db0-4d34-b6b5-1e2363c81621\"></fullUrl><resource><PractitionerRole xmlns=\"http://hl7.org/fhir\"><id value=\"ae4ff942-9db0-4d34-b6b5-1e2363c81621\"></id><meta><profile value=\"https://demis.rki.de/fhir/StructureDefinition/NotifierRole\"></profile></meta><organization><reference value=\"Organization/7c4a1bc4-b31e-4833-ba50-ba43fd7c1926\"></reference></organization></PractitionerRole></resource></entry><entry><fullUrl value=\"https://demis.rki.de/fhir/QuestionnaireResponse/1627a582-c3b1-44ac-a5fd-8970fc6a6289\"></fullUrl><resource><QuestionnaireResponse xmlns=\"http://hl7.org/fhir\"><id value=\"1627a582-c3b1-44ac-a5fd-8970fc6a6289\"></id><meta><profile value=\"https://demis.rki.de/fhir/StructureDefinition/StatisticInformationBedOccupancy\"></profile></meta><questionnaire value=\"https://demis.rki.de/fhir/Questionnaire/StatisticQuestionsBedOccupancy\"></questionnaire><status value=\"completed\"></status><item><linkId value=\"numberOperableBedsGeneralWardAdults\"></linkId><answer><valueInteger value=\"250\"></valueInteger></answer></item><item><linkId value=\"numberOccupiedBedsGeneralWardAdults\"></linkId><answer><valueInteger value=\"221\"></valueInteger></answer></item><item><linkId value=\"numberOperableBedsGeneralWardChildren\"></linkId><answer><valueInteger value=\"50\"></valueInteger></answer></item><item><linkId value=\"numberOccupiedBedsGeneralWardChildren\"></linkId><answer><valueInteger value=\"37\"></valueInteger></answer></item></QuestionnaireResponse></resource></entry></Bundle>",
            APPLICATION_XML,
            APPLICATION_XML),
        arguments(
            "{\"resourceType\":\"Bundle\",\"id\":\"24a55a68-7121-4a89-9178-8f7a1e639aec\",\"meta\":{\"lastUpdated\":\"2022-09-05T19:05:21.964+02:00\",\"profile\":[\"https://demis.rki.de/fhir/StructureDefinition/ReportBundle\"]},\"identifier\":{\"system\":\"https://demis.rki.de/fhir/NamingSystem/NotificationBundleId\",\"value\":\"5b9a47fa-10c7-4277-b2ca-f12bf2a0a6f7\"},\"type\":\"document\",\"timestamp\":\"2022-09-05T19:05:21.964+02:00\",\"entry\":[{\"fullUrl\":\"https://demis.rki.de/fhir/Composition/5726c782-8d6a-4057-a3d7-a6e72db043bd\",\"resource\":{\"resourceType\":\"Composition\",\"id\":\"5726c782-8d6a-4057-a3d7-a6e72db043bd\",\"meta\":{\"profile\":[\"https://demis.rki.de/fhir/StructureDefinition/ReportBedOccupancy\"]},\"identifier\":{\"system\":\"https://demis.rki.de/fhir/NamingSystem/NotificationId\",\"value\":\"4afd102f-abf1-4ddc-980e-a06080034d61\"},\"status\":\"final\",\"type\":{\"coding\":[{\"system\":\"http://loinc.org\",\"code\":\"80563-0\",\"display\":\"Report\"}]},\"category\":[{\"coding\":[{\"system\":\"https://demis.rki.de/fhir/CodeSystem/reportCategory\",\"code\":\"bedOccupancyReport\",\"display\":\"Bettenbelegungsstatistik\"}]}],\"subject\":{\"identifier\":{\"system\":\"https://demis.rki.de/fhir/NamingSystem/InekStandortId\",\"value\":\"987654\"}},\"date\":\"2022-09-05T19:05:21+02:00\",\"author\":[{\"reference\":\"PractitionerRole/5a47db03-a48f-4b5e-a638-9a59bb754841\"}],\"title\":\"Bericht (Krankenhausbettenbelegungsstatistik)\",\"section\":[{\"code\":{\"coding\":[{\"system\":\"https://demis.rki.de/fhir/CodeSystem/reportSection\",\"code\":\"statisticInformationBedOccupancySection\",\"display\":\"Abschnitt 'Statistische Informationen zur Krankenhausbettenbelegung'\"}]},\"entry\":[{\"reference\":\"QuestionnaireResponse/36a25277-c394-44a3-9027-f030282343a7\"}]}]}},{\"fullUrl\":\"https://demis.rki.de/fhir/Organization/27911e87-6ce0-4833-85e3-b6fe1279412e\",\"resource\":{\"resourceType\":\"Organization\",\"id\":\"27911e87-6ce0-4833-85e3-b6fe1279412e\",\"meta\":{\"profile\":[\"https://demis.rki.de/fhir/StructureDefinition/NotifierFacility\"]},\"type\":[{\"coding\":[{\"system\":\"https://demis.rki.de/fhir/CodeSystem/organizationType\",\"code\":\"hospital\",\"display\":\"Krankenhaus\"}]}],\"name\":\"Testkrankenhaus - gematik GmbH\",\"telecom\":[{\"system\":\"phone\",\"value\":\"0123456789\",\"use\":\"work\"}],\"address\":[{\"line\":[\"Friedrichstr. 136\"],\"city\":\"Berlin\",\"postalCode\":\"10117\"}]}},{\"fullUrl\":\"https://demis.rki.de/fhir/PractitionerRole/5a47db03-a48f-4b5e-a638-9a59bb754841\",\"resource\":{\"resourceType\":\"PractitionerRole\",\"id\":\"5a47db03-a48f-4b5e-a638-9a59bb754841\",\"meta\":{\"profile\":[\"https://demis.rki.de/fhir/StructureDefinition/NotifierRole\"]},\"organization\":{\"reference\":\"Organization/27911e87-6ce0-4833-85e3-b6fe1279412e\"}}},{\"fullUrl\":\"https://demis.rki.de/fhir/QuestionnaireResponse/36a25277-c394-44a3-9027-f030282343a7\",\"resource\":{\"resourceType\":\"QuestionnaireResponse\",\"id\":\"36a25277-c394-44a3-9027-f030282343a7\",\"meta\":{\"profile\":[\"https://demis.rki.de/fhir/StructureDefinition/StatisticInformationBedOccupancy\"]},\"questionnaire\":\"https://demis.rki.de/fhir/Questionnaire/StatisticQuestionsBedOccupancy\",\"status\":\"completed\",\"item\":[{\"linkId\":\"numberOperableBedsGeneralWardAdults\",\"answer\":[{\"valueInteger\":250}]},{\"linkId\":\"numberOccupiedBedsGeneralWardAdults\",\"answer\":[{\"valueInteger\":221}]},{\"linkId\":\"numberOperableBedsGeneralWardChildren\",\"answer\":[{\"valueInteger\":50}]},{\"linkId\":\"numberOccupiedBedsGeneralWardChildren\",\"answer\":[{\"valueInteger\":37}]}]}}]}",
            APPLICATION_JSON,
            ALL),
        arguments(
            "{\"resourceType\":\"Bundle\",\"id\":\"24a55a68-7121-4a89-9178-8f7a1e639aec\",\"meta\":{\"lastUpdated\":\"2022-09-05T19:05:21.964+02:00\",\"profile\":[\"https://demis.rki.de/fhir/StructureDefinition/ReportBundle\"]},\"identifier\":{\"system\":\"https://demis.rki.de/fhir/NamingSystem/NotificationBundleId\",\"value\":\"5b9a47fa-10c7-4277-b2ca-f12bf2a0a6f7\"},\"type\":\"document\",\"timestamp\":\"2022-09-05T19:05:21.964+02:00\",\"entry\":[{\"fullUrl\":\"https://demis.rki.de/fhir/Composition/5726c782-8d6a-4057-a3d7-a6e72db043bd\",\"resource\":{\"resourceType\":\"Composition\",\"id\":\"5726c782-8d6a-4057-a3d7-a6e72db043bd\",\"meta\":{\"profile\":[\"https://demis.rki.de/fhir/StructureDefinition/ReportBedOccupancy\"]},\"identifier\":{\"system\":\"https://demis.rki.de/fhir/NamingSystem/NotificationId\",\"value\":\"4afd102f-abf1-4ddc-980e-a06080034d61\"},\"status\":\"final\",\"type\":{\"coding\":[{\"system\":\"http://loinc.org\",\"code\":\"80563-0\",\"display\":\"Report\"}]},\"category\":[{\"coding\":[{\"system\":\"https://demis.rki.de/fhir/CodeSystem/reportCategory\",\"code\":\"bedOccupancyReport\",\"display\":\"Bettenbelegungsstatistik\"}]}],\"subject\":{\"identifier\":{\"system\":\"https://demis.rki.de/fhir/NamingSystem/InekStandortId\",\"value\":\"987654\"}},\"date\":\"2022-09-05T19:05:21+02:00\",\"author\":[{\"reference\":\"PractitionerRole/5a47db03-a48f-4b5e-a638-9a59bb754841\"}],\"title\":\"Bericht (Krankenhausbettenbelegungsstatistik)\",\"section\":[{\"code\":{\"coding\":[{\"system\":\"https://demis.rki.de/fhir/CodeSystem/reportSection\",\"code\":\"statisticInformationBedOccupancySection\",\"display\":\"Abschnitt 'Statistische Informationen zur Krankenhausbettenbelegung'\"}]},\"entry\":[{\"reference\":\"QuestionnaireResponse/36a25277-c394-44a3-9027-f030282343a7\"}]}]}},{\"fullUrl\":\"https://demis.rki.de/fhir/Organization/27911e87-6ce0-4833-85e3-b6fe1279412e\",\"resource\":{\"resourceType\":\"Organization\",\"id\":\"27911e87-6ce0-4833-85e3-b6fe1279412e\",\"meta\":{\"profile\":[\"https://demis.rki.de/fhir/StructureDefinition/NotifierFacility\"]},\"type\":[{\"coding\":[{\"system\":\"https://demis.rki.de/fhir/CodeSystem/organizationType\",\"code\":\"hospital\",\"display\":\"Krankenhaus\"}]}],\"name\":\"Testkrankenhaus - gematik GmbH\",\"telecom\":[{\"system\":\"phone\",\"value\":\"0123456789\",\"use\":\"work\"}],\"address\":[{\"line\":[\"Friedrichstr. 136\"],\"city\":\"Berlin\",\"postalCode\":\"10117\"}]}},{\"fullUrl\":\"https://demis.rki.de/fhir/PractitionerRole/5a47db03-a48f-4b5e-a638-9a59bb754841\",\"resource\":{\"resourceType\":\"PractitionerRole\",\"id\":\"5a47db03-a48f-4b5e-a638-9a59bb754841\",\"meta\":{\"profile\":[\"https://demis.rki.de/fhir/StructureDefinition/NotifierRole\"]},\"organization\":{\"reference\":\"Organization/27911e87-6ce0-4833-85e3-b6fe1279412e\"}}},{\"fullUrl\":\"https://demis.rki.de/fhir/QuestionnaireResponse/36a25277-c394-44a3-9027-f030282343a7\",\"resource\":{\"resourceType\":\"QuestionnaireResponse\",\"id\":\"36a25277-c394-44a3-9027-f030282343a7\",\"meta\":{\"profile\":[\"https://demis.rki.de/fhir/StructureDefinition/StatisticInformationBedOccupancy\"]},\"questionnaire\":\"https://demis.rki.de/fhir/Questionnaire/StatisticQuestionsBedOccupancy\",\"status\":\"completed\",\"item\":[{\"linkId\":\"numberOperableBedsGeneralWardAdults\",\"answer\":[{\"valueInteger\":250}]},{\"linkId\":\"numberOccupiedBedsGeneralWardAdults\",\"answer\":[{\"valueInteger\":221}]},{\"linkId\":\"numberOperableBedsGeneralWardChildren\",\"answer\":[{\"valueInteger\":50}]},{\"linkId\":\"numberOccupiedBedsGeneralWardChildren\",\"answer\":[{\"valueInteger\":37}]}]}}]}",
            APPLICATION_JSON,
            APPLICATION_JSON),
        arguments(
            "{\"resourceType\":\"Bundle\",\"id\":\"24a55a68-7121-4a89-9178-8f7a1e639aec\",\"meta\":{\"lastUpdated\":\"2022-09-05T19:05:21.964+02:00\",\"profile\":[\"https://demis.rki.de/fhir/StructureDefinition/ReportBundle\"]},\"identifier\":{\"system\":\"https://demis.rki.de/fhir/NamingSystem/NotificationBundleId\",\"value\":\"5b9a47fa-10c7-4277-b2ca-f12bf2a0a6f7\"},\"type\":\"document\",\"timestamp\":\"2022-09-05T19:05:21.964+02:00\",\"entry\":[{\"fullUrl\":\"https://demis.rki.de/fhir/Composition/5726c782-8d6a-4057-a3d7-a6e72db043bd\",\"resource\":{\"resourceType\":\"Composition\",\"id\":\"5726c782-8d6a-4057-a3d7-a6e72db043bd\",\"meta\":{\"profile\":[\"https://demis.rki.de/fhir/StructureDefinition/ReportBedOccupancy\"]},\"identifier\":{\"system\":\"https://demis.rki.de/fhir/NamingSystem/NotificationId\",\"value\":\"4afd102f-abf1-4ddc-980e-a06080034d61\"},\"status\":\"final\",\"type\":{\"coding\":[{\"system\":\"http://loinc.org\",\"code\":\"80563-0\",\"display\":\"Report\"}]},\"category\":[{\"coding\":[{\"system\":\"https://demis.rki.de/fhir/CodeSystem/reportCategory\",\"code\":\"bedOccupancyReport\",\"display\":\"Bettenbelegungsstatistik\"}]}],\"subject\":{\"identifier\":{\"system\":\"https://demis.rki.de/fhir/NamingSystem/InekStandortId\",\"value\":\"987654\"}},\"date\":\"2022-09-05T19:05:21+02:00\",\"author\":[{\"reference\":\"PractitionerRole/5a47db03-a48f-4b5e-a638-9a59bb754841\"}],\"title\":\"Bericht (Krankenhausbettenbelegungsstatistik)\",\"section\":[{\"code\":{\"coding\":[{\"system\":\"https://demis.rki.de/fhir/CodeSystem/reportSection\",\"code\":\"statisticInformationBedOccupancySection\",\"display\":\"Abschnitt 'Statistische Informationen zur Krankenhausbettenbelegung'\"}]},\"entry\":[{\"reference\":\"QuestionnaireResponse/36a25277-c394-44a3-9027-f030282343a7\"}]}]}},{\"fullUrl\":\"https://demis.rki.de/fhir/Organization/27911e87-6ce0-4833-85e3-b6fe1279412e\",\"resource\":{\"resourceType\":\"Organization\",\"id\":\"27911e87-6ce0-4833-85e3-b6fe1279412e\",\"meta\":{\"profile\":[\"https://demis.rki.de/fhir/StructureDefinition/NotifierFacility\"]},\"type\":[{\"coding\":[{\"system\":\"https://demis.rki.de/fhir/CodeSystem/organizationType\",\"code\":\"hospital\",\"display\":\"Krankenhaus\"}]}],\"name\":\"Testkrankenhaus - gematik GmbH\",\"telecom\":[{\"system\":\"phone\",\"value\":\"0123456789\",\"use\":\"work\"}],\"address\":[{\"line\":[\"Friedrichstr. 136\"],\"city\":\"Berlin\",\"postalCode\":\"10117\"}]}},{\"fullUrl\":\"https://demis.rki.de/fhir/PractitionerRole/5a47db03-a48f-4b5e-a638-9a59bb754841\",\"resource\":{\"resourceType\":\"PractitionerRole\",\"id\":\"5a47db03-a48f-4b5e-a638-9a59bb754841\",\"meta\":{\"profile\":[\"https://demis.rki.de/fhir/StructureDefinition/NotifierRole\"]},\"organization\":{\"reference\":\"Organization/27911e87-6ce0-4833-85e3-b6fe1279412e\"}}},{\"fullUrl\":\"https://demis.rki.de/fhir/QuestionnaireResponse/36a25277-c394-44a3-9027-f030282343a7\",\"resource\":{\"resourceType\":\"QuestionnaireResponse\",\"id\":\"36a25277-c394-44a3-9027-f030282343a7\",\"meta\":{\"profile\":[\"https://demis.rki.de/fhir/StructureDefinition/StatisticInformationBedOccupancy\"]},\"questionnaire\":\"https://demis.rki.de/fhir/Questionnaire/StatisticQuestionsBedOccupancy\",\"status\":\"completed\",\"item\":[{\"linkId\":\"numberOperableBedsGeneralWardAdults\",\"answer\":[{\"valueInteger\":250}]},{\"linkId\":\"numberOccupiedBedsGeneralWardAdults\",\"answer\":[{\"valueInteger\":221}]},{\"linkId\":\"numberOperableBedsGeneralWardChildren\",\"answer\":[{\"valueInteger\":50}]},{\"linkId\":\"numberOccupiedBedsGeneralWardChildren\",\"answer\":[{\"valueInteger\":37}]}]}}]}",
            APPLICATION_JSON,
            APPLICATION_XML),
        arguments(
            "<Bundle xmlns=\"http://hl7.org/fhir\"><id value=\"7a5af172-0d72-4395-8c60-c33ece5eaad2\"></id><meta><lastUpdated value=\"2022-09-05T19:06:15.141+02:00\"></lastUpdated><profile value=\"https://demis.rki.de/fhir/StructureDefinition/ReportBundle\"></profile></meta><identifier><system value=\"https://demis.rki.de/fhir/NamingSystem/NotificationBundleId\"></system><value value=\"58ab4c30-71a2-431f-a776-0febb51969f4\"></value></identifier><type value=\"document\"></type><timestamp value=\"2022-09-05T19:06:15.141+02:00\"></timestamp><entry><fullUrl value=\"https://demis.rki.de/fhir/Composition/fee47acd-4608-43f6-8b39-2fc48ce29057\"></fullUrl><resource><Composition xmlns=\"http://hl7.org/fhir\"><id value=\"fee47acd-4608-43f6-8b39-2fc48ce29057\"></id><meta><profile value=\"https://demis.rki.de/fhir/StructureDefinition/ReportBedOccupancy\"></profile></meta><identifier><system value=\"https://demis.rki.de/fhir/NamingSystem/NotificationId\"></system><value value=\"24919c8c-68c9-429c-9d92-2c5bce70715b\"></value></identifier><status value=\"final\"></status><type><coding><system value=\"http://loinc.org\"></system><code value=\"80563-0\"></code><display value=\"Report\"></display></coding></type><category><coding><system value=\"https://demis.rki.de/fhir/CodeSystem/reportCategory\"></system><code value=\"bedOccupancyReport\"></code><display value=\"Bettenbelegungsstatistik\"></display></coding></category><subject><identifier><system value=\"https://demis.rki.de/fhir/NamingSystem/InekStandortId\"></system><value value=\"987654\"></value></identifier></subject><date value=\"2022-09-05T19:06:15+02:00\"></date><author><reference value=\"PractitionerRole/ae4ff942-9db0-4d34-b6b5-1e2363c81621\"></reference></author><title value=\"Bericht (Krankenhausbettenbelegungsstatistik)\"></title><section><code><coding><system value=\"https://demis.rki.de/fhir/CodeSystem/reportSection\"></system><code value=\"statisticInformationBedOccupancySection\"></code><display value=\"Abschnitt 'Statistische Informationen zur Krankenhausbettenbelegung'\"></display></coding></code><entry><reference value=\"QuestionnaireResponse/1627a582-c3b1-44ac-a5fd-8970fc6a6289\"></reference></entry></section></Composition></resource></entry><entry><fullUrl value=\"https://demis.rki.de/fhir/Organization/7c4a1bc4-b31e-4833-ba50-ba43fd7c1926\"></fullUrl><resource><Organization xmlns=\"http://hl7.org/fhir\"><id value=\"7c4a1bc4-b31e-4833-ba50-ba43fd7c1926\"></id><meta><profile value=\"https://demis.rki.de/fhir/StructureDefinition/NotifierFacility\"></profile></meta><type><coding><system value=\"https://demis.rki.de/fhir/CodeSystem/organizationType\"></system><code value=\"hospital\"></code><display value=\"Krankenhaus\"></display></coding></type><name value=\"Testkrankenhaus - gematik GmbH\"></name><telecom><system value=\"phone\"></system><value value=\"0123456789\"></value><use value=\"work\"></use></telecom><address><line value=\"Friedrichstr. 136\"></line><city value=\"Berlin\"></city><postalCode value=\"10117\"></postalCode></address></Organization></resource></entry><entry><fullUrl value=\"https://demis.rki.de/fhir/PractitionerRole/ae4ff942-9db0-4d34-b6b5-1e2363c81621\"></fullUrl><resource><PractitionerRole xmlns=\"http://hl7.org/fhir\"><id value=\"ae4ff942-9db0-4d34-b6b5-1e2363c81621\"></id><meta><profile value=\"https://demis.rki.de/fhir/StructureDefinition/NotifierRole\"></profile></meta><organization><reference value=\"Organization/7c4a1bc4-b31e-4833-ba50-ba43fd7c1926\"></reference></organization></PractitionerRole></resource></entry><entry><fullUrl value=\"https://demis.rki.de/fhir/QuestionnaireResponse/1627a582-c3b1-44ac-a5fd-8970fc6a6289\"></fullUrl><resource><QuestionnaireResponse xmlns=\"http://hl7.org/fhir\"><id value=\"1627a582-c3b1-44ac-a5fd-8970fc6a6289\"></id><meta><profile value=\"https://demis.rki.de/fhir/StructureDefinition/StatisticInformationBedOccupancy\"></profile></meta><questionnaire value=\"https://demis.rki.de/fhir/Questionnaire/StatisticQuestionsBedOccupancy\"></questionnaire><status value=\"completed\"></status><item><linkId value=\"numberOperableBedsGeneralWardAdults\"></linkId><answer><valueInteger value=\"250\"></valueInteger></answer></item><item><linkId value=\"numberOccupiedBedsGeneralWardAdults\"></linkId><answer><valueInteger value=\"221\"></valueInteger></answer></item><item><linkId value=\"numberOperableBedsGeneralWardChildren\"></linkId><answer><valueInteger value=\"50\"></valueInteger></answer></item><item><linkId value=\"numberOccupiedBedsGeneralWardChildren\"></linkId><answer><valueInteger value=\"37\"></valueInteger></answer></item></QuestionnaireResponse></resource></entry></Bundle>",
            APPLICATION_XML,
            APPLICATION_JSON));
  }

  public static Stream<Arguments> hlsCombinations() {
    return Stream.of(
        arguments(
            """
                    [
                                                            {
                                                                "id": 987653,
                                                                "ik": 987654321,
                                                                "label": "Testkrankenhaus - gematik GmbH",
                                                                "postalCode": 10117,
                                                                "city": "Berlin",
                                                                "line": "Friedrichstr.",
                                                                "houseNumber": "136"
                                                            }
                                                        ]""",
            "Validation Exception (for id 987654 no InEK data found)"));
  }

  public static Stream<Arguments> hlsCombinationsWithWeirdData() {
    return Stream.of(
        arguments(
            """
                            [
                                                                    {
                                                                        "id": 987654,
                                                                        "ik": 987654321,
                                                                        "label": "anderes Testkrankenhaus",
                                                                        "postalCode": 10117,
                                                                        "city": "Berlin",
                                                                        "line": "Friedrichstr.",
                                                                        "houseNumber": "136"
                                                                    }
                                                                ]""",
            "Validation Exception (Entered data: Testkrankenhaus - gematik GmbH | InEK Data: anderes Testkrankenhaus)"),
        arguments(
            """
                            [
                                                                    {
                                                                        "id": 987654,
                                                                        "ik": 987654321,
                                                                        "label": "Testkrankenhaus - gematik GmbH",
                                                                        "postalCode": 10116,
                                                                        "city": "Berlin",
                                                                        "line": "Friedrichstr.",
                                                                        "houseNumber": "136"
                                                                    }
                                                                ]""",
            "Validation Exception (Entered data: 10117 | InEK Data: 10116)"),
        arguments(
            """
                            [
                                                                    {
                                                                        "id": 987654,
                                                                        "ik": 987654321,
                                                                        "label": "Testkrankenhaus - gematik GmbH",
                                                                        "postalCode": 10117,
                                                                        "city": "Hamburg",
                                                                        "line": "Friedrichstr.",
                                                                        "houseNumber": "136"
                                                                    }
                                                                ]""",
            "Validation Exception (Entered data: Berlin | InEK Data: Hamburg)"),
        arguments(
            """
                            [
                                                                    {
                                                                        "id": 987654,
                                                                        "ik": 987654321,
                                                                        "label": "Testkrankenhaus - gematik GmbH",
                                                                        "postalCode": 10117,
                                                                        "city": "Berlin",
                                                                        "line": "Friedrichstrasse",
                                                                        "houseNumber": "136"
                                                                    }
                                                                ]""",
            "Validation Exception (Entered data: Friedrichstr. 136 | InEK Data: Friedrichstrasse 136)"),
        arguments(
            """
                            [
                                                                    {
                                                                        "id": 987654,
                                                                        "ik": 987654321,
                                                                        "label": "Testkrankenhaus - gematik GmbH",
                                                                        "postalCode": 10117,
                                                                        "city": "Berlin",
                                                                        "line": "Friedrichstr.",
                                                                        "houseNumber": "100"
                                                                    }
                                                                ]""",
            "Validation Exception (Entered data: Friedrichstr. 136 | InEK Data: Friedrichstr. 100)"));
  }

  public static Stream<Arguments> validationIssuesCombinations() {
    return Stream.of(
        arguments(
            "<Bundle><id value=\"1\"/><meta><profile value=\"https://demis.rki.de/fhir/StructureDefinition/ReportBundle\"/></meta></Bundle>",
            APPLICATION_XML,
            ALL,
            "{\"resourceType\":\"OperationOutcome\",\"issue\":[{\"severity\":\"error\",\"code\":\"processing\",\"diagnostics\":\"Composition.subject: mindestens erforderlich = 1, aber nur gefunden 0\",\"location\":[\"Bundle.entry[0].resource.ofType(Composition)\",\"Line 33, Col 26\"]}]}",
            "<OperationOutcome xmlns=\"http://hl7.org/fhir\"><issue><severity value=\"error\"/><code value=\"processing\"/><diagnostics value=\"Composition.subject: mindestens erforderlich = 1, aber nur gefunden 0\"/><location value=\"Bundle.entry[0].resource.ofType(Composition)\"/><location value=\"Line 33, Col 26\"/></issue></OperationOutcome>"),
        arguments(
            "<Bundle><id value=\"1\"/><meta><profile value=\"https://demis.rki.de/fhir/StructureDefinition/ReportBundle\"/></meta></Bundle>",
            APPLICATION_XML,
            APPLICATION_XML,
            "{\"resourceType\":\"OperationOutcome\",\"issue\":[{\"severity\":\"error\",\"code\":\"processing\",\"diagnostics\":\"Composition.subject: mindestens erforderlich = 1, aber nur gefunden 0\",\"location\":[\"Bundle.entry[0].resource.ofType(Composition)\",\"Line 33, Col 26\"]}]}",
            "<OperationOutcome xmlns=\"http://hl7.org/fhir\"><issue><severity value=\"error\"/><code value=\"processing\"/><diagnostics value=\"Composition.subject: mindestens erforderlich = 1, aber nur gefunden 0\"/><location value=\"Bundle.entry[0].resource.ofType(Composition)\"/><location value=\"Line 33, Col 26\"/></issue></OperationOutcome>"),
        arguments(
            "{\"resourceType\": \"Parameters\",\"parameter\": [{\"name\": \"content\",\"resource\": {\"resourceType\": \"Bundle\",\"id\": \"1\", \"meta\": {\"profile\": [\"https://demis.rki.de/fhir/StructureDefinition/ReportBundle\"]}}}]}",
            APPLICATION_JSON,
            ALL,
            "{\"resourceType\":\"OperationOutcome\",\"issue\":[{\"severity\":\"error\",\"code\":\"processing\",\"diagnostics\":\"Composition.subject: mindestens erforderlich = 1, aber nur gefunden 0\",\"location\":[\"Bundle.entry[0].resource.ofType(Composition)\",\"Line 33, Col 26\"]}]}",
            "{\"resourceType\":\"OperationOutcome\",\"issue\":[{\"severity\":\"error\",\"code\":\"processing\",\"diagnostics\":\"Composition.subject: mindestens erforderlich = 1, aber nur gefunden 0\",\"location\":[\"Bundle.entry[0].resource.ofType(Composition)\",\"Line 33, Col 26\"]}]}"),
        arguments(
            "{\"resourceType\":\"Bundle\",\"id\":\"1\", \"meta\": {\"profile\": [\"https://demis.rki.de/fhir/StructureDefinition/ReportBundle\"]}}",
            APPLICATION_JSON,
            APPLICATION_JSON,
            "{\"resourceType\":\"OperationOutcome\",\"issue\":[{\"severity\":\"error\",\"code\":\"processing\",\"diagnostics\":\"Composition.subject: mindestens erforderlich = 1, aber nur gefunden 0\",\"location\":[\"Bundle.entry[0].resource.ofType(Composition)\",\"Line 33, Col 26\"]}]}",
            "{\"resourceType\":\"OperationOutcome\",\"issue\":[{\"severity\":\"error\",\"code\":\"processing\",\"diagnostics\":\"Composition.subject: mindestens erforderlich = 1, aber nur gefunden 0\",\"location\":[\"Bundle.entry[0].resource.ofType(Composition)\",\"Line 33, Col 26\"]}]}"),
        arguments(
            "{\"resourceType\":\"Bundle\",\"id\":\"1\", \"meta\": {\"profile\": [\"https://demis.rki.de/fhir/StructureDefinition/ReportBundle\"]}}",
            APPLICATION_JSON,
            APPLICATION_XML,
            "{\"resourceType\":\"OperationOutcome\",\"issue\":[{\"severity\":\"error\",\"code\":\"processing\",\"diagnostics\":\"Composition.subject: mindestens erforderlich = 1, aber nur gefunden 0\",\"location\":[\"Bundle.entry[0].resource.ofType(Composition)\",\"Line 33, Col 26\"]}]}",
            "<OperationOutcome xmlns=\"http://hl7.org/fhir\"><issue><severity value=\"error\"/><code value=\"processing\"/><diagnostics value=\"Composition.subject: mindestens erforderlich = 1, aber nur gefunden 0\"/><location value=\"Bundle.entry[0].resource.ofType(Composition)\"/><location value=\"Line 33, Col 26\"/></issue></OperationOutcome>"),
        arguments(
            "<Parameters><parameter><name value=\"content\"/><resource><Bundle><id value=\"1\"/><meta><profile value=\"https://demis.rki.de/fhir/StructureDefinition/ReportBundle\"/></meta></Bundle></resource></parameter></Parameters>",
            APPLICATION_XML,
            APPLICATION_JSON,
            "{\"resourceType\":\"OperationOutcome\",\"issue\":[{\"severity\":\"error\",\"code\":\"processing\",\"diagnostics\":\"Composition.subject: mindestens erforderlich = 1, aber nur gefunden 0\",\"location\":[\"Bundle.entry[0].resource.ofType(Composition)\",\"Line 33, Col 26\"]}]}",
            "{\"resourceType\":\"OperationOutcome\",\"issue\":[{\"severity\":\"error\",\"code\":\"processing\",\"diagnostics\":\"Composition.subject: mindestens erforderlich = 1, aber nur gefunden 0\",\"location\":[\"Bundle.entry[0].resource.ofType(Composition)\",\"Line 33, Col 26\"]}]}"));
  }

  public static Stream<Arguments> validationServiceIssuesCombinations() {
    return Stream.of(
        arguments(
            "<Bundle><id value=\"1\"/><meta><profile value=\"https://demis.rki.de/fhir/StructureDefinition/ReportBundle\"/></meta></Bundle>",
            APPLICATION_XML,
            ALL,
            "{\"resourceType\":\"OperationOutcome\",\"issue\":[{\"severity\":\"error\",\"code\":\"processing\",\"diagnostics\":\"Composition.subject: mindestens erforderlich = 1, aber nur gefunden 0\",\"location\":[\"Bundle.entry[0].resource.ofType(Composition)\",\"Line 33, Col 26\"]}]}",
            "<OperationOutcome xmlns=\"http://hl7.org/fhir\"><issue><severity value=\"error\"/><code value=\"exception\"/><diagnostics value=\"00000000-0000-0000-0000-000000000000: Internal Error (Error-ID-RPS-000)\"/></issue></OperationOutcome>"),
        arguments(
            "<Bundle><id value=\"1\"/><meta><profile value=\"https://demis.rki.de/fhir/StructureDefinition/ReportBundle\"/></meta></Bundle>",
            APPLICATION_XML,
            APPLICATION_XML,
            "{\"resourceType\":\"OperationOutcome\",\"issue\":[{\"severity\":\"error\",\"code\":\"processing\",\"diagnostics\":\"Composition.subject: mindestens erforderlich = 1, aber nur gefunden 0\",\"location\":[\"Bundle.entry[0].resource.ofType(Composition)\",\"Line 33, Col 26\"]}]}",
            "<OperationOutcome xmlns=\"http://hl7.org/fhir\"><issue><severity value=\"error\"/><code value=\"exception\"/><diagnostics value=\"00000000-0000-0000-0000-000000000000: Internal Error (Error-ID-RPS-000)\"/></issue></OperationOutcome>"),
        arguments(
            "{\"resourceType\": \"Parameters\",\"parameter\": [{\"name\": \"content\",\"resource\": {\"resourceType\": \"Bundle\",\"id\": \"1\", \"meta\": {\"profile\": [\"https://demis.rki.de/fhir/StructureDefinition/ReportBundle\"]}}}]}",
            APPLICATION_JSON,
            ALL,
            "{\"resourceType\":\"OperationOutcome\",\"issue\":[{\"severity\":\"error\",\"code\":\"processing\",\"diagnostics\":\"Composition.subject: mindestens erforderlich = 1, aber nur gefunden 0\",\"location\":[\"Bundle.entry[0].resource.ofType(Composition)\",\"Line 33, Col 26\"]}]}",
            "{\"resourceType\":\"OperationOutcome\",\"issue\":[{\"severity\":\"error\",\"code\":\"exception\",\"diagnostics\":\"00000000-0000-0000-0000-000000000000: Internal Error (Error-ID-RPS-000)\"}]}"),
        arguments(
            "{\"resourceType\":\"Bundle\",\"id\":\"1\", \"meta\": {\"profile\": [\"https://demis.rki.de/fhir/StructureDefinition/ReportBundle\"]}}",
            APPLICATION_JSON,
            APPLICATION_JSON,
            "{\"resourceType\":\"OperationOutcome\",\"issue\":[{\"severity\":\"error\",\"code\":\"processing\",\"diagnostics\":\"Composition.subject: mindestens erforderlich = 1, aber nur gefunden 0\",\"location\":[\"Bundle.entry[0].resource.ofType(Composition)\",\"Line 33, Col 26\"]}]}",
            "{\"resourceType\":\"OperationOutcome\",\"issue\":[{\"severity\":\"error\",\"code\":\"exception\",\"diagnostics\":\"00000000-0000-0000-0000-000000000000: Internal Error (Error-ID-RPS-000)\"}]}"),
        arguments(
            "{\"resourceType\":\"Bundle\",\"id\":\"1\", \"meta\": {\"profile\": [\"https://demis.rki.de/fhir/StructureDefinition/ReportBundle\"]}}",
            APPLICATION_JSON,
            APPLICATION_XML,
            "{\"resourceType\":\"OperationOutcome\",\"issue\":[{\"severity\":\"error\",\"code\":\"processing\",\"diagnostics\":\"Composition.subject: mindestens erforderlich = 1, aber nur gefunden 0\",\"location\":[\"Bundle.entry[0].resource.ofType(Composition)\",\"Line 33, Col 26\"]}]}",
            "<OperationOutcome xmlns=\"http://hl7.org/fhir\"><issue><severity value=\"error\"/><code value=\"exception\"/><diagnostics value=\"00000000-0000-0000-0000-000000000000: Internal Error (Error-ID-RPS-000)\"/></issue></OperationOutcome>"),
        arguments(
            "<Parameters><parameter><name value=\"content\"/><resource><Bundle><id value=\"1\"/><meta><profile value=\"https://demis.rki.de/fhir/StructureDefinition/ReportBundle\"/></meta></Bundle></resource></parameter></Parameters>",
            APPLICATION_XML,
            APPLICATION_JSON,
            "{\"resourceType\":\"OperationOutcome\",\"issue\":[{\"severity\":\"error\",\"code\":\"processing\",\"diagnostics\":\"Composition.subject: mindestens erforderlich = 1, aber nur gefunden 0\",\"location\":[\"Bundle.entry[0].resource.ofType(Composition)\",\"Line 33, Col 26\"]}]}",
            "{\"resourceType\":\"OperationOutcome\",\"issue\":[{\"severity\":\"error\",\"code\":\"exception\",\"diagnostics\":\"00000000-0000-0000-0000-000000000000: Internal Error (Error-ID-RPS-000)\"}]}"));
  }
}
