package dev.bootcamp.authservice.infrastructure.persistence;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Copia de un cliente tal como se guarda en la colección {@code customers}; su id es el del
 * cliente en customer-service.
 */
@Getter
@AllArgsConstructor
@Document(collection = "customers")
public class CustomerDocument {
    @Id private final String id;

    /**
     * PERSONAL o BUSINESS.
     */
    private final String type;

    /**
     * STANDARD, VIP o PYME.
     */
    private final String profile;
}
