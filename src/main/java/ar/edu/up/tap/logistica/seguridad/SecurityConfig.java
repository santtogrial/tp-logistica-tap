package ar.edu.up.tap.logistica.seguridad;

import ar.edu.up.tap.logistica.web.dto.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.IOException;
import java.util.List;

/**
 * Middleware de seguridad: define qué endpoints son públicos, cuáles requieren login
 * y qué rol puede usar cada uno. La API es stateless: cada request trae su JWT.
 */
@Configuration
public class SecurityConfig {

    private static final String ADMIN = "ADMIN";
    private static final String OPERADOR = "OPERADOR";
    private static final String CHOFER = "CHOFER";

    private final JwtAuthenticationFilter jwtFilter;
    private final ObjectMapper objectMapper;
    private final List<String> origenesPermitidos;

    public SecurityConfig(JwtAuthenticationFilter jwtFilter, ObjectMapper objectMapper,
                          @Value("${logistica.cors.origenes-permitidos}") List<String> origenesPermitidos) {
        this.jwtFilter = jwtFilter;
        this.objectMapper = objectMapper;
        this.origenesPermitidos = origenesPermitidos;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Públicos
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/seguimiento/**").permitAll()
                        .requestMatchers("/ws/**", "/actuator/health", "/demo.html", "/error").permitAll()
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        // Chofer
                        .requestMatchers(HttpMethod.GET, "/api/rutas/actual").hasRole(CHOFER)
                        .requestMatchers(HttpMethod.POST, "/api/vehiculos/*/posicion").hasRole(CHOFER)
                        .requestMatchers(HttpMethod.POST, "/api/paradas/*/resultado").hasAnyRole(CHOFER, ADMIN)
                        .requestMatchers(HttpMethod.POST, "/api/rutas/*/iniciar", "/api/rutas/*/finalizar")
                            .hasAnyRole(CHOFER, OPERADOR, ADMIN)
                        // Administrador
                        .requestMatchers(HttpMethod.PATCH, "/api/vehiculos/*/estado").hasRole(ADMIN)
                        // Operador
                        .requestMatchers("/api/**").hasAnyRole(OPERADOR, ADMIN)
                        .anyRequest().authenticated())
                .exceptionHandling(e -> e
                        .authenticationEntryPoint((req, res, ex) ->
                                escribirError(req, res, HttpStatus.UNAUTHORIZED, "Falta el token o no es válido"))
                        .accessDeniedHandler((req, res, ex) ->
                                escribirError(req, res, HttpStatus.FORBIDDEN, "Tu rol no tiene permiso para esta operación")))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(origenesPermitidos);
        config.setAllowedMethods(List.of("GET", "POST", "PATCH", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    private void escribirError(HttpServletRequest request, HttpServletResponse response,
                               HttpStatus status, String mensaje) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(),
                ErrorResponse.de(status.value(), status.getReasonPhrase(), mensaje, request.getRequestURI()));
    }
}
