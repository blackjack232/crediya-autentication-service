package co.com.pragma.model.user.constants;


/**
 * Enumeración que define los roles de usuario dentro del sistema.
 * Cada rol está representado con el prefijo estándar "ROLE_".
 */
public enum UserRole {
    ROLE_ADMIN("ROLE_ADMIN"),
    ROLE_ASESOR("ROLE_ASESOR"),
    ROLE_CLIENTE("ROLE_CLIENTE");

    private final String value;

    UserRole(String value) {
        this.value = value;
    }

    /**
     * Obtiene el valor en formato String del rol.
     *
     * @return nombre del rol (ejemplo: "ROLE_ADMIN").
     */
    public String getValue() {
        return value;
    }
}
