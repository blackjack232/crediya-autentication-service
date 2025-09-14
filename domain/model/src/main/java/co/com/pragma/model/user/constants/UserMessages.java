package co.com.pragma.model.user.constants;

public class UserMessages {

    private UserMessages() {

    }

    public static final String FIRST_NAME_REQUIRED = "El nombre es requerido";
    public static final String LAST_NAME_REQUIRED = "El apellido es requerido";
    public static final String INVALID_EMAIL = "Formato de email invalido";
    public static final String BASE_SALARY_REQUIRED = "Salario fuera de rango (0 - 15.000.000)";
    public static final String EMAIL_ALREADY_EXISTS = "El email ya esta registrado";
    public static final String DOCUMENT_ALREADY_EXISTS = "El documento ya esta registrado";
    public static final String USUARIO_CREADO = "Usuario creado exitosamente";
    public static final String ROLE_NOT_FOUND = "El rol especificado no existe";
    public static final String USER_VERIFY =  "Usuario verificado";
    public static final String USER_NOT_AUTHENTICATE=  "No autenticado: token no válido o ausente";
    // Generales

    public static final String USER_CREATED = "Usuario creado correctamente";
    public static final String USER_NOT_FOUND = "Usuario no encontrado";
    public static final String USER_VALIDATED = "Usuario encontrado, rol validado";

    // Seguridad

    public static final String USER_NO_SECURITY_CONTEXT = "No hay contexto de seguridad (token ausente)";
    public static final String USER_ACCESS_DENIED_ASESOR = "Acceso denegado: no tienes el rol ASESOR";

    // 🔐 Autenticación
    public static final String LOGIN_SUCCESS = "Login exitoso";
    public static final String LOGIN_FAILED = "Credenciales inválidas o error en la autenticación";


    // Mensajes de error genérico
    public static final String ERROR_GENERAL = "Ocurrió un error inesperado";

    // Mensajes de validación / error
    public static final String ERROR_INVALID_EMAIL = "Formato de correo invalido";
    public static final String ERROR_INVALID_CREDENTIALS = "Credenciales invalidas";
    public static final String ERROR_USER_NOT_FOUND = "Usuario no encontrado";
}
