package dev.bootcamp.authservice.infrastructure.config;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Clock;
import java.util.Base64;

/**
 * Claves RSA y firma de los JWT.
 *
 * <p>Las claves llegan en Base64 (DER, sin encabezados PEM) desde config-repo o desde las
 * variables {@code JWT_PRIVATE_KEY} y {@code JWT_PUBLIC_KEY}. El API Gateway valida con la clave
 * pública.
 */
@Configuration
public class JwtConfig {

    /**
     * Codificador de JWT firmados con RS256.
     */
    @Bean
    public JwtEncoder jwtEncoder(
            @Value("${jwt.private-key}") String privateKey,
            @Value("${jwt.public-key}") String publicKey) throws GeneralSecurityException {
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        RSAPrivateKey rsaPrivateKey = (RSAPrivateKey) keyFactory.generatePrivate(
                new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKey)));
        RSAPublicKey rsaPublicKey = (RSAPublicKey) keyFactory.generatePublic(
                new X509EncodedKeySpec(Base64.getDecoder().decode(publicKey)));
        RSAKey rsaKey = new RSAKey.Builder(rsaPublicKey).privateKey(rsaPrivateKey).build();
        return new NimbusJwtEncoder(new ImmutableJWKSet<>(new JWKSet(rsaKey)));
    }

    /**
     * Reloj del sistema; con él se fechan los tokens.
     */
    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
