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

package de.gematik.demis.reportprocessingservice.security;

import static org.springframework.http.HttpHeaders.*;
import static org.springframework.http.HttpMethod.*;
import static org.springframework.web.cors.CorsConfiguration.ALL;

import de.gematik.demis.reportprocessingservice.properties.OAuth2Properties;
import de.gematik.demis.reportprocessingservice.security.converters.JwtAuthoritiesConverter;
import de.gematik.demis.reportprocessingservice.security.filters.CustomAccessDeniedHandler;
import de.gematik.demis.reportprocessingservice.security.filters.CustomAuthenticationEntryPoint;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@EnableWebSecurity
@EnableMethodSecurity
@Configuration
@AllArgsConstructor
@Slf4j
public class SecurityConfiguration {
  private final JwtAuthoritiesConverter jwtAuthoritiesConverter;

  private final CustomAccessDeniedHandler accessDeniedHandler;

  private final CustomAuthenticationEntryPoint entryPoint;

  private final OAuth2Properties oAuth2Properties;

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
    httpSecurity
        .csrf(AbstractHttpConfigurer::disable)
        .cors(Customizer.withDefaults())
        .authorizeHttpRequests(
            authorize ->
                authorize
                    .requestMatchers(determineOpenRequestPath())
                    .permitAll()
                    .anyRequest()
                    .authenticated());
    if (oAuth2Properties.isValidateJwt()) {
      httpSecurity.oauth2ResourceServer(
          oauth2 ->
              oauth2
                  .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                  .authenticationEntryPoint(entryPoint)
                  .accessDeniedHandler(accessDeniedHandler));
    }
    return httpSecurity
        .sessionManagement(
            sessionMgmt -> sessionMgmt.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .build();
  }

  private String[] determineOpenRequestPath() {
    if (oAuth2Properties.isValidateJwt()) {
      return new String[] {
        "/actuator/**", "/v3/api-docs/**", "/swagger-ui/**", "/v3/api-docs.yaml", "/swagger-ui.html"
      };
    } else {
      return new String[] {"/**"};
    }
  }

  private Converter<Jwt, ? extends AbstractAuthenticationToken> jwtAuthenticationConverter() {
    JwtAuthenticationConverter jwtConverter = new JwtAuthenticationConverter();
    jwtConverter.setJwtGrantedAuthoritiesConverter(jwtAuthoritiesConverter);
    return jwtConverter;
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    final var corsConfiguration = new CorsConfiguration();
    corsConfiguration.setAllowedOrigins(List.of(ALL));
    corsConfiguration.setAllowedMethods(
        List.of(GET.name(), POST.name(), OPTIONS.name(), HEAD.name()));
    corsConfiguration.setAllowedHeaders(List.of(ORIGIN, ACCEPT, CONTENT_TYPE, AUTHORIZATION));
    corsConfiguration.setExposedHeaders(List.of(CONTENT_TYPE, ACCEPT));

    final var configurationSource = new UrlBasedCorsConfigurationSource();
    configurationSource.registerCorsConfiguration("/**", corsConfiguration);
    return configurationSource;
  }
}
