package com.jp.jchannels.auth;

import com.jp.jchannels.model.HttpRequest;
import com.jp.jchannels.model.auth.SessionInfo;
import com.jp.jchannels.model.exception.UnauthorizedAccessException;

import javax.servlet.http.HttpServletRequest;

public interface IAuthorizer {

    SessionInfo authorize(HttpRequest request, String scope) throws UnauthorizedAccessException;
}
