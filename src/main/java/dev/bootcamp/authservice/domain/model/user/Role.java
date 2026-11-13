package dev.bootcamp.authservice.domain.model.user;

/**
 * Rol que lleva el token en su claim {@code roles}; el API Gateway decide con él qué rutas puede
 * usar.
 */
public enum Role {
    /**
     * Cliente del banco: productos bancarios y autorizar su tarjeta para un monedero.
     */
    CUSTOMER,
    /**
     * Dueño de un monedero Yanki: solo las rutas de Yanki.
     */
    WALLET
}
