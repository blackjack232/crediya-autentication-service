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

@Component
public class JwtProvider implements TokenProvider {

    private final PrivateKey privateKey;
    private final PublicKey publicKey;
    private final long EXPIRATION_TIME = 3600000; // 1 hora

    public JwtProvider() throws Exception {
        this.privateKey = loadPrivateKey("keys/private_key_pkcs8.pem");
        this.publicKey = loadPublicKey("keys/public_key.pem");
    }

    @Override
    public String generateToken(User user) {
        return Jwts.builder()
                .setSubject(user.getEmail())
                .claim("role", String.valueOf(user.getIdRole())) // convertir a String
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(privateKey, SignatureAlgorithm.RS256)
                .compact();
    }


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

    @Override
    public String getSubjectFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(publicKey)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    @Override
    public List<String> extractRole(String token) {
        String role = Jwts.parserBuilder()
                .setSigningKey(publicKey)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("role", String.class);
        return List.of(role); // siempre devolvemos lista
    }


    private PrivateKey loadPrivateKey(String path) throws Exception {
        try (InputStream inputStream = new ClassPathResource(path).getInputStream()) {
            byte[] keyBytes = cleanPem(inputStream.readAllBytes(),
                    "-----BEGIN PRIVATE KEY-----",   // 👈 en lugar de RSA PRIVATE KEY
                    "-----END PRIVATE KEY-----");
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
            return KeyFactory.getInstance("RSA").generatePrivate(spec);
        }
    }

    private PublicKey loadPublicKey(String path) throws Exception {
        try (InputStream inputStream = new ClassPathResource(path).getInputStream()) {
            byte[] keyBytes = cleanPem(inputStream.readAllBytes(),
                    "-----BEGIN PUBLIC KEY-----",
                    "-----END PUBLIC KEY-----");
            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
            return KeyFactory.getInstance("RSA").generatePublic(spec);
        }
    }


    private byte[] cleanPem(byte[] pemBytes, String beginMarker, String endMarker) {
        String pem = new String(pemBytes, StandardCharsets.UTF_8);
        pem = pem.replace(beginMarker, "")
                .replace(endMarker, "")
                .replaceAll("\\s", ""); // quitar saltos de línea y espacios
        return Base64.getDecoder().decode(pem);
    }
}
