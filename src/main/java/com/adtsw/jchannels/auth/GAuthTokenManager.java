package com.adtsw.jchannels.auth;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.apache.v2.ApacheHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.adtsw.jchannels.model.auth.SessionInfo;
import com.adtsw.jchannels.model.auth.TokenInfo;
import com.adtsw.jchannels.model.exception.InvalidTokenException;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

public class GAuthTokenManager implements ITokenManager {

    private final JsonFactory gsonFactory = new GsonFactory();
    private final HttpTransport transport = new ApacheHttpTransport();
    private final GoogleIdTokenVerifier verifier;
    private final long tokenValiditySeconds;
    public static final long DEFAULT_TIME_SKEW_SECONDS = 300L;

    public GAuthTokenManager(String clientId, long tokenValiditySeconds) {
        this.verifier = new GoogleIdTokenVerifier.Builder(transport, gsonFactory)
            .setAudience(Collections.singletonList(clientId))
            .build();
        this.tokenValiditySeconds = tokenValiditySeconds;
    }
    
    public GAuthTokenManager(String clientId) {
        this(clientId, -1L);
    }

    @Override
    public TokenInfo generate(SessionInfo sessionInfo) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SessionInfo validate(String token) throws InvalidTokenException {

        try {

            GoogleIdToken idToken = GoogleIdToken.parse(gsonFactory, token);
            boolean signatureVerified = verify(idToken);

            if (signatureVerified) {

                Payload payload = idToken.getPayload();

                String userId = payload.getSubject();
                String email = payload.getEmail();

                long currentTimeMillis = System.currentTimeMillis();
                boolean emailVerified = payload.getEmailVerified();
                boolean issuedAtVerified = verifyIssuedAtTime(payload, currentTimeMillis, DEFAULT_TIME_SKEW_SECONDS);
                boolean expiryVerified = verifyExpirationTime(payload, currentTimeMillis, DEFAULT_TIME_SKEW_SECONDS);
                boolean issuedAtRangeVerified = verifyIssuedAtTimeRange(payload, currentTimeMillis, this.tokenValiditySeconds);
                
                if(emailVerified && issuedAtVerified && (expiryVerified || issuedAtRangeVerified)) {
                    return new SessionInfo(email, new ArrayList<>());
                }

                throw new InvalidTokenException("token expired");

            } else {
                throw new InvalidTokenException("signature verification failed");
            }

        } catch (Exception e) {
            throw new InvalidTokenException(e.getMessage());
        }
    }

    public boolean verify(GoogleIdToken googleIdToken) throws GeneralSecurityException, IOException {

        Iterator<PublicKey> var2 = verifier.getPublicKeysManager().getPublicKeys().iterator();

        PublicKey publicKey;
        do {
            if (!var2.hasNext()) {
                return false;
            }
            publicKey = var2.next();
        } while(!googleIdToken.verifySignature(publicKey));

        return true;
    }

    public final boolean verifyIssuedAtTime(Payload payload, long currentTimeMillis, long acceptableTimeSkewSeconds) {
        return currentTimeMillis >= (payload.getIssuedAtTimeSeconds() - acceptableTimeSkewSeconds) * 1000L;
    }

    public final boolean verifyExpirationTime(Payload payload, long currentTimeMillis, long acceptableTimeSkewSeconds) {
        return currentTimeMillis <= (payload.getExpirationTimeSeconds() + acceptableTimeSkewSeconds) * 1000L;
    }

    public final boolean verifyIssuedAtTimeRange(Payload payload, long currentTimeMillis, long acceptableTimeRangeSeconds) {
        return currentTimeMillis <= (payload.getIssuedAtTimeSeconds() + acceptableTimeRangeSeconds) * 1000L;
    }
}
