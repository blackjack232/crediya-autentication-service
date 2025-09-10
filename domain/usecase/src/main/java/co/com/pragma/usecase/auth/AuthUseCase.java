package co.com.pragma.usecase.auth;

import co.com.pragma.model.auth.Auth;
import co.com.pragma.model.auth.gateways.TokenProvider;
import co.com.pragma.model.user.User;
import co.com.pragma.model.user.gateways.UserRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.regex.Pattern;

@RequiredArgsConstructor
public class AuthUseCase {
    private final UserRepository userRepository;
    private final TokenProvider tokenProvider;

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    public Mono<String> login(Auth auth) {
        if (!EMAIL_PATTERN.matcher(auth.getEmail()).matches()) {
            return Mono.error(new RuntimeException("Formato de correo invalido"));
        }

        return userRepository.findByEmail(auth.getEmail())
                .flatMap(user -> {
                    if (user.getPassword().equals(auth.getPassword())) {
                        return Mono.just(tokenProvider.generateToken(user));
                    } else {
                        return Mono.error(new RuntimeException("Credenciales invalidas"));
                    }
                })
                .switchIfEmpty(Mono.error(new RuntimeException("Usuario no encontrado")));
    }
}


