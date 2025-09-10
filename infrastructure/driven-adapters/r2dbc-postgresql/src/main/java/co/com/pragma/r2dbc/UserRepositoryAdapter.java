package co.com.pragma.r2dbc;

import co.com.pragma.model.user.User;
import co.com.pragma.model.user.gateways.UserRepository;
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

    @Override
    @Transactional
    public Mono<User> save(User user) {
        log.warn("Insertando usuario en base de datos: {}", user.toString());

        return client.sql("""
                INSERT INTO auth.users(first_name, last_name, email, identity_document, phone, base_salary, id_role)
                VALUES (:firstName, :lastName, :email, :identityDocument, :phone, :baseSalary, :idRole)
                RETURNING id_user
                """)
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
                .doOnSuccess(u -> log.info("Usuario insertado con id {}", u.getIdUser()))
                .doOnError(e -> log.error("Error al insertar usuario: {}", e.getMessage()));
    }

    @Override
    public Mono<Boolean> existsByEmail(String email) {
        return client.sql("SELECT COUNT(*) as cnt FROM auth.users WHERE email = :email")
                .bind("email", email)
                .map(row -> row.get("cnt", Long.class) > 0)
                .one();
    }
    @Override
    public Mono<Boolean> existsByIdentityDocument(String identityDocument) {
        return client.sql("SELECT COUNT(*) as cnt FROM auth.users WHERE identity_document = :identityDocument")
                .bind("identityDocument", identityDocument)
                .map(row -> row.get("cnt", Long.class) > 0)
                .one()
                .doOnSuccess(exists -> {
                    if (Boolean.TRUE.equals(exists)) {
                        log.info("✅ Usuario con documento [{}] encontrado en la base de datos.", identityDocument);
                    } else {
                        log.warn("Usuario con documento [{}] NO existe en la base de datos.", identityDocument);
                    }
                })
                .doOnError(e -> log.error(" Error al verificar usuario: {}", e.getMessage()));
    }

    @Override
    public Mono<Boolean> existsByRol(Long id) {
        return client.sql("SELECT COUNT(*) AS cnt FROM auth.role WHERE uniqueid = :id")
                .bind("id", id)
                .map(row -> {
                    Number count = row.get("cnt", Number.class); // soporte para Integer/Long
                    return count != null && count.longValue() > 0;
                })
                .one();
    }
    @Override
    public Mono<User> findByEmail(String email) {
        log.info("Buscando usuario con email: {}", email);

        return client.sql("""
            SELECT id_user, first_name, last_name, email, identity_document, phone, base_salary, id_role, password
            FROM auth.users 
            WHERE email = :email
            """)
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
                .doOnNext(user -> log.info("Usuario encontrado: {} {}", user.getFirstName(), user.getLastName()))
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn("No se encontró usuario con email: {}", email);
                    return Mono.empty();
                }))
                .doOnError(e -> log.error("Error al buscar usuario con email {}: {}", email, e.getMessage()));
    }

}
