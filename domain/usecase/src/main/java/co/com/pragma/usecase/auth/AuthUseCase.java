package co.com.pragma.usecase.auth;

import co.com.pragma.model.auth.Auth;
import co.com.pragma.model.auth.gateways.TokenProvider;
import co.com.pragma.model.user.User;
import co.com.pragma.model.user.constants.UserConstants;
import co.com.pragma.model.user.constants.UserMessages;
import co.com.pragma.model.user.gateways.UserRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.regex.Pattern;

import co.com.pragma.model.user.User;
import co.com.pragma.model.user.gateways.UserRepository;
import co.com.pragma.model.user.constants.UserConstants;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.regex.Pattern;

/**
 * Caso de uso que gestiona la autenticación de usuarios.
 * Encargado de validar credenciales, generar tokens JWT y
 * manejar las reglas de negocio relacionadas con el login.
 */
@RequiredArgsConstructor
public class AuthUseCase {

    private final UserRepository userRepository;
    private final TokenProvider tokenProvider;

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile(UserConstants.EMAIL_REGEX);

    /**
     * Realiza el proceso de autenticación del usuario.
     * <p>
     * Pasos:
     * 1. Validar el formato del correo electrónico con una expresión regular.
     * 2. Buscar al usuario en la base de datos por su email.
     * 3. Verificar si la contraseña ingresada coincide con la almacenada.
     * 4. Si las credenciales son válidas, generar y retornar un token JWT.
     * </p>
     *
     * @param auth Objeto con las credenciales (email y password).
     * @return Mono que emite un token JWT si la autenticación es exitosa,
     *         o un error en caso de fallo.
     */
    public Mono<String> login(Auth auth) {
        if (!EMAIL_PATTERN.matcher(auth.getEmail()).matches()) {
            return Mono.error(new RuntimeException(UserMessages.ERROR_INVALID_EMAIL));
        }

        return userRepository.findByEmail(auth.getEmail())
                .flatMap(user -> {
                    if (user.getPassword().equals(auth.getPassword())) {
                        return Mono.just(tokenProvider.generateToken(user));
                    } else {
                        return Mono.error(new RuntimeException(UserMessages.ERROR_INVALID_CREDENTIALS));
                    }
                })
                .switchIfEmpty(Mono.error(new RuntimeException(UserMessages.ERROR_USER_NOT_FOUND)));
    }
}


