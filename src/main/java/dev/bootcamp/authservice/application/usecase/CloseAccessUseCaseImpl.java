package dev.bootcamp.authservice.application.usecase;

import dev.bootcamp.authservice.application.port.CustomerRepository;
import dev.bootcamp.authservice.application.port.UserAccountRepository;
import dev.bootcamp.authservice.application.port.WalletRepository;
import dev.bootcamp.authservice.application.port.WalletUserRepository;
import dev.bootcamp.authservice.domain.model.wallet.Wallet;
import io.reactivex.rxjava3.core.Completable;
import lombok.RequiredArgsConstructor;

/**
 * Implementación de {@link CloseAccessUseCase}.
 */
@RequiredArgsConstructor
public class CloseAccessUseCaseImpl implements CloseAccessUseCase {
    private final CustomerRepository customerRepository;
    private final UserAccountRepository userAccountRepository;
    private final WalletRepository walletRepository;
    private final WalletUserRepository walletUserRepository;

    @Override
    public Completable closeCustomer(String customerId) {
        return userAccountRepository.deleteByCustomerId(customerId)
                .andThen(customerRepository.deleteById(customerId));
    }

    @Override
    public Completable closeWallet(Wallet wallet) {
        return walletUserRepository.deleteByWalletId(wallet.id())
                .andThen(walletRepository.delete(wallet));
    }
}
