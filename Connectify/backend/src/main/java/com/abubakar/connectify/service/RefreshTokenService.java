package com.abubakar.connectify.service;

import com.abubakar.connectify.entity.RefreshToken;
import com.abubakar.connectify.entity.User;

public interface RefreshTokenService {

    RefreshToken createRefreshToken(
            User user
    );

    RefreshToken verifyRefreshToken(
            String token
    );

    void deleteByUser(
            User user
    );

}

