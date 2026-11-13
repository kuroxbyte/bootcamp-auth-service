package dev.bootcamp.authservice.infrastructure.persistence;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Acceso de un monedero tal como se guarda en la colección {@code wallet_users}. Los índices
 * únicos impiden dos accesos para el mismo monedero o celular aunque lleguen dos registros a la
 * vez.
 */
@Getter
@AllArgsConstructor
@Document(collection = "wallet_users")
public class WalletUserDocument {
    @Id private final String id;
    @Indexed(unique = true) private final String walletId;
    @Indexed(unique = true) private final String phone;

    /**
     * Contraseña cifrada con BCrypt.
     */
    private final String passwordHash;
}
