package co.com.pragma.r2dbc;

import co.com.pragma.model.user.User;
import co.com.pragma.model.user.gateways.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
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
                .one();
    }
}
