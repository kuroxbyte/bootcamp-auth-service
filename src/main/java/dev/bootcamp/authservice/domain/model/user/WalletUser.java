package dev.bootcamp.authservice.domain.model.user;

/**
 * Acceso del dueño de un monedero Yanki: inicia sesión con el celular y una contraseña. Guarda la
 * contraseña solo cifrada.
 *
 * @param id           id del acceso; null antes de guardarlo
 * @param walletId     monedero al que pertenece; un monedero tiene un solo acceso
 * @param phone        celular del monedero, único
 * @param passwordHash contraseña cifrada
 */
public record WalletUser(String id, String walletId, String phone, String passwordHash) {}
