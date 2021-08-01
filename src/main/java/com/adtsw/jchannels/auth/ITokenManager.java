package com.adtsw.jchannels.auth;

import com.adtsw.jchannels.model.auth.SessionInfo;
import com.adtsw.jchannels.model.auth.TokenInfo;
import com.adtsw.jchannels.model.exception.InvalidTokenException;

public interface ITokenManager {

    TokenInfo generate(SessionInfo sessionInfo);
    
    SessionInfo validate(String token) throws InvalidTokenException;
}
