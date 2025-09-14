package co.com.pragma.r2dbc.security;



import co.com.pragma.model.auth.gateways.TokenProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
@EnableReactiveMethodSecurity
@Configuration
public class SecurityConfig {

    private final TokenProvider tokenProvider;

    public SecurityConfig(TokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    /**
     * Configura la cadena de filtros de seguridad (SecurityWebFilterChain) para la aplicación
     * utilizando Spring Security en entornos reactivos (WebFlux).
     * <p>
     * Se definen las siguientes reglas:
     * <ul>
     *   <li>Se deshabilitan CSRF, autenticación básica y login por formulario.</li>
     *   <li>Se permite el acceso público a los endpoints de autenticación
     *       (<code>/api/login</code>) y a la documentación de Swagger
     *       (<code>/swagger-ui/**</code>, <code>/v3/api-docs/**</code>, <code>/webjars/**</code>).</li>
     *   <li>El endpoint <code>POST /api/users/create</code> requiere los roles <b>ADMIN</b> o <b>ASESOR</b>.</li>
     *   <li>El endpoint <code>GET /api/users/{identification}</code> requiere el rol <b>CLIENTE</b>.</li>
     *   <li>El endpoint <code>GET /api/users/validate-role/{identification}</code> requiere el rol <b>ASESOR</b>.</li>
     *   <li>Cualquier otro endpoint bajo <code>/api/users/**</code> requiere autenticación vía JWT.</li>
     *   <li>Las demás rutas quedan accesibles sin autenticación.</li>
     * </ul>
     * Además, se agrega un filtro personalizado {@link JwtAuthenticationFilter}
     * encargado de validar el token JWT en cada petición.
     *
     * @param http Objeto {@link ServerHttpSecurity} para configurar la seguridad web reactiva.
     * @return {@link SecurityWebFilterChain} con la configuración de seguridad aplicada.
     */
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http
                .csrf(csrf -> csrf.disable())
                .httpBasic(httpBasic -> httpBasic.disable())
                .formLogin(form -> form.disable())
                .authorizeExchange(auth -> auth
                        .pathMatchers("/api/login").permitAll()
                        .pathMatchers("/swagger-ui/**", "/v3/api-docs/**", "/webjars/**").permitAll()
                        .pathMatchers(HttpMethod.POST, "/api/users/create").hasAnyRole("ADMIN", "ASESOR")
                        .pathMatchers(HttpMethod.GET, "/api/users/{identification}").hasRole("CLIENTE")
                        .pathMatchers(HttpMethod.GET, "/api/users/validate-role/{identification}").hasRole("ASESOR")
                        .pathMatchers("/api/users/**").authenticated()
                        .anyExchange().permitAll()
                )
                .addFilterAt(new JwtAuthenticationFilter(tokenProvider), SecurityWebFiltersOrder.AUTHENTICATION);

        return http.build();
    }
}

