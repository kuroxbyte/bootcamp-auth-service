package dev.bootcamp.authservice.application.usecase;

import dev.bootcamp.authservice.domain.exception.BusinessRuleException;
import dev.bootcamp.authservice.domain.model.user.UserAccount;
import io.reactivex.rxjava3.core.Single;

/**
 * Caso de uso para registrar el usuario con el que un cliente inicia sesión.
 *
 * <p>Reglas:
 *
 * <ul>
 *   <li>El cliente debe existir en la copia local (evento {@code CustomerCreated}).
 *   <li>Un cliente tiene un solo usuario.
 *   <li>El nombre de usuario es único.
 *   <li>La contraseña tiene entre 8 y 72 caracteres y se guarda cifrada.
 * </ul>
 *
 * <p>Es reactivo: no hace nada hasta que se suscribe al {@link Single}. Termina con error {@link
 * BusinessRuleException} si se incumple una regla; en ese caso no se guarda nada.
 */
public interface RegisterUserUseCase {

    /**
     * Registra el usuario del cliente.
     *
     * @param customerId id del cliente en customer-service
     * @param username   nombre de usuario
     * @param password   contraseña sin cifrar
     * @return el usuario guardado, con la contraseña cifrada
     */
    Single<UserAccount> execute(String customerId, String username, String password);
}
