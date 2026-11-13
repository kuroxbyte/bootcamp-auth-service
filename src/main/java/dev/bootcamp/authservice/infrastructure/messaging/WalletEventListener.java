package dev.bootcamp.authservice.infrastructure.messaging;

import dev.bootcamp.authservice.application.usecase.SaveWalletUseCase;
import dev.bootcamp.authservice.domain.model.wallet.Wallet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Consume el tópico {@code yanki-events} de yanki-service y guarda cada monedero creado en la
 * copia local. Su contrato está en {@code docs/events/asyncapi.yaml} del repositorio raíz.
 *
 * <p>Ignora los tipos de evento que no conoce. Si guardar falla, la excepción hace que Kafka
 * vuelva a entregar el mensaje.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WalletEventListener {

    static final String WALLET_CREATED = "WalletCreated";

    private final SaveWalletUseCase saveWalletUseCase;
    private final JsonMapper jsonMapper;

    /**
     * Procesa un evento del tópico de monederos.
     *
     * @param message evento en JSON
     */
    @KafkaListener(topics = "${messaging.topics.yanki-events}")
    public void onWalletEvent(String message) {
        JsonNode event = jsonMapper.readTree(message);
        String eventType = event.path("eventType").asString();
        if (!WALLET_CREATED.equals(eventType)) {
            log.debug("Ignoring wallet event type={}", eventType);
            return;
        }
        Wallet wallet = new Wallet(
                event.get("walletId").asString(),
                event.get("phone").asString());
        saveWalletUseCase.execute(wallet).blockingAwait();
        log.info("Wallet known from WalletCreated walletId={}", wallet.id());
    }
}
