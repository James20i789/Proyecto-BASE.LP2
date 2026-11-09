package pe.edu.upeu.lp2_gp2.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${security.jwt.secret-key}")
    private String secretKey;

    @Value("${security.jwt.expiration-time}")
    private Long jwtExpiration;

    // DECLARACIÓN PÚBLICA
    public <T> T extractClaim(String token, Function<Claims,T> claimsResolver){
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    // EXTRAER EL USUARIO - NOMBRE | Clase PÚBLICA
    public String extractUsername (String token){
        return extractClaim(token, Claims::getSubject);
    }
    // GENERAR TOKEN 01
    public String generateToken(UserDetails userDetails){
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role",userDetails.getAuthorities().iterator().next().getAuthority());
        return generateToken(extraClaims,userDetails);
    }
    // GENERAR TOKEN 02
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails){
        return buildToken(extraClaims, userDetails, jwtExpiration);
    }
    // CONSTRUIR EL TOKEN
    private String buildToken(Map<String, Object> extraClaims, UserDetails userDetails, long expiration){
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis()+expiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256) // FIRMA CON LA LLAVE DEL ALGORITMO;
                .compact();
    }

    // TOKEN VALIDADO
    private boolean isTokenValid(String token, UserDetails userDetails){
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }
    // TOKEN EXPIRADO
    private boolean isTokenExpired (String token){
        return extractExpiration(token).before(new Date());
    }
    // EXTRAER EXPIRACIÓN
    private Date extractExpiration(String token){
        return extractClaim(token, Claims::getExpiration);
    }
    // EXTRACCIÓN DE LOS TOKENS
    private Claims extractAllClaims(String token){
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSignInKey(){
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
