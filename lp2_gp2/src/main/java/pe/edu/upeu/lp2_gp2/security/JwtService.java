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
    private long jwtExpiration;

    // EXTRAER TOKEN - RESOLVER - PUBLIC
    public <T> T extractClaim(String token, Function<Claims,T> claimsResolver){
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    // EXTRAER NOMBRE | CLASE PÚBLICA
    public String extractUsername (String token){
        return extractClaim(token, Claims::getSubject);
    }
    // GENERAR TOKEN | CLASE PÚBLIC (USUARIO-DETALLES) - 01
    public String generateToken(UserDetails userDetails){
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role",userDetails.getAuthorities().iterator().next().getAuthority());
        return generateToken(extraClaims,userDetails);
    }
    // GENERAR TOKEN | CLASE PÚBLIC (USUARIO-DETALLES) - 02
    public String generateToken(Map<String,Object> extraClaims, UserDetails userDetails){
        return buildToken(extraClaims,userDetails,jwtExpiration);
    }
    // CONSTRUIR TOKEN
    private String buildToken(Map<String, Object> extraClaims,UserDetails userDetails, long expiration){
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256) //firma con la llave del algoritmo;
                .compact();
    }
    // CLASE PUBLIC - VALIDAR TOKEN
    public boolean isTokenValid(String token, UserDetails userDetails){
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }
    private boolean isTokenExpired (String token){
        return extractExpiration(token).before(new Date());
    }
    private Date extractExpiration(String token){
        return extractClaim(token, Claims::getExpiration);
    }

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