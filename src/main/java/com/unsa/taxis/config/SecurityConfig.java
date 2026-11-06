
package com.unsa.taxis.config;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

import java.util.Collection;
import java.util.List;

import org.springframework.http.HttpMethod;

@Configuration
public class SecurityConfig {

    @Value("${app.security.jwt.secret}")
    private String jwtSecret;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JwtEncoder jwtEncoder() {
        SecretKey secretKey = new SecretKeySpec(
                jwtSecret.getBytes(StandardCharsets.UTF_8),
                "HmacSHA256"
        );

        return new NimbusJwtEncoder(
                new ImmutableSecret<>(secretKey)
        );
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        SecretKey secretKey = new SecretKeySpec(
                jwtSecret.getBytes(StandardCharsets.UTF_8),
                "HmacSHA256"
        );

        return NimbusJwtDecoder
                .withSecretKey(secretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
            .cors(Customizer.withDefaults())
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authorizeHttpRequests(auth -> auth

                // Solicitudes CORS
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                // Autenticación pública
                .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()

                // Cambio obligatorio de contraseña
                .requestMatchers(
                        HttpMethod.PUT,
                        "/api/auth/cambiar-password"
                ).hasAuthority("CAMBIAR_PASSWORD")

                // Webhook de WhatsApp
                .requestMatchers(
                        HttpMethod.GET,
                        "/api/whatsapp/webhook"
                ).permitAll()
                .requestMatchers(
                        HttpMethod.POST,
                        "/api/whatsapp/webhook"
                ).permitAll()

                // Solicitud de viaje del pasajero
                .requestMatchers(
                        HttpMethod.POST,
                        "/api/viajes"
                ).permitAll()

                // Viajes
                .requestMatchers(
                        HttpMethod.GET,
                        "/api/viajes"
                ).hasAuthority("ROLE_ADMIN")

                .requestMatchers(
                        HttpMethod.GET,
                        "/api/viajes/*"
                ).hasAnyAuthority("ROLE_ADMIN", "ROLE_CHOFER")

                .requestMatchers(
                        HttpMethod.PUT,
                        "/api/viajes/*/iniciar",
                        "/api/viajes/*/finalizar"
                ).hasAuthority("ROLE_CHOFER")

                // Ofertas propias del chofer
                .requestMatchers(
                        HttpMethod.GET,
                        "/api/choferes/*/ofertas"
                ).hasAuthority("ROLE_CHOFER")

                // Ubicación y estado del chofer
                .requestMatchers(
                        HttpMethod.PUT,
                        "/api/choferes/*/ubicacion",
                        "/api/choferes/*/estado"
                ).hasAuthority("ROLE_CHOFER")

                // Consultas generales de choferes
                .requestMatchers(
                        HttpMethod.GET,
                        "/api/choferes",
                        "/api/choferes/**"
                ).hasAuthority("ROLE_ADMIN")

                // Todo lo demás queda bloqueado
                .anyRequest().denyAll()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .jwtAuthenticationConverter(jwtAuthenticationConverter())
                )
            );

        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {

        JwtAuthenticationConverter converter =
                new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(jwt -> {

            List<String> roles = jwt.getClaimAsStringList("roles");

            if (roles == null) {
                return List.of();
            }

            return roles.stream()
                    .map(rol -> (GrantedAuthority) new SimpleGrantedAuthority(rol))
                    .toList();
        });

        return converter;
    }
}
