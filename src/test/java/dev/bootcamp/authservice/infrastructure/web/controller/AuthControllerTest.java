package dev.bootcamp.authservice.infrastructure.web.controller;

import dev.bootcamp.authservice.application.usecase.AdminLoginUseCase;
import dev.bootcamp.authservice.application.usecase.LoginUseCase;
import dev.bootcamp.authservice.application.usecase.RegisterUserUseCase;
import dev.bootcamp.authservice.application.usecase.RegisterWalletUserUseCase;
import dev.bootcamp.authservice.application.usecase.WalletLoginUseCase;
import dev.bootcamp.authservice.domain.exception.BusinessRuleException;
import dev.bootcamp.authservice.domain.exception.InvalidCredentialsException;
import dev.bootcamp.authservice.domain.model.user.AccessToken;
import dev.bootcamp.authservice.domain.model.user.UserAccount;
import dev.bootcamp.authservice.domain.model.user.WalletUser;
import io.reactivex.rxjava3.core.Single;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebFluxTest(AuthController.class)
class AuthControllerTest {

    private static final String REGISTER =
            "{\"customerId\":\"c1\",\"username\":\"ana\",\"password\":\"Secreta123\"}";
    private static final String LOGIN = "{\"username\":\"ana\",\"password\":\"Secreta123\"}";

    @Autowired private WebTestClient webTestClient;

    @MockitoBean private RegisterUserUseCase registerUserUseCase;

    @MockitoBean private LoginUseCase loginUseCase;

    @MockitoBean private RegisterWalletUserUseCase registerWalletUserUseCase;

    @MockitoBean private WalletLoginUseCase walletLoginUseCase;

    @MockitoBean private AdminLoginUseCase adminLoginUseCase;

    private WebTestClient.ResponseSpec post(String path, String body) {
        return webTestClient.post()
                .uri(path)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .exchange();
    }

    @Test
    void registersUserWithoutReturningThePassword() {
        when(registerUserUseCase.execute("c1", "ana", "Secreta123"))
                .thenReturn(Single.just(new UserAccount("u1", "c1", "ana", "hash")));

        post("/api/v1/auth/register", REGISTER)
                .expectStatus()
                .isCreated()
                .expectBody()
                .jsonPath("$.customerId")
                .isEqualTo("c1")
                .jsonPath("$.username")
                .isEqualTo("ana")
                .jsonPath("$.password")
                .doesNotExist()
                .jsonPath("$.passwordHash")
                .doesNotExist();
    }

    @Test
    void rejectsInvalidRegistrationBody() {
        post("/api/v1/auth/register", "{\"customerId\":\"c1\",\"username\":\"an\","
                + "\"password\":\"Secreta123\"}")
                .expectStatus()
                .isBadRequest();
        post("/api/v1/auth/register", "{\"customerId\":\"c1\",\"username\":\"ana\","
                + "\"password\":\"corta\"}")
                .expectStatus()
                .isBadRequest();

        verify(registerUserUseCase, never()).execute(any(), any(), any());
    }

    @Test
    void returnsUnprocessableWhenRegistrationBreaksRuleOrIsDuplicated() {
        when(registerUserUseCase.execute("c1", "ana", "Secreta123"))
                .thenReturn(
                        Single.error(new BusinessRuleException("El cliente ya tiene un usuario")))
                .thenReturn(Single.error(new DuplicateKeyException("E11000")));

        post("/api/v1/auth/register", REGISTER).expectStatus().isEqualTo(422);
        post("/api/v1/auth/register", REGISTER).expectStatus().isEqualTo(422);
    }

    @Test
    void returnsBearerTokenOnLogin() {
        when(loginUseCase.execute("ana", "Secreta123"))
                .thenReturn(Single.just(new AccessToken("jwt", 3600)));

        post("/api/v1/auth/login", LOGIN)
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.accessToken")
                .isEqualTo("jwt")
                .jsonPath("$.tokenType")
                .isEqualTo("Bearer")
                .jsonPath("$.expiresIn")
                .isEqualTo(3600);
    }

    @Test
    void returnsUnauthorizedForInvalidCredentials() {
        when(loginUseCase.execute("ana", "Secreta123"))
                .thenReturn(Single.error(new InvalidCredentialsException()));

        post("/api/v1/auth/login", LOGIN)
                .expectStatus()
                .isUnauthorized()
                .expectBody()
                .jsonPath("$.detail")
                .isEqualTo("Usuario o contraseña incorrectos");
    }

    @Test
    void returnsInternalErrorWithoutDetails() {
        when(loginUseCase.execute("ana", "Secreta123"))
                .thenReturn(Single.error(new IllegalStateException("boom")));

        post("/api/v1/auth/login", LOGIN)
                .expectStatus()
                .is5xxServerError()
                .expectBody()
                .jsonPath("$.detail")
                .isEqualTo("Ocurrió un error inesperado");
    }

    @Test
    void registersWalletUserAndLogsInWithThePhone() {
        String body = "{\"phone\":\"987654321\",\"password\":\"Secreta123\"}";
        when(registerWalletUserUseCase.execute("987654321", "Secreta123"))
                .thenReturn(Single.just(new WalletUser("u1", "w1", "987654321", "hash")));
        when(walletLoginUseCase.execute("987654321", "Secreta123"))
                .thenReturn(Single.just(new AccessToken("jwt", 3600)))
                .thenReturn(Single.error(new InvalidCredentialsException()));

        post("/api/v1/auth/wallet-users", body)
                .expectStatus()
                .isCreated()
                .expectBody()
                .jsonPath("$.walletId")
                .isEqualTo("w1")
                .jsonPath("$.passwordHash")
                .doesNotExist();
        post("/api/v1/auth/wallet-login", body)
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.accessToken")
                .isEqualTo("jwt");
        post("/api/v1/auth/wallet-login", body).expectStatus().isUnauthorized();
        post("/api/v1/auth/wallet-users", "{\"phone\":\"987654321\",\"password\":\"corta\"}")
                .expectStatus()
                .isBadRequest();
    }

    @Test
    void returnsBearerTokenOnAdminLoginAndUnauthorizedOnWrongPassword() {
        when(adminLoginUseCase.execute("admin", "Admin2026!"))
                .thenReturn(Single.just(new AccessToken("admin-jwt", 3600)));
        when(adminLoginUseCase.execute("admin", "otra"))
                .thenReturn(Single.error(new InvalidCredentialsException()));

        post("/api/v1/auth/admin-login", "{\"username\":\"admin\",\"password\":\"Admin2026!\"}")
                .expectStatus().isOk()
                .expectBody().jsonPath("$.accessToken").isEqualTo("admin-jwt");
        post("/api/v1/auth/admin-login", "{\"username\":\"admin\",\"password\":\"otra\"}")
                .expectStatus().isUnauthorized();
    }
}
