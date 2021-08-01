package com.adtsw.jchannels.auth;

import com.adtsw.jchannels.model.Constants;
import com.adtsw.jchannels.model.HttpRequest;
import com.adtsw.jchannels.model.auth.SessionInfo;
import com.adtsw.jchannels.model.exception.InvalidTokenException;
import com.adtsw.jchannels.model.exception.UnauthorizedAccessException;
import lombok.AllArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@AllArgsConstructor
public class APIAuthorizer implements IAuthorizer {

    private static final Logger logger = LogManager.getLogger(APIAuthorizer.class);

    private final ITokenManager tokenManager;

    public SessionInfo authorize(HttpRequest request, String scope) throws UnauthorizedAccessException {

        SessionInfo sessionInfo = null;

        try {
            String authorizationHeaderValue = request.getHeaders().get(Constants.AUTHORIZATION_HEADER);
            if(StringUtils.isNotEmpty(authorizationHeaderValue)) {
                String[] authHeaderValueSplits = authorizationHeaderValue.split(" ");
                if(authHeaderValueSplits.length == 2
                    && Constants.AUTHORIZATION_BEARER_TYPE.equals(authHeaderValueSplits[0])) {
                    sessionInfo = tokenManager.validate(authHeaderValueSplits[1]);
                }
            }
        } catch (InvalidTokenException te) {
            logger.warn("Exception while verifying token : " + te.getMessage());
        }
        
        /*
            Allow access only if 
                - Scope of API is not given or defined as open ( allow with / without session info )
                - Scope of API is given as AUTHENTICATED ( allow if session info exists )
                - Scope of API is any other ( allow only if session info has required scopes ) 
         */
        if(StringUtils.isNotEmpty(scope) && !Constants.OPEN_SCOPE.equals(scope)) {
            // enter if scope is given and is not open. Here authentication should atleast be done
            if(sessionInfo == null) {
                throw new UnauthorizedAccessException("Unauthorized access");
            }
            if(!Constants.AUTHENTICATED_SCOPE.equals(scope) && !sessionInfo.getScope().contains(scope)) {
                // enter if API is not open for all authenticated users and session scope doesn't contain required scope
                throw new UnauthorizedAccessException(
                    "Insufficient permissions for user " + sessionInfo.getIdentity());
            }
        }
        
        return sessionInfo;
    }
}
