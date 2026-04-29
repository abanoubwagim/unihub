package com.unihub.identity.application.usecase;

import java.util.UUID;

public interface DeleteAccountUseCase {
    void deleteAccount(UUID userId, String password);
}
