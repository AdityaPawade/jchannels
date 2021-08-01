package com.adtsw.jchannels.auth;

import com.adtsw.jchannels.model.auth.SessionInfo;
import com.adtsw.jchannels.model.auth.TokenInfo;
import com.adtsw.jchannels.model.exception.InvalidTokenException;
import com.adtsw.jcommons.utils.HashingUtil;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class JWTTokenManagerTest {

    public static void main(String[] args) throws InvalidTokenException {

        byte[] digest = null;

        try {

            MessageDigest md5 = MessageDigest.getInstance("MD5");
            String md5String = HashingUtil.getMd5Hash("jchannels");
            digest = md5String.getBytes(StandardCharsets.UTF_8);

        } catch (NoSuchAlgorithmException e) {
        }

        JWTTokenManager auth = new JWTTokenManager(digest, 2 * 24 * 60 * 60);
        TokenInfo tokenInfo = auth.generate(new SessionInfo("id1", Arrays.asList("s1", "s2")));
        SessionInfo sessionInfo = auth.validate(tokenInfo.getToken());
        System.out.println("secure !!");
    }
}
