package co.com.pragma.r2dbc.contants;


public final class UserSqlConstants {

    private UserSqlConstants() {
        throw new IllegalStateException("Utility class");
    }

    // SQL QUERIES
    public static final String INSERT_USER = """
            INSERT INTO auth.users(first_name, last_name, email, identity_document, phone, base_salary, id_role)
            VALUES (:firstName, :lastName, :email, :identityDocument, :phone, :baseSalary, :idRole)
            RETURNING id_user
            """;

    public static final String EXISTS_BY_EMAIL = """
            SELECT COUNT(*) as cnt FROM auth.users WHERE email = :email
            """;

    public static final String EXISTS_BY_DOCUMENT = """
            SELECT COUNT(*) as cnt FROM auth.users WHERE identity_document = :identityDocument
            """;

    public static final String EXISTS_BY_ROLE = """
            SELECT COUNT(*) AS cnt FROM auth.role WHERE uniqueid = :id
            """;

    public static final String FIND_BY_EMAIL = """
            SELECT id_user, first_name, last_name, email, identity_document, phone, base_salary, id_role, password
            FROM auth.users
            WHERE email = :email
            """;

    // LOG MESSAGES
    public static final String LOG_INSERT_USER = "Insertando usuario en base de datos: {}";
    public static final String LOG_USER_INSERTED = "Usuario insertado con id {}";
    public static final String LOG_INSERT_ERROR = "Error al insertar usuario: {}";

    public static final String LOG_DOC_FOUND = " Usuario con documento [{}] encontrado en la base de datos.";
    public static final String LOG_DOC_NOT_FOUND = "Usuario con documento [{}] NO existe en la base de datos.";
    public static final String LOG_DOC_ERROR = "Error al verificar usuario: {}";

    public static final String LOG_SEARCH_USER = "Buscando usuario con email: {}";
    public static final String LOG_USER_FOUND = "Usuario encontrado: {} {}";
    public static final String LOG_USER_NOT_FOUND = "No se encontró usuario con email: {}";
    public static final String LOG_SEARCH_ERROR = "Error al buscar usuario con email {}: {}";
}
