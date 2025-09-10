package co.com.pragma.model.auth.gateways;

import co.com.pragma.model.user.User;



import java.util.List;


public interface TokenProvider {
    /**
     * Genera un token JWT para el usuario especificado.
     * @param user usuario para incluir en el token
     * @return el token JWT
     */
    String generateToken(User user);

    /**
     * Valida el token JWT.
     * @param token token a validar
     * @return true si es válido, false en caso contrario
     */
    boolean validateToken(String token);

    /**
     * Extrae el email (o el subject) del token.
     * @param token token JWT
     * @return email o identificador
     */
    String getSubjectFromToken(String token);

    List<String> extractRole(String token);
}
