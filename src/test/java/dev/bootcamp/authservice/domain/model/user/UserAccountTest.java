package dev.bootcamp.authservice.domain.model.user;

import dev.bootcamp.authservice.domain.exception.BusinessRuleException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserAccountTest {

    @Test
    void acceptsPasswordBetween8And72Characters() {
        assertThatCode(() -> UserAccount.validatePassword("12345678")).doesNotThrowAnyException();
        assertThatCode(() -> UserAccount.validatePassword("x".repeat(72)))
                .doesNotThrowAnyException();
    }

    @Test
    void rejectsMissingShortOrLongPassword() {
        assertThatThrownBy(() -> UserAccount.validatePassword(null))
                .isInstanceOf(BusinessRuleException.class);
        assertThatThrownBy(() -> UserAccount.validatePassword("1234567"))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("La contraseña debe tener entre 8 y 72 caracteres");
        assertThatThrownBy(() -> UserAccount.validatePassword("x".repeat(73)))
                .isInstanceOf(BusinessRuleException.class);
    }
}
