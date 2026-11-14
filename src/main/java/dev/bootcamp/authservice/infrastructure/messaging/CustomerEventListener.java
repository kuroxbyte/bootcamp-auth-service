package dev.bootcamp.authservice.infrastructure.messaging;

import dev.bootcamp.authservice.application.usecase.CloseAccessUseCase;
import dev.bootcamp.authservice.application.usecase.SaveCustomerUseCase;
import dev.bootcamp.authservice.domain.model.customer.Customer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Consume el tópico {@code customer-events} de customer-service: guarda en la copia local cada
 * cliente creado o modificado y quita el acceso de los clientes dados de baja.
 * Su contrato está en {@code docs/events/asyncapi.yaml} del repositorio raíz.
 *
 * <p>Ignora los tipos de evento que no conoce. Si guardar falla, la excepción hace que Kafka
 * vuelva a entregar el mensaje.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CustomerEventListener {

    static final String CUSTOMER_CREATED = "CustomerCreated";
    static final String CUSTOMER_UPDATED = "CustomerUpdated";
    static final String CUSTOMER_CLOSED = "CustomerClosed";

    private final SaveCustomerUseCase saveCustomerUseCase;
    private final CloseAccessUseCase closeAccessUseCase;
    private final JsonMapper jsonMapper;

    /**
     * Procesa un evento del tópico de clientes.
     *
     * @param message evento en JSON
     */
    @KafkaListener(topics = "${messaging.topics.customer-events}")
    public void onCustomerEvent(String message) {
        JsonNode event = jsonMapper.readTree(message);
        String eventType = event.path("eventType").asString();
        String customerId = event.path("customerId").asString();
        switch (eventType) {
            case CUSTOMER_CREATED, CUSTOMER_UPDATED -> saveCustomerUseCase.execute(new Customer(
                    customerId,
                    event.get("type").asString(),
                    event.get("profile").asString())).blockingAwait();
            case CUSTOMER_CLOSED -> closeAccessUseCase.closeCustomer(customerId).blockingAwait();
            default -> {
                log.debug("Ignoring customer event type={}", eventType);
                return;
            }
        }
        log.info("{} processed customerId={}", eventType, customerId);
    }
}
