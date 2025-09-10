package co.com.pragma.r2dbc.security;

import co.com.pragma.model.auth.gateways.TokenProvider;
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

@Component
public class JwtAuthenticationFilter implements WebFilter {

    private final TokenProvider tokenProvider;

    public JwtAuthenticationFilter(TokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            try {
                if (!tokenProvider.validateToken(token)) {
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return exchange.getResponse().setComplete();
                }

                String subject = tokenProvider.getSubjectFromToken(token);

                // Mapear idRole a ROLE_* automáticamente
                List<SimpleGrantedAuthority> roles = tokenProvider.extractRole(token).stream()
                        .map(this::mapRole)
                        .toList();

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

    private SimpleGrantedAuthority mapRole(String idRole) {
        return switch (idRole) {
            case "2" -> new SimpleGrantedAuthority("ROLE_ADMIN");
            case "3" -> new SimpleGrantedAuthority("ROLE_CLIENTE");
            case "4" -> new SimpleGrantedAuthority("ROLE_ASESOR");
            default -> new SimpleGrantedAuthority("ROLE_UNKNOWN");
        };
    }

}
