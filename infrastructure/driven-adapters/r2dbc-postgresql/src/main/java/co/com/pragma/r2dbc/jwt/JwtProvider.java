package co.com.pragma.r2dbc.jwt;

import co.com.pragma.model.auth.gateways.TokenProvider;
import co.com.pragma.model.user.User;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.List;

/**
 * Implementación de {@link TokenProvider} basada en JWT con RSA (RS256).
 *
 * - Firma tokens con clave privada en formato PKCS#8.
 * - Valida tokens con clave pública.
 * - Permite extraer información como el subject (email) y roles.
 */
@Component
public class JwtProvider implements TokenProvider {

    private final PrivateKey privateKey;
    private final PublicKey publicKey;

    /** Tiempo de expiración del token en milisegundos (1 hora). */
    private final long EXPIRATION_TIME = 3600000;

    /**
     * Constructor que carga las llaves pública y privada desde el classpath.
     *
     * @throws Exception si ocurre un error cargando las llaves.
     */
    public JwtProvider() throws Exception {
        this.privateKey = loadPrivateKey("keys/private_key_pkcs8.pem");
        this.publicKey = loadPublicKey("keys/public_key.pem");
    }

    /**
     * Genera un token JWT para el usuario autenticado.
     *
     * @param user usuario autenticado.
     * @return token JWT firmado con RS256.
     */
    @Override
    public String generateToken(User user) {
        return Jwts.builder()
                .setSubject(user.getEmail()) // email será el "subject"
                .claim("role", String.valueOf(user.getIdRole())) // rol convertido a String
                .setIssuedAt(new Date()) // fecha de emisión
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME)) // expiración
                .signWith(privateKey, SignatureAlgorithm.RS256) // firmado con la clave privada
                .compact();
    }

    /**
     * Valida un token JWT asegurando que esté firmado correctamente
     * y no haya expirado.
     *
     * @param token token JWT a validar.
     * @return true si el token es válido, false en caso contrario.
     */
    @Override
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(publicKey)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    /**
     * Obtiene el "subject" del token, que corresponde al email del usuario.
     *
     * @param token token JWT.
     * @return email del usuario contenido en el token.
     */
    @Override
    public String getSubjectFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(publicKey)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    /**
     * Extrae los roles del usuario desde el token JWT.
     *
     * @param token token JWT.
     * @return lista con los roles del usuario.
     */
    @Override
    public List<String> extractRole(String token) {
        String role = Jwts.parserBuilder()
                .setSigningKey(publicKey)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("role", String.class);
        return List.of(role); // se devuelve como lista para mayor flexibilidad
    }

    /**
     * Carga la clave privada desde un archivo PEM en formato PKCS#8.
     *
     * @param path ruta del archivo PEM en resources.
     * @return clave privada RSA.
     * @throws Exception si ocurre un error al procesar la clave.
     */
    private PrivateKey loadPrivateKey(String path) throws Exception {
        try (InputStream inputStream = new ClassPathResource(path).getInputStream()) {
            byte[] keyBytes = cleanPem(inputStream.readAllBytes(),
                    "-----BEGIN PRIVATE KEY-----",
                    "-----END PRIVATE KEY-----");
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
            return KeyFactory.getInstance("RSA").generatePrivate(spec);
        }
    }

    /**
     * Carga la clave pública desde un archivo PEM.
     *
     * @param path ruta del archivo PEM en resources.
     * @return clave pública RSA.
     * @throws Exception si ocurre un error al procesar la clave.
     */
    private PublicKey loadPublicKey(String path) throws Exception {
        try (InputStream inputStream = new ClassPathResource(path).getInputStream()) {
            byte[] keyBytes = cleanPem(inputStream.readAllBytes(),
                    "-----BEGIN PUBLIC KEY-----",
                    "-----END PUBLIC KEY-----");
            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
            return KeyFactory.getInstance("RSA").generatePublic(spec);
        }
    }

    /**
     * Limpia el contenido de un archivo PEM eliminando las cabeceras,
     * pies de archivo y espacios en blanco.
     *
     * @param pemBytes contenido del archivo PEM.
     * @param beginMarker marcador de inicio (ej: "-----BEGIN PUBLIC KEY-----").
     * @param endMarker marcador de fin (ej: "-----END PUBLIC KEY-----").
     * @return arreglo de bytes con la clave decodificada en Base64.
     */
    private byte[] cleanPem(byte[] pemBytes, String beginMarker, String endMarker) {
        String pem = new String(pemBytes, StandardCharsets.UTF_8);
        pem = pem.replace(beginMarker, "")
                .replace(endMarker, "")
                .replaceAll("\\s", ""); // quitar saltos de línea y espacios
        return Base64.getDecoder().decode(pem);
    }
}
