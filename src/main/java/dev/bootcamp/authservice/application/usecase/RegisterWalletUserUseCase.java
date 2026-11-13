package dev.bootcamp.authservice.application.usecase;

import dev.bootcamp.authservice.domain.exception.BusinessRuleException;
import dev.bootcamp.authservice.domain.model.user.WalletUser;
import io.reactivex.rxjava3.core.Single;

/**
 * Caso de uso para registrar el acceso del dueño de un monedero Yanki con su celular y una
 * contraseña.
 *
 * <p>Reglas: el monedero debe existir (auth-service lo conoce por el evento {@code WalletCreated};
 * si se acaba de crear, puede tardar unos segundos); cada monedero tiene un solo acceso; la
 * contraseña tiene entre 8 y 72 caracteres y se guarda cifrada. Es reactivo: no hace nada hasta
 * que se suscribe al {@link Single}.
 */
public interface RegisterWalletUserUseCase {

    /**
     * Registra el acceso.
     *
     * @return el acceso guardado; error {@link BusinessRuleException} si la contraseña no es
     *     válida, el celular no tiene monedero o el monedero ya tiene acceso
     */
    Single<WalletUser> execute(String phone, String password);
}
