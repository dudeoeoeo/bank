package com.bank.project.common.token;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class TokenProvider {

    private TokenEncoder tokenEncoder;
    private Environment env;

    public TokenProvider(TokenEncoder tokenEncoder, Environment env) {
        this.tokenEncoder = tokenEncoder;
        this.env = env;
    }

    public String getUserIdFromPublicToken(String publicToken) {
        String decode = tokenEncoder.decode(publicToken);
        return decode.split(TokenProperties.PUBLIC_KEY)[1];
    }

    public String createPublicToken(String userId) {
        final String uuid = UUID.randomUUID().toString() + TokenProperties.PUBLIC_KEY + userId;
        final String encodeStr = tokenEncoder.encode(uuid);

        return encodeStr;
    }

    public String createPrivateToken(String userId) {
        final String uuid = UUID.randomUUID().toString() + userId;
        final String privateEncode = tokenEncoder.privateEncode(uuid);

        return privateEncode;
    }
}
