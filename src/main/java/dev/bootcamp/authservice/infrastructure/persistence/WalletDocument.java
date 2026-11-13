package dev.bootcamp.authservice.infrastructure.persistence;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Copia de un monedero Yanki tal como se guarda en la colección {@code wallets}; su id es el de
 * yanki-service.
 */
@Getter
@AllArgsConstructor
@Document(collection = "wallets")
public class WalletDocument {
    @Id private final String id;
    @Indexed(unique = true) private final String phone;
}
