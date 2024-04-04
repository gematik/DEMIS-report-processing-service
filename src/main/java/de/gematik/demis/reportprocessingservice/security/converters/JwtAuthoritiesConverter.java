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

package de.gematik.demis.reportprocessingservice.security.converters;

import de.gematik.demis.reportprocessingservice.properties.OAuth2Properties;
import jakarta.validation.constraints.NotNull;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@Component
@Slf4j
public class JwtAuthoritiesConverter implements Converter<Jwt, Collection<GrantedAuthority>> {
  private static final String RESOURCE_ACCESS = "resource_access";
  private static final String ROLES = "roles";
  private final OAuth2Properties oAuth2Properties;

  @Override
  @SuppressWarnings("unchecked")
  public Collection<GrantedAuthority> convert(@NotNull Jwt jwt) {
    Set<GrantedAuthority> grantedAuthorities = new HashSet<>();
    try {
      final var resources =
          (Map<String, List<String>>)
              jwt.getClaimAsMap(RESOURCE_ACCESS).get(oAuth2Properties.getClientId());
      resources
          .get(ROLES)
          .forEach(role -> grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_" + role)));
      return grantedAuthorities;
    } catch (Exception e) {
      log.warn("could not convert roles to authorities: {}", e.getLocalizedMessage());
      return AuthorityUtils.NO_AUTHORITIES;
    }
  }
}
