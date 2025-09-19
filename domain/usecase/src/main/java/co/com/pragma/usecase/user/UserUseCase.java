package co.com.pragma.usecase.user;

import co.com.pragma.model.user.User;
import co.com.pragma.model.user.constants.UserConstants;
import co.com.pragma.model.user.constants.UserMessages;
import co.com.pragma.model.user.gateways.UserRepository;
import lombok.RequiredArgsConstructor;


import reactor.core.publisher.Mono;

/**
 * Caso de uso para la gestión de usuarios en el sistema.
 * Contiene la lógica de negocio relacionada con validaciones
 * y operaciones de persistencia.
 */
@RequiredArgsConstructor
public class UserUseCase {

    private final UserRepository userRepository;


    /**
     * Crea un nuevo usuario validando:
     * <ul>
     *   <li>Nombre obligatorio (no nulo ni vacío).</li>
     *   <li>Apellido obligatorio (no nulo ni vacío).</li>
     *   <li>Email válido con formato correcto.</li>
     *   <li>Salario base dentro del rango permitido.</li>
     *   <li>Rol existente en el sistema.</li>
     *   <li>Email no repetido.</li>
     *   <li>Documento de identidad no duplicado.</li>
     * </ul>
     *
     * @param user objeto {@link User} con la información del usuario.
     * @return Mono con el usuario creado o un error con el mensaje correspondiente.
     */
    public Mono<User> saveUser(User user) {
        try {
            validateBasicFields(user);
        } catch (IllegalArgumentException ex) {
            return Mono.error(ex);
        }

        return validateRole(user.getIdRole())
                .then(validateEmail(user.getEmail()))
                .then(validateIdentityDocument(user.getIdentityDocument()))
                .then(userRepository.save(user));
    }

    // 🔹 Validaciones locales (síncronas)
    private void validateBasicFields(User user) {
        if (user.getFirstName() == null || user.getFirstName().isBlank()) {
            throw new IllegalArgumentException(UserMessages.FIRST_NAME_REQUIRED);
        }
        if (user.getLastName() == null || user.getLastName().isBlank()) {
            throw new IllegalArgumentException(UserMessages.LAST_NAME_REQUIRED);
        }
        if (user.getEmail() == null ||
                !user.getEmail().matches(UserConstants.EMAIL_REGEX)) {
            throw new IllegalArgumentException(UserMessages.INVALID_EMAIL);
        }
        if (user.getBaseSalary() == null ||
                user.getBaseSalary().compareTo(UserConstants.MIN_SALARY) < 0 ||
                user.getBaseSalary().compareTo(UserConstants.MAX_SALARY) > 0) {
            throw new IllegalArgumentException(UserMessages.BASE_SALARY_REQUIRED);
        }
    }

    // 🔹 Validaciones reactivas (negocio)
    private Mono<Void> validateRole(Long idRole) {
        return userRepository.existsByRol(idRole)
                .flatMap(roleExists -> roleExists
                        ? Mono.empty()
                        : Mono.error(new IllegalArgumentException(UserMessages.ROLE_NOT_FOUND)));
    }

    private Mono<Void> validateEmail(String email) {
        return userRepository.existsByEmail(email)
                .flatMap(emailExists -> emailExists
                        ? Mono.error(new IllegalArgumentException(UserMessages.EMAIL_ALREADY_EXISTS))
                        : Mono.empty());
    }

    private Mono<Void> validateIdentityDocument(String identityDocument) {
        return userRepository.existsByIdentityDocument(identityDocument)
                .flatMap(documentExists -> documentExists
                        ? Mono.error(new IllegalArgumentException(UserMessages.DOCUMENT_ALREADY_EXISTS))
                        : Mono.empty());
    }

    /**
     * Verifica si un usuario existe dado su número de identificación.
     *
     * @param identityDocument número de documento del usuario.
     * @return {@link Mono} que emite {@code true} si el usuario existe,
     * o {@code false} si no existe.
     */
    public Mono<Boolean>
    existsUserByIdentification(String identityDocument) {
        return userRepository.existsByIdentityDocument(identityDocument);
    }
}
