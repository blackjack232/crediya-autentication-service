package co.com.pragma.usecase.user;

import co.com.pragma.model.user.User;
import co.com.pragma.model.user.constants.UserMessages;
import co.com.pragma.model.user.gateways.UserRepository;
import lombok.RequiredArgsConstructor;


import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@RequiredArgsConstructor
public class UserUseCase {
    private final UserRepository userRepository;
    private static final BigDecimal MIN_SALARY = new BigDecimal("0");
    private static final BigDecimal MAX_SALARY = new BigDecimal("20000000");
    public Mono<User> saveUser(User user) {
        if (user.getFirstName() == null || user.getFirstName().isBlank()) {
            return Mono.error(new IllegalArgumentException(UserMessages.FIRST_NAME_REQUIRED));
        }
        if (user.getLastName() == null || user.getLastName().isBlank()) {
            return Mono.error(new IllegalArgumentException(UserMessages.LAST_NAME_REQUIRED));
        }
        if (user.getEmail() == null || !user.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            return Mono.error(new IllegalArgumentException(UserMessages.INVALID_EMAIL));
        }
        if (user.getBaseSalary() == null || (user.getBaseSalary().compareTo(MIN_SALARY) < 0 ||
                user.getBaseSalary().compareTo(MAX_SALARY) > 0)) {
            return Mono.error(new IllegalArgumentException(UserMessages.BASE_SALARY_REQUIRED));
        }


        return userRepository.existsByRol(user.getIdRole())
                .flatMap(roleExists -> {
                    if (!roleExists) {
                        return Mono.error(new IllegalArgumentException(UserMessages.ROLE_NOT_FOUND));
                    }
                    return userRepository.existsByEmail(user.getEmail());
                })
                .flatMap(emailExists -> {
                    if (emailExists) {
                        return Mono.error(new IllegalArgumentException(UserMessages.EMAIL_ALREADY_EXISTS));
                    }
                    return userRepository.existsByIdentityDocument(user.getIdentityDocument());
                })
                .flatMap(documentExists -> {
                    if (documentExists) {
                        return Mono.error(new IllegalArgumentException(UserMessages.DOCUMENT_ALREADY_EXISTS));
                    }
                    return userRepository.save(user);
                });

    }

    /**
     * Verifica si un usuario existe dado su número de identificación.
     *
     * @param identityDocument Número de documento
     * @return Mono<Boolean> indicando si existe
     */
    public Mono<Boolean> existsUserByIdentification(String identityDocument) {
        return userRepository.existsByIdentityDocument(identityDocument);
    }
}
