package com.grupo3.tienda_ropa.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${security.jwt.secret-key:}")
    private String secretKey;

    // Expiración por defecto: 24 horas (86400000 milisegundos)
    @Value("${security.jwt.expiration-time:86400000}")
    private long jwtExpiration;

    private SecretKey signInKey;

    @jakarta.annotation.PostConstruct
    public void init() {
        if (secretKey == null || secretKey.trim().isEmpty()) {
            // Genera una clave aleatoria segura para esta sesión si no se provee una en el entorno
            this.signInKey = Jwts.SIG.HS256.key().build();
        } else {
            byte[] keyBytes = Decoders.BASE64.decode(secretKey);
            this.signInKey = Keys.hmacShaKeyFor(keyBytes);
        }
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String generateToken(UserDetails userDetails) {
        Map<String, Object> extraClaims = new HashMap<>();
        // Extraemos los roles de las autoridades y los guardamos en el token
        String rol = userDetails.getAuthorities().stream()
                .findFirst()
                .map(auth -> auth.getAuthority().replace("ROLE_", ""))
                .orElse("CLIENTE");
        extraClaims.put("rol", rol);
        return generateToken(extraClaims, userDetails);
    }

    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return Jwts.builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSignInKey())
                .compact();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSignInKey() {
        return this.signInKey;
    }
}
