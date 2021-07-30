package com.jp.jchannels.auth;

import com.jp.jchannels.model.auth.SessionInfo;

public interface ITokenValidator {

    boolean validate(String tokenId, SessionInfo sessionInfo);
}
