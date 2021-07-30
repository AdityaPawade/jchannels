package com.jp.jchannels.auth;

import com.jp.jchannels.model.auth.SessionInfo;
import com.jp.jchannels.model.auth.TokenInfo;
import com.jp.jchannels.model.exception.InvalidTokenException;

public interface ITokenManager {

    TokenInfo generate(SessionInfo sessionInfo);
    
    SessionInfo validate(String token) throws InvalidTokenException;
}
