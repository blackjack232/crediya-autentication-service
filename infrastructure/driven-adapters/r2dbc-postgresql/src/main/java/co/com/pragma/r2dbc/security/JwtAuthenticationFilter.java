package co.com.pragma.r2dbc.security;

import co.com.pragma.model.auth.gateways.TokenProvider;
import co.com.pragma.model.user.constants.UserRole;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Filtro personalizado que intercepta todas las solicitudes entrantes
 * para validar el token JWT presente en el encabezado de autorización.
 */
@Component
public class JwtAuthenticationFilter implements WebFilter {

    private final TokenProvider tokenProvider;

    public JwtAuthenticationFilter(TokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    /**
     * Aplica el filtro a cada petición, validando el token JWT y
     * agregando la autenticación al contexto de seguridad reactivo.
     *
     * @param exchange intercambio de la petición y respuesta
     * @param chain    cadena de filtros a ejecutar
     * @return Mono<Void> indicando la continuación o bloqueo de la petición
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            try {
                // Si el token es inválido → se retorna UNAUTHORIZED
                if (!tokenProvider.validateToken(token)) {
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return exchange.getResponse().setComplete();
                }

                // Extraer el usuario (subject) del token
                String subject = tokenProvider.getSubjectFromToken(token);

                // Extraer y mapear roles del token
                List<SimpleGrantedAuthority> roles = tokenProvider.extractRole(token).stream()
                        .map(this::mapRole)
                        .toList();

                // Autenticación del usuario con sus roles
                Authentication auth = new UsernamePasswordAuthenticationToken(subject, null, roles);

                return chain.filter(exchange)
                        .contextWrite(ReactiveSecurityContextHolder.withAuthentication(auth));

            } catch (Exception e) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }
        }

        return chain.filter(exchange);
    }

    /**
     * Convierte el id de rol obtenido del token en un {@link SimpleGrantedAuthority}.
     *
     * @param idRole identificador del rol (ejemplo: "1", "2", "3")
     * @return rol en formato SimpleGrantedAuthority
     */
    private SimpleGrantedAuthority mapRole(String idRole) {
        return switch (idRole) {
            case "1" -> new SimpleGrantedAuthority(UserRole.ROLE_ADMIN.getValue());
            case "2" -> new SimpleGrantedAuthority(UserRole.ROLE_CLIENTE.getValue());
            case "3" -> new SimpleGrantedAuthority(UserRole.ROLE_ASESOR.getValue());
            default -> new SimpleGrantedAuthority("ROLE_UNKNOWN");
        };
    }
}
