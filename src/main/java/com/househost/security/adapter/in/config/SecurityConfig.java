package com.househost.security.adapter.in.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.househost.shared.dto.ResponseDTO;
import com.househost.security.adapter.in.web.JwtAuthenticationFilter;
import jakarta.servlet.DispatcherType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final String[] ADMIN_ROLES = {"CEO", "CTO", "ADMIN"};
    private static final String[] MANAGEMENT_ROLES = {"CEO", "CTO", "ADMIN", "MANAGER"};
    private static final String[] OPERATIONAL_ROLES = {"CEO", "CTO", "ADMIN", "MANAGER", "RECEPTION"};
    private static final String[] ALL_ROLES = {"CEO", "CTO", "ADMIN", "MANAGER", "RECEPTION", "HOUSEKEEPING"};

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final ObjectMapper objectMapper;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter, ObjectMapper objectMapper) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.objectMapper = objectMapper;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(401);
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.getWriter().write(objectMapper.writeValueAsString(new ResponseDTO("error", "Autenticacao obrigatoria.", null)));
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(403);
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.getWriter().write(objectMapper.writeValueAsString(new ResponseDTO("error", "Voce nao tem permissao para executar esta operacao.", null)));
                        }))
                .authorizeHttpRequests(auth -> auth
                        .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(
                                HttpMethod.POST,
                                "/notifier/provider-feedback/sns"
                        ).permitAll()
                        .requestMatchers("/", "/index.html", "/css/**", "/js/**", "/assets/**", "/favicon.ico").permitAll()
                        .requestMatchers("/public/**").permitAll()
                        .requestMatchers("/auth/login", "/auth/registration", "/auth/users/quick-access").permitAll()
                        .requestMatchers(HttpMethod.POST, "/client-logs").hasAnyRole(ALL_ROLES)
                        .requestMatchers(
                                HttpMethod.POST,
                                "/ratings",
                                "/ratings/**"
                        ).hasAnyRole(OPERATIONAL_ROLES)
                        .requestMatchers(
                                HttpMethod.GET,
                                "/ratings",
                                "/ratings/**"
                        ).hasAnyRole(OPERATIONAL_ROLES)
                        .requestMatchers(
                                HttpMethod.GET,
                                "/financial-transaction-plans/booking/**",
                                "/financial-transaction-plans/*/scheduled/**",
                                "/financial-transaction-plans/commands/reservation/**",
                                "/financial-transaction-plans/*/commands/replacement/**"
                        ).hasAnyRole(OPERATIONAL_ROLES)
                        .requestMatchers(
                                HttpMethod.POST,
                                "/financial-transaction-plans/*/scheduled/*/replace"
                        ).hasAnyRole(OPERATIONAL_ROLES)
                        .requestMatchers(
                                "/data-processing-operations/**",
                                "/legal-basis-assessments/**",
                                "/privacy-policies/**",
                                "/audit-events/**",
                                "/suppliers/**"
                        ).hasAnyRole(ADMIN_ROLES)
                        .requestMatchers(
                                "/financial-transactions/**",
                                "/financial-transaction-plans/**",
                                "/cashiers/**",
                                "/cashier-entries/**",
                                "/cashier-expenses/**"
                        ).hasAnyRole(MANAGEMENT_ROLES)
                        .requestMatchers(HttpMethod.DELETE,
                                "/bookings/**",
                                "/guests/**",
                                "/rooms/**",
                                "/check-ins/**",
                                "/check-outs/**"
                        ).hasAnyRole(MANAGEMENT_ROLES)
                        .requestMatchers(HttpMethod.POST,
                                "/bookings/**",
                                "/guests/**",
                                "/rooms/**",
                                "/check-ins/**",
                                "/check-outs/**"
                        ).hasAnyRole(OPERATIONAL_ROLES)
                        .requestMatchers(HttpMethod.PUT,
                                "/bookings/**",
                                "/guests/**",
                                "/rooms/**",
                                "/check-ins/**",
                                "/check-outs/**"
                        ).hasAnyRole(OPERATIONAL_ROLES)
                        .requestMatchers(request ->
                                HttpMethod.GET.matches(request.getMethod())
                                        && request.getRequestURI().matches("/guests(?:/\\d+)?")
                                        && "false".equalsIgnoreCase(request.getParameter("masked"))
                        ).hasAnyRole(OPERATIONAL_ROLES)
                        .requestMatchers(HttpMethod.GET, "/guests/*/contact", "/guests/*/edit").hasAnyRole(OPERATIONAL_ROLES)
                        .requestMatchers(HttpMethod.GET, "/guests/**").hasAnyRole(OPERATIONAL_ROLES)
                        .requestMatchers(HttpMethod.GET,
                                "/bookings/**",
                                "/rooms/**",
                                "/check-ins/**",
                                "/check-outs/**",
                                "/metrics/**"
                        ).hasAnyRole(ALL_ROLES)
                        .requestMatchers("/auth/users/**").authenticated()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("Authorization", "X-Correlation-ID"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
