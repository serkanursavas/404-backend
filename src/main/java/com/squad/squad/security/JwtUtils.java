package com.squad.squad.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.Date;

@Component
public class JwtUtils {
    private static final long JWT_EXPIRATION_MS = 259200000; // Token geçerlilik süresi (1 gün)

    private static final String SECRET_KEY_STRING = System.getenv("JWT_SECRET_KEY");
    private static final SecretKey SECRET_KEY = new SecretKeySpec(Base64.getDecoder().decode(SECRET_KEY_STRING), "HmacSHA256");

    static {
        if (SECRET_KEY_STRING == null || SECRET_KEY_STRING.isEmpty()) {
            throw new IllegalStateException("JWT_SECRET_KEY environment variable is not set!");
        }
    }

    // JWT token oluşturma
    public String generateToken(Integer id, String username, String role) {

        return Jwts.builder()
                .setSubject(username.toLowerCase())  // Kullanıcı adını subject olarak ayarlıyoruz
                .claim("role", role)
                .claim("id", id)       // ID'yi de claim olarak ekliyoruz// Rol bilgisini doğrudan claim olarak ekliyoruz
                .setIssuedAt(new Date())  // Token oluşturulma tarihini ayarlıyoruz
                .setExpiration(new Date(System.currentTimeMillis() + JWT_EXPIRATION_MS))  // Token geçerlilik süresini ayarlıyoruz
                .signWith(SECRET_KEY, SignatureAlgorithm.HS256)  // İmzalama işlemi yapılıyor
                .compact();
    }

    // Token'dan kullanıcı adı çıkarma
    public String extractUsername(String token) {
        return extractClaims(token).getSubject();
    }

    // Token'dan rol çıkarma
    public String extractRole(String token) {
        Claims claims = extractClaims(token);
        return claims.get("role", String.class);  // Token'dan rol bilgisini çıkarıyoruz
    }

    // Token'ın geçerliliğini kontrol etme
    public boolean validateToken(String token, String username) {
        String extractedUsername = extractUsername(token);
        return (extractedUsername.equals(username) && !isTokenExpired(token));
    }

    // Token'dan Claims çıkarma
    private Claims extractClaims(String token) {
        return Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // Token'ın süresinin dolup dolmadığını kontrol etme
    private boolean isTokenExpired(String token) {
        return extractClaims(token).getExpiration().before(new Date());
    }
}