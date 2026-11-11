package dev.bootcamp.authservice.infrastructure.config;

import dev.bootcamp.authservice.application.port.CustomerRepository;
import dev.bootcamp.authservice.application.port.PasswordHasher;
import dev.bootcamp.authservice.application.port.TokenIssuer;
import dev.bootcamp.authservice.application.port.UserAccountRepository;
import dev.bootcamp.authservice.application.usecase.LoginUseCase;
import dev.bootcamp.authservice.application.usecase.LoginUseCaseImpl;
import dev.bootcamp.authservice.application.usecase.RegisterUserUseCase;
import dev.bootcamp.authservice.application.usecase.RegisterUserUseCaseImpl;
import dev.bootcamp.authservice.application.usecase.SaveCustomerUseCase;
import dev.bootcamp.authservice.application.usecase.SaveCustomerUseCaseImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registra los casos de uso como beans. Los casos de uso no dependen de Spring; aquí se les
 * inyectan los adaptadores de sus puertos.
 */
@Configuration
public class UseCaseConfig {

    /**
     * Registra el caso de uso {@link SaveCustomerUseCase}.
     */
    @Bean
    public SaveCustomerUseCase saveCustomerUseCase(CustomerRepository customerRepository) {
        return new SaveCustomerUseCaseImpl(customerRepository);
    }

    /**
     * Registra el caso de uso {@link RegisterUserUseCase}.
     */
    @Bean
    public RegisterUserUseCase registerUserUseCase(
            CustomerRepository customerRepository,
            UserAccountRepository userAccountRepository,
            PasswordHasher passwordHasher) {
        return new RegisterUserUseCaseImpl(
                customerRepository,
                userAccountRepository,
                passwordHasher);
    }

    /**
     * Registra el caso de uso {@link LoginUseCase}.
     */
    @Bean
    public LoginUseCase loginUseCase(
            UserAccountRepository userAccountRepository,
            PasswordHasher passwordHasher,
            TokenIssuer tokenIssuer) {
        return new LoginUseCaseImpl(userAccountRepository, passwordHasher, tokenIssuer);
    }
}
