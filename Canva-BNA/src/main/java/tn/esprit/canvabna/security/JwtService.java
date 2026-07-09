package tn.esprit.canvabna.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import tn.esprit.canvabna.entity.User;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Service responsible for all JWT operations:
 * <ul>
 *   <li>Token generation with custom claims</li>
 *   <li>Token validation</li>
 *   <li>Claims extraction</li>
 *   <li>Expiration handling</li>
 * </ul>
 *
 * <p>Uses JJWT 0.12.x API — no deprecated methods.
 * Secret key is Base64-encoded and configured via {@code application.properties}.
 */
@Slf4j
@Service
public class JwtService {

    @Value("${app.jwt.secret}")
    private String secretKey;

    @Value("${app.jwt.expiration-ms}")
    private long expirationMs;

    // ---------------------------------------------------------------- Public API

    /**
     * Generates a JWT for the given {@link CustomUserDetails}.
     * Extra claims embedded: {@code id}, {@code matricule}, {@code role}, {@code structure}.
     *
     * @param userDetails the authenticated user details
     * @return signed JWT string
     */
    public String generateToken(final CustomUserDetails userDetails) {
        final User user = userDetails.getUser();

        final Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("id", user.getId());
        extraClaims.put("matricule", user.getMatricule());
        extraClaims.put("role", user.getRole().name());
        extraClaims.put("structure", user.getStructure());

        return buildToken(extraClaims, userDetails);
    }

    /**
     * Validates the token against the given {@link UserDetails}.
     *
     * @param token       the JWT string
     * @param userDetails the user to validate against
     * @return {@code true} if the token is valid and belongs to the user
     */
    public boolean isTokenValid(final String token, final UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    /**
     * Extracts the username (matricule) from the token subject.
     *
     * @param token the JWT string
     * @return username / matricule string
     */
    public String extractUsername(final String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // ---------------------------------------------------------------- Private helpers

    private String buildToken(final Map<String, Object> extraClaims, final UserDetails userDetails) {
        final Date now = new Date();
        final Date expiry = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(getSigningKey())
                .compact();
    }

    private boolean isTokenExpired(final String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(final String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private <T> T extractClaim(final String token, final Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(final String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSigningKey() {
        final byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // ---------------------------------------------------------------- Validation helpers (used by filter)

    /**
     * Checks whether the token string can be parsed and verified without throwing.
     * Logs the specific failure reason for diagnostics.
     *
     * @param token the raw JWT string
     * @return {@code true} if the token is structurally valid and the signature verifies
     */
    public boolean isTokenStructurallyValid(final String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("JWT expired: {}", e.getMessage());
        } catch (SecurityException | MalformedJwtException e) {
            log.warn("Invalid JWT signature or structure: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.warn("Unsupported JWT: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }
}
