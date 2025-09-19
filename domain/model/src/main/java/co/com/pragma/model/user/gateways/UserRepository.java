package co.com.pragma.model.user.gateways;

import co.com.pragma.model.user.User;
import reactor.core.publisher.Mono;

public interface UserRepository {
    /**
     * Guarda un nuevo usuario en la base de datos.
     *
     * @param user objeto {@link User} que contiene la información del usuario.
     * @return un {@link Mono} con el usuario guardado.
     */
    Mono<User> save(User user);

    /**
     * Verifica si existe un usuario registrado con el email proporcionado.
     *
     * @param email dirección de correo electrónico a verificar.
     * @return un {@link Mono} que emite {@code true} si el email ya está registrado,
     * o {@code false} en caso contrario.
     */
    Mono<Boolean> existsByEmail(String email);

    /**
     * Verifica si existe un usuario registrado con el documento de identidad dado.
     *
     * @param identityDocument número de documento de identidad a validar.
     * @return un {@link Mono} que emite {@code true} si el documento ya existe,
     * o {@code false} en caso contrario.
     */
    Mono<Boolean> existsByIdentityDocument(String identityDocument);

    /**
     * Verifica si existe un rol en el sistema con el identificador proporcionado.
     *
     * @param id identificador único del rol.
     * @return un {@link Mono} que emite {@code true} si el rol existe,
     * o {@code false} si no se encuentra.
     */
    Mono<Boolean> existsByRol(Long id);

    /**
     * Busca un usuario en el sistema a partir de su email.
     * compara los passwaords
     *
     * @param email dirección de correo electrónico del usuario.
     * @return un {@link Mono} que emite el {@link User} encontrado o vacío si no existe.
     */

    Mono<User> findByEmail(String email, String rawPassword);
}
