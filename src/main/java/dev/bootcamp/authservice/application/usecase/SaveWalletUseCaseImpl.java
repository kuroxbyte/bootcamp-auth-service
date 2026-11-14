package dev.bootcamp.authservice.application.usecase;

import dev.bootcamp.authservice.application.port.WalletRepository;
import dev.bootcamp.authservice.application.port.WalletUserRepository;
import dev.bootcamp.authservice.domain.model.wallet.Wallet;
import io.reactivex.rxjava3.core.Completable;
import lombok.RequiredArgsConstructor;

/**
 * Implementación de {@link SaveWalletUseCase}.
 */
@RequiredArgsConstructor
public class SaveWalletUseCaseImpl implements SaveWalletUseCase {
    private final WalletRepository walletRepository;
    private final WalletUserRepository walletUserRepository;

    @Override
    public Completable execute(Wallet wallet) {
        return walletUserRepository.deleteByPhoneAndOtherWallet(wallet.phone(), wallet.id())
                .andThen(walletRepository.deleteOtherWithPhone(wallet))
                .andThen(walletRepository.save(wallet));
    }
}
