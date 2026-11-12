package dev.bootcamp.authservice.domain.model.customer;

/**
 * Copia local de un cliente del banco, construida con los eventos {@code CustomerCreated} de
 * customer-service. auth-service la usa para saber que el cliente existe sin consultarlo por REST.
 *
 * @param id      id del cliente en customer-service
 * @param type    PERSONAL o BUSINESS
 * @param profile STANDARD, VIP o PYME
 */
public record Customer(String id, String type, String profile) {}
