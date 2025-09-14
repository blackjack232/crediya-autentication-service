package co.com.pragma.r2dbc;

import co.com.pragma.model.user.User;
import co.com.pragma.model.user.gateways.UserRepository;
import co.com.pragma.r2dbc.contants.UserSqlConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@Slf4j
@Repository
public class UserRepositoryAdapter implements UserRepository {

    private final DatabaseClient client;

    public UserRepositoryAdapter(DatabaseClient client) {
        this.client = client;
    }

    /**
     * Guarda un nuevo usuario en la base de datos.
     * <p>
     * Inserta un registro en la tabla <code>auth.users</code> y retorna el objeto
     * {@link User} con el <code>id_user</code> generado.
     *
     * @param user Objeto {@link User} a insertar.
     * @return {@link Mono} que emite el usuario guardado con su ID asignado.
     */
    @Override
    @Transactional
    public Mono<User> save(User user) {
        log.warn(UserSqlConstants.LOG_INSERT_USER, user.toString());

        return client.sql(UserSqlConstants.INSERT_USER)
                .bind("firstName", user.getFirstName())
                .bind("lastName", user.getLastName())
                .bind("email", user.getEmail())
                .bind("identityDocument", user.getIdentityDocument())
                .bind("phone", user.getPhone())
                .bind("baseSalary", user.getBaseSalary())
                .bind("idRole", user.getIdRole())
                .map(row -> user.toBuilder()
                        .idUser(row.get("id_user", Long.class))
                        .build()
                )
                .one()
                .doOnSuccess(u -> log.info(UserSqlConstants.LOG_USER_INSERTED, u.getIdUser()))
                .doOnError(e -> log.error(UserSqlConstants.LOG_INSERT_ERROR, e.getMessage()));
    }

    /**
     * Verifica si existe un usuario en la base de datos con el email dado.
     *
     * @param email Dirección de correo electrónico a verificar.
     * @return {@link Mono} que emite <code>true</code> si el usuario existe, de lo contrario <code>false</code>.
     */
    @Override
    public Mono<Boolean> existsByEmail(String email) {
        return client.sql(UserSqlConstants.EXISTS_BY_EMAIL)
                .bind("email", email)
                .map(row -> row.get("cnt", Long.class) > 0)
                .one();
    }

    /**
     * Verifica si existe un usuario en la base de datos con el documento de identidad dado.
     *
     * @param identityDocument Documento de identidad a verificar.
     * @return {@link Mono} que emite <code>true</code> si el usuario existe, de lo contrario <code>false</code>.
     */
    @Override
    public Mono<Boolean> existsByIdentityDocument(String identityDocument) {
        return client.sql(UserSqlConstants.EXISTS_BY_DOCUMENT)
                .bind("identityDocument", identityDocument)
                .map(row -> row.get("cnt", Long.class) > 0)
                .one()
                .doOnSuccess(exists -> {
                    if (Boolean.TRUE.equals(exists)) {
                        log.info(UserSqlConstants.LOG_DOC_FOUND, identityDocument);
                    } else {
                        log.warn(UserSqlConstants.LOG_DOC_NOT_FOUND, identityDocument);
                    }
                })
                .doOnError(e -> log.error(UserSqlConstants.LOG_DOC_ERROR, e.getMessage()));
    }

    /**
     * Verifica si existe un rol en la base de datos con el identificador dado.
     *
     * @param id Identificador único del rol.
     * @return {@link Mono} que emite <code>true</code> si el rol existe, de lo contrario <code>false</code>.
     */
    @Override
    public Mono<Boolean> existsByRol(Long id) {
        return client.sql(UserSqlConstants.EXISTS_BY_ROLE)
                .bind("id", id)
                .map(row -> {
                    Number count = row.get("cnt", Number.class);
                    return count != null && count.longValue() > 0;
                })
                .one();
    }

    /**
     * Busca un usuario en la base de datos utilizando su dirección de correo electrónico.
     * <p>
     * Si el usuario existe, retorna el objeto {@link User} completo; de lo contrario, retorna vacío.
     *
     * @param email Dirección de correo electrónico del usuario.
     * @return {@link Mono} que emite el {@link User} encontrado o vacío si no existe.
     */
    @Override
    public Mono<User> findByEmail(String email) {
        log.info(UserSqlConstants.LOG_SEARCH_USER, email);

        return client.sql(UserSqlConstants.FIND_BY_EMAIL)
                .bind("email", email)
                .map(row -> User.builder()
                        .idUser(row.get("id_user", Long.class))
                        .firstName(row.get("first_name", String.class))
                        .lastName(row.get("last_name", String.class))
                        .email(row.get("email", String.class))
                        .identityDocument(row.get("identity_document", String.class))
                        .phone(row.get("phone", String.class))
                        .baseSalary(row.get("base_salary", BigDecimal.class))
                        .idRole(row.get("id_role", Long.class))
                        .password(row.get("password", String.class))
                        .build()
                )
                .one()
                .doOnNext(user -> log.info(UserSqlConstants.LOG_USER_FOUND, user.getFirstName(), user.getLastName()))
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn(UserSqlConstants.LOG_USER_NOT_FOUND, email);
                    return Mono.empty();
                }))
                .doOnError(e -> log.error(UserSqlConstants.LOG_SEARCH_ERROR, email, e.getMessage()));
    }
}

