package dev.bootcamp.authservice.infrastructure.security;

import dev.bootcamp.authservice.application.port.TokenIssuer;
import dev.bootcamp.authservice.domain.model.user.AccessToken;
import dev.bootcamp.authservice.domain.model.user.Role;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * Implementa el puerto {@link TokenIssuer} con JWT firmados con RS256.
 *
 * <p>Claims: {@code iss}, {@code sub} (id del cliente o del monedero), {@code preferred_username}
 * (nombre de usuario o celular), {@code roles} ({@code CUSTOMER} o {@code WALLET}), {@code iat} y
 * {@code exp}.
 */
@Component
public class JwtTokenIssuer implements TokenIssuer {

    private final JwtEncoder jwtEncoder;
    private final Clock clock;
    private final String issuer;
    private final Duration expiration;

    /**
     * Crea el emisor con el emisor y la duración configurados.
     */
    public JwtTokenIssuer(
            JwtEncoder jwtEncoder,
            Clock clock,
            @Value("${jwt.issuer}") String issuer,
            @Value("${jwt.expiration}") Duration expiration) {
        this.jwtEncoder = jwtEncoder;
        this.clock = clock;
        this.issuer = issuer;
        this.expiration = expiration;
    }

    @Override
    public AccessToken issue(String subject, String username, Role role) {
        Instant now = Instant.now(clock);
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .subject(subject)
                .issuedAt(now)
                .expiresAt(now.plus(expiration))
                .claim("preferred_username", username)
                .claim("roles", List.of(role.name()))
                .build();
        JwsHeader header = JwsHeader.with(SignatureAlgorithm.RS256).build();
        String token = jwtEncoder
                .encode(JwtEncoderParameters.from(header, claims))
                .getTokenValue();
        return new AccessToken(token, expiration.toSeconds());
    }
}
