package dev.bootcamp.authservice.infrastructure.security;

import dev.bootcamp.authservice.domain.model.user.AccessToken;
import dev.bootcamp.authservice.domain.model.user.UserAccount;
import dev.bootcamp.authservice.infrastructure.config.JwtConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.time.Duration;
import java.util.Base64;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = {JwtConfig.class, JwtTokenIssuer.class})
class JwtTokenIssuerTest {

    @Autowired private JwtTokenIssuer tokenIssuer;

    @Value("${jwt.public-key}")
    private String publicKey;

    @Test
    void issuesSignedTokenWithCustomerAsSubject() throws Exception {
        AccessToken token = tokenIssuer.issue(new UserAccount("u1", "c1", "ana", "hash"));

        RSAPublicKey key = (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(
                new X509EncodedKeySpec(Base64.getDecoder().decode(publicKey)));
        Jwt jwt = NimbusJwtDecoder.withPublicKey(key).build().decode(token.value());

        assertThat(token.expiresIn()).isEqualTo(3600);
        assertThat(jwt.getSubject()).isEqualTo("c1");
        assertThat(jwt.getClaimAsString("iss")).isEqualTo("bank-auth-service");
        assertThat(jwt.getClaimAsString("preferred_username")).isEqualTo("ana");
        assertThat(jwt.getClaimAsStringList("roles")).isEqualTo(List.of("CUSTOMER"));
        assertThat(Duration.between(jwt.getIssuedAt(), jwt.getExpiresAt()))
                .isEqualTo(Duration.ofHours(1));
        assertThat(jwt.getHeaders()).containsEntry("alg", "RS256");
    }
}
