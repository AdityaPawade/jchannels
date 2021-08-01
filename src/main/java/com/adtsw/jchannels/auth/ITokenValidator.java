package com.adtsw.jchannels.auth;

import com.adtsw.jchannels.model.auth.SessionInfo;

public interface ITokenValidator {

    boolean validate(String tokenId, SessionInfo sessionInfo);
}
