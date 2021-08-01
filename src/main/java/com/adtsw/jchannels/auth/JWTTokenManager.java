package com.adtsw.jchannels.auth;

import com.adtsw.jchannels.model.Constants;
import com.adtsw.jchannels.model.auth.SessionInfo;
import com.adtsw.jchannels.model.auth.TokenInfo;
import com.adtsw.jchannels.model.exception.InvalidTokenException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@AllArgsConstructor
public class JWTTokenManager implements ITokenManager {

    private static final Logger logger = LogManager.getLogger(JWTTokenManager.class);

    private final byte[] secretKey;
    private final long expirationInSeconds;
    private final Optional<ITokenValidator> additionalValidator;

    public JWTTokenManager(byte[] secretKey, long expirationInSeconds) {
        this.secretKey = secretKey;
        this.expirationInSeconds = expirationInSeconds;
        this.additionalValidator = Optional.empty();
    }

    public TokenInfo generate(SessionInfo sessionInfo) {

        SecretKey secretKey = Keys.hmacShaKeyFor(this.secretKey);
        String tokenId = sessionInfo.getIdentity() + "_" + System.currentTimeMillis();
        Date expirationDate = Date.from(Instant.now().plusSeconds(expirationInSeconds));
        String jws = Jwts.builder()
            .claim(Constants.SCOPE_CLAIM, sessionInfo.getScope())
            .setSubject(sessionInfo.getIdentity())
            .setId(tokenId)
            .setExpiration(expirationDate)
            .signWith(secretKey)
            .compact();
        
        return new TokenInfo(tokenId, jws);
    }
    
    public SessionInfo validate(String token) throws InvalidTokenException {

        try {

            Jws<Claims> claims = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token);
            String tokenId = claims.getBody().getId();
            checkExpiration(tokenId, claims);

            String sessionIdentity = claims.getBody().getSubject();
            Object tokenScope = claims.getBody().get(Constants.SCOPE_CLAIM);
            List<String> scope;
            try {
                if(tokenScope == null) scope = new ArrayList<>(); 
                else scope = (List<String>) tokenScope;
            } catch (Exception e) {
                scope = new ArrayList<>();
            }

            SessionInfo sessionInfo = new SessionInfo(sessionIdentity, scope);
            runAdditionalValidations(tokenId, sessionInfo);
            return sessionInfo;

        } catch (JwtException e) {
            throw new InvalidTokenException(e.getMessage());
        }
    }

    private void checkExpiration(String tokenId, Jws<Claims> claims) throws InvalidTokenException {
        
        Date expirationDate = claims.getBody().getExpiration();
        Instant currentInstance = ZonedDateTime.now().toInstant();
        if(expirationDate == null || currentInstance.isAfter(expirationDate.toInstant())) {
            logger.warn("token id " + tokenId + " failed expiration check");
            throw new InvalidTokenException("Expiration check failed for " + tokenId);
        }
    }

    private void runAdditionalValidations(String tokenId, SessionInfo sessionInfo) throws InvalidTokenException {
        if(additionalValidator.isPresent()) {
            if(!additionalValidator.get().validate(tokenId, sessionInfo)) {
                logger.warn("token id " + tokenId + " failed additional validation");
                throw new InvalidTokenException("Additional validation failed for " + tokenId);
            }
        }
    }
}
