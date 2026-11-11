package dev.bootcamp.authservice.infrastructure.persistence;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Usuario tal como se guarda en la colección {@code user_accounts}. Los índices únicos impiden
 * dos usuarios con el mismo nombre o para el mismo cliente aunque lleguen dos registros a la vez.
 */
@Getter
@AllArgsConstructor
@Document(collection = "user_accounts")
public class UserAccountDocument {
    @Id private final String id;
    @Indexed(unique = true) private final String customerId;
    @Indexed(unique = true) private final String username;

    /**
     * Contraseña cifrada con BCrypt.
     */
    private final String passwordHash;
}
