package com.unihub.identity.application;

import java.util.UUID;

public interface DeleteAccountUseCase {
    void deleteAccount(UUID userId, String password);
}
