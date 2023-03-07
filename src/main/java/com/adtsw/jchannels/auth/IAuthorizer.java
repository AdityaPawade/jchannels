package com.adtsw.jchannels.auth;

import com.adtsw.jchannels.model.HttpRequest;
import com.adtsw.jchannels.model.auth.SessionInfo;
import com.adtsw.jchannels.model.exception.UnauthorizedAccessException;

public interface IAuthorizer {

    SessionInfo authorize(HttpRequest request, String scope) throws UnauthorizedAccessException;
}
