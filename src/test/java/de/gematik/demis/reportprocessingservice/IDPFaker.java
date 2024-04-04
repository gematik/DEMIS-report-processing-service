/*
 * Copyright [2024], gematik GmbH
 *
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
 */

package de.gematik.demis.reportprocessingservice;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.nimbusds.jose.JWSAlgorithm;
import de.gematik.demis.reportprocessingservice.properties.OAuth2Properties;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.jose4j.jwk.JsonWebKeySet;
import org.jose4j.jwk.RsaJsonWebKey;
import org.jose4j.jwk.RsaJwkGenerator;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.lang.JoseException;

@Slf4j
public class IDPFaker {
  private RsaJsonWebKey rsaJsonWebKey;

  public IDPFaker() {
    this.rsaJsonWebKey = generateRsaJsonWebKey();
  }

  public void fakingServer(String issuerUri) {
    String openidConfig =
        "{\n"
            + "  \"issuer\": \""
            + issuerUri
            + "\",\n"
            + "  \"authorization_endpoint\": \""
            + issuerUri
            + "/protocol/openid-connect/auth\",\n"
            + "  \"token_endpoint\": \""
            + issuerUri
            + "/protocol/openid-connect/token\",\n"
            + "  \"token_introspection_endpoint\": \""
            + issuerUri
            + "/protocol/openid-connect/token/introspect\",\n"
            + "  \"end_session_endpoint\": \""
            + issuerUri
            + "/protocol/openid-connect/logout\",\n"
            + "  \"jwks_uri\": \""
            + issuerUri
            + "/protocol/openid-connect/certs\""
            + "}";
    stubFor(
        WireMock.get(urlPathMatching(".*\\.well-known\\/openid-configuration"))
            .willReturn(
                aResponse().withHeader("Content-Type", "application/json").withBody(openidConfig)));
    // create endpoint for public key for signature verification
    stubFor(
        WireMock.get(urlPathMatching(".*\\/protocol\\/openid-connect\\/certs"))
            .willReturn(
                aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withBody(new JsonWebKeySet(generateRsaJsonWebKey()).toJson())));
  }

  public String fakingToken(String issuerUri, OAuth2Properties oAuth2Properties)
      throws JoseException {
    return fakingToken(issuerUri, oAuth2Properties, true);
  }

  public String fakingToken(String issuerUri, OAuth2Properties oAuth2Properties, boolean withIk)
      throws JoseException {

    JwtClaims claims = new JwtClaims();
    claims.setJwtId(UUID.randomUUID().toString());
    claims.setClaim("typ", "Bearer"); // set type of token
    claims.setExpirationTimeMinutesInTheFuture(10);
    claims.setNotBeforeMinutesInThePast(0);
    claims.setIssuedAtToNow();
    claims.setIssuer(issuerUri);
    claims.setAudience("notification-entry-service");
    claims.setSubject(UUID.randomUUID().toString());
    claims.setClaim("acr", "1");
    claims.setClaim("scope", "profile");
    claims.setClaim("azp", "notDemisGateway");
    if (withIk) {
      claims.setClaim("ik", "987654321");
    }
    claims.setClaim(
        "resource_access",
        Map.of(
            oAuth2Properties.getClientId(), Map.of("roles", oAuth2Properties.getAllowedRoles())));

    JsonWebSignature jws = new JsonWebSignature();
    jws.setPayload(claims.toJson());
    jws.setKey(rsaJsonWebKey.getPrivateKey());
    jws.setKeyIdHeaderValue(rsaJsonWebKey.getKeyId());
    jws.setAlgorithmHeaderValue(rsaJsonWebKey.getAlgorithm());
    jws.setHeader("typ", "JWT");

    // Sign  JWS and produce the compact serialization
    return jws.getCompactSerialization();
  }

  private RsaJsonWebKey generateRsaJsonWebKey() {
    if (rsaJsonWebKey == null) {
      try {
        rsaJsonWebKey = RsaJwkGenerator.generateJwk(2048);
        rsaJsonWebKey.setKeyId("k1");
        rsaJsonWebKey.setAlgorithm(JWSAlgorithm.RS256.getName());
        rsaJsonWebKey.setUse("sig");
        return rsaJsonWebKey;
      } catch (JoseException e) {
        throw new RuntimeException(e);
      }
    }
    return rsaJsonWebKey;
  }
}
