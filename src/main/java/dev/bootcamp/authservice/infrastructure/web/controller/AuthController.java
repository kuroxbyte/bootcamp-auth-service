package dev.bootcamp.authservice.infrastructure.web.controller;

import dev.bootcamp.authservice.application.usecase.LoginUseCase;
import dev.bootcamp.authservice.application.usecase.RegisterUserUseCase;
import dev.bootcamp.authservice.application.usecase.RegisterWalletUserUseCase;
import dev.bootcamp.authservice.application.usecase.WalletLoginUseCase;
import dev.bootcamp.authservice.infrastructure.web.api.AuthApi;
import dev.bootcamp.authservice.infrastructure.web.dto.LoginRequestDto;
import dev.bootcamp.authservice.infrastructure.web.dto.RegisterRequestDto;
import dev.bootcamp.authservice.infrastructure.web.dto.RegisterResponseDto;
import dev.bootcamp.authservice.infrastructure.web.dto.TokenResponseDto;
import dev.bootcamp.authservice.infrastructure.web.dto.WalletLoginRequestDto;
import dev.bootcamp.authservice.infrastructure.web.dto.WalletUserRequestDto;
import dev.bootcamp.authservice.infrastructure.web.dto.WalletUserResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Endpoints de registro e inicio de sesión de clientes y de monederos Yanki definidos en el
 * contrato OpenAPI.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class AuthController implements AuthApi {

    private static final String BEARER = "Bearer";

    private final RegisterUserUseCase registerUserUseCase;
    private final LoginUseCase loginUseCase;
    private final RegisterWalletUserUseCase registerWalletUserUseCase;
    private final WalletLoginUseCase walletLoginUseCase;

    @Override
    public Mono<ResponseEntity<RegisterResponseDto>> register(
            Mono<RegisterRequestDto> registerRequestDto,
            ServerWebExchange exchange) {
        return registerRequestDto
                .flatMap(request -> Mono.from(registerUserUseCase
                        .execute(
                                request.getCustomerId(),
                                request.getUsername(),
                                request.getPassword())
                        .toFlowable()))
                .doOnNext(user -> log.info(
                        "User registered customerId={} username={}",
                        user.customerId(),
                        user.username()))
                .map(user -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(new RegisterResponseDto(user.customerId(), user.username())));
    }

    @Override
    public Mono<ResponseEntity<TokenResponseDto>> login(
            Mono<LoginRequestDto> loginRequestDto,
            ServerWebExchange exchange) {
        return loginRequestDto
                .flatMap(request -> Mono.from(loginUseCase
                        .execute(request.getUsername(), request.getPassword())
                        .toFlowable()))
                .map(token -> ResponseEntity.ok(
                        new TokenResponseDto(token.value(), BEARER, token.expiresIn())));
    }

    @Override
    public Mono<ResponseEntity<WalletUserResponseDto>> registerWalletUser(
            Mono<WalletUserRequestDto> walletUserRequestDto,
            ServerWebExchange exchange) {
        return walletUserRequestDto
                .flatMap(request -> Mono.from(registerWalletUserUseCase
                        .execute(request.getPhone(), request.getPassword())
                        .toFlowable()))
                .doOnNext(user -> log.info("Wallet user registered walletId={}", user.walletId()))
                .map(user -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(new WalletUserResponseDto(user.walletId(), user.phone())));
    }

    @Override
    public Mono<ResponseEntity<TokenResponseDto>> walletLogin(
            Mono<WalletLoginRequestDto> walletLoginRequestDto,
            ServerWebExchange exchange) {
        return walletLoginRequestDto
                .flatMap(request -> Mono.from(walletLoginUseCase
                        .execute(request.getPhone(), request.getPassword())
                        .toFlowable()))
                .map(token -> ResponseEntity.ok(
                        new TokenResponseDto(token.value(), BEARER, token.expiresIn())));
    }
}
