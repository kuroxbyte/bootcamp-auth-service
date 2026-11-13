package dev.bootcamp.authservice.infrastructure.messaging;

import dev.bootcamp.authservice.application.usecase.SaveWalletUseCase;
import dev.bootcamp.authservice.domain.model.wallet.Wallet;
import io.reactivex.rxjava3.core.Completable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.json.JsonMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WalletEventListenerTest {

    @Mock private SaveWalletUseCase saveWalletUseCase;

    private WalletEventListener listener;

    @BeforeEach
    void createListener() {
        listener = new WalletEventListener(saveWalletUseCase, JsonMapper.builder().build());
    }

    @Test
    void savesWalletOfWalletCreatedEvent() {
        when(saveWalletUseCase.execute(any())).thenReturn(Completable.complete());

        listener.onWalletEvent("{\"eventType\":\"WalletCreated\",\"walletId\":\"w1\","
                + "\"phone\":\"987654321\",\"occurredAt\":\"2026-11-15T10:00:00Z\"}");

        verify(saveWalletUseCase).execute(new Wallet("w1", "987654321"));
    }

    @Test
    void ignoresUnknownEventTypes() {
        listener.onWalletEvent("{\"eventType\":\"WalletDeleted\",\"walletId\":\"w1\"}");

        verify(saveWalletUseCase, never()).execute(any());
    }
}
