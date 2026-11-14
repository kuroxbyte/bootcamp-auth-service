package dev.bootcamp.authservice.infrastructure.messaging;

import dev.bootcamp.authservice.application.usecase.CloseAccessUseCase;
import dev.bootcamp.authservice.application.usecase.SaveCustomerUseCase;
import dev.bootcamp.authservice.domain.model.customer.Customer;
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
class CustomerEventListenerTest {

    @Mock private SaveCustomerUseCase saveCustomerUseCase;

    @Mock private CloseAccessUseCase closeAccessUseCase;

    private CustomerEventListener listener;

    @BeforeEach
    void createListener() {
        listener = new CustomerEventListener(
                saveCustomerUseCase, closeAccessUseCase, JsonMapper.builder().build());
    }

    @Test
    void savesCustomerOfCustomerCreatedEvent() {
        when(saveCustomerUseCase.execute(any())).thenReturn(Completable.complete());

        listener.onCustomerEvent("{\"eventType\":\"CustomerCreated\",\"customerId\":\"c1\","
                + "\"type\":\"BUSINESS\",\"profile\":\"PYME\","
                + "\"occurredAt\":\"2026-11-15T10:00:00Z\"}");

        verify(saveCustomerUseCase).execute(new Customer("c1", "BUSINESS", "PYME"));
    }

    @Test
    void ignoresUnknownEventTypes() {
        listener.onCustomerEvent("{\"eventType\":\"CustomerDeleted\",\"customerId\":\"c1\"}");

        verify(saveCustomerUseCase, never()).execute(any());
    }

    @Test
    void replacesCustomerOfCustomerUpdatedEvent() {
        when(saveCustomerUseCase.execute(any())).thenReturn(Completable.complete());

        listener.onCustomerEvent("{\"eventType\":\"CustomerUpdated\",\"customerId\":\"c1\","
                + "\"type\":\"PERSONAL\",\"profile\":\"VIP\"}");

        verify(saveCustomerUseCase).execute(new Customer("c1", "PERSONAL", "VIP"));
    }

    @Test
    void closesAccessOfCustomerClosedEvent() {
        when(closeAccessUseCase.closeCustomer("c1")).thenReturn(Completable.complete());

        listener.onCustomerEvent("{\"eventType\":\"CustomerClosed\",\"customerId\":\"c1\"}");

        verify(closeAccessUseCase).closeCustomer("c1");
        verify(saveCustomerUseCase, never()).execute(any());
    }
}
