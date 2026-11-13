package dev.bootcamp.authservice.domain.model.wallet;

/**
 * Copia local de un monedero Yanki, construida con el evento {@code WalletCreated} de
 * yanki-service.
 *
 * @param id    id del monedero
 * @param phone celular del monedero; con él inicia sesión su dueño
 */
public record Wallet(String id, String phone) {}
