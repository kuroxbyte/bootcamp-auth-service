package dev.bootcamp.authservice.infrastructure.web;

import dev.bootcamp.authservice.domain.exception.BusinessRuleException;
import dev.bootcamp.authservice.domain.exception.InvalidCredentialsException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.result.method.annotation.ResponseEntityExceptionHandler;
import reactor.core.publisher.Mono;

/**
 * Traduce las excepciones a respuestas Problem Details (RFC 7807).
 *
 * <ul>
 *   <li>400: datos de entrada inválidos; lo resuelve {@link ResponseEntityExceptionHandler}.
 *   <li>401: {@link InvalidCredentialsException}.
 *   <li>422: {@link BusinessRuleException}, o un usuario duplicado que detecta el índice único de
 *       MongoDB ({@link DuplicateKeyException}) cuando dos registros llegan a la vez.
 *   <li>500: cualquier otro error inesperado.
 * </ul>
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    /**
     * Responde 401 cuando el usuario o la contraseña son incorrectos.
     */
    @ExceptionHandler(InvalidCredentialsException.class)
    public Mono<ResponseEntity<ProblemDetail>> handleInvalidCredentials(
            InvalidCredentialsException ex) {
        log.warn("Login rejected: {}", ex.getMessage());
        return problem(HttpStatus.UNAUTHORIZED, "No autenticado", ex.getMessage());
    }

    /**
     * Responde 422 cuando se incumple una regla de negocio.
     */
    @ExceptionHandler(BusinessRuleException.class)
    public Mono<ResponseEntity<ProblemDetail>> handleBusinessRule(BusinessRuleException ex) {
        log.warn("Business rule violated: {}", ex.getMessage());
        return problem(
                HttpStatus.UNPROCESSABLE_CONTENT,
                "Regla de negocio incumplida",
                ex.getMessage());
    }

    /**
     * Responde 422 cuando el índice único rechaza un usuario duplicado.
     */
    @ExceptionHandler(DuplicateKeyException.class)
    public Mono<ResponseEntity<ProblemDetail>> handleDuplicate(DuplicateKeyException ex) {
        log.warn("Duplicate user account: {}", ex.getMessage());
        return problem(
                HttpStatus.UNPROCESSABLE_CONTENT,
                "Regla de negocio incumplida",
                "El cliente ya tiene un usuario o el nombre de usuario ya está en uso");
    }

    /**
     * Responde 500 ante cualquier otro error, sin exponer el detalle interno.
     */
    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ProblemDetail>> handleUnexpected(Exception ex) {
        log.error("Unexpected error", ex);
        return problem(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Error interno",
                "Ocurrió un error inesperado");
    }

    private static Mono<ResponseEntity<ProblemDetail>> problem(
            HttpStatus status,
            String title,
            String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        return Mono.just(ResponseEntity.status(status).body(problem));
    }
}
