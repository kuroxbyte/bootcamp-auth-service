package dev.bootcamp.authservice.application.usecase;

import dev.bootcamp.authservice.application.port.WalletRepository;
import dev.bootcamp.authservice.domain.model.wallet.Wallet;
import io.reactivex.rxjava3.core.Completable;
import lombok.RequiredArgsConstructor;

/**
 * Implementación de {@link SaveWalletUseCase}.
 */
@RequiredArgsConstructor
public class SaveWalletUseCaseImpl implements SaveWalletUseCase {
    private final WalletRepository walletRepository;

    @Override
    public Completable execute(Wallet wallet) {
        return walletRepository.save(wallet);
    }
}
