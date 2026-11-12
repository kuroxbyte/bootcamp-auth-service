package dev.bootcamp.authservice.infrastructure.messaging;

import dev.bootcamp.authservice.application.usecase.SaveCustomerUseCase;
import dev.bootcamp.authservice.domain.model.customer.Customer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Consume el tópico {@code customer-events} de customer-service y guarda cada cliente creado en
 * la copia local. Su contrato está en {@code docs/events/asyncapi.yaml} del repositorio raíz.
 *
 * <p>Ignora los tipos de evento que no conoce. Si guardar falla, la excepción hace que Kafka
 * vuelva a entregar el mensaje.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CustomerEventListener {

    static final String CUSTOMER_CREATED = "CustomerCreated";

    private final SaveCustomerUseCase saveCustomerUseCase;
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
        if (!CUSTOMER_CREATED.equals(eventType)) {
            log.debug("Ignoring customer event type={}", eventType);
            return;
        }
        Customer customer = new Customer(
                event.get("customerId").asString(),
                event.get("type").asString(),
                event.get("profile").asString());
        saveCustomerUseCase.execute(customer).blockingAwait();
        log.info("Customer known from CustomerCreated customerId={}", customer.id());
    }
}
