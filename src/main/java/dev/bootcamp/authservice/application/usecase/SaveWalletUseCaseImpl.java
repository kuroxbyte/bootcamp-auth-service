package dev.bootcamp.authservice.application.usecase;

import dev.bootcamp.authservice.application.port.WalletRepository;
import dev.bootcamp.authservice.domain.model.wallet.Wallet;
import io.reactivex.rxjava3.core.Completable;

/**
 * Implementación de {@link SaveWalletUseCase}.
 */
public class SaveWalletUseCaseImpl implements SaveWalletUseCase {
    private final WalletRepository walletRepository;

    /**
     * Crea el caso de uso con el adaptador de su puerto.
     */
    public SaveWalletUseCaseImpl(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    @Override
    public Completable execute(Wallet wallet) {
        return walletRepository.save(wallet);
    }
}
