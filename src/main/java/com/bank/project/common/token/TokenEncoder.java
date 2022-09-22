package com.bank.project.common.token;

import lombok.SneakyThrows;
import org.apache.commons.codec.binary.Base64;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.Key;
import java.security.MessageDigest;

@Component
public class TokenEncoder  {

    private String iv;
    private Key keySpec;
    private static final String UTF_8 = "UTF-8";
    private static final String AES = "AES";
    private static final String SHA_256 = "SHA-256";

    public TokenEncoder() throws UnsupportedEncodingException {

        byte [] keyBytes = new byte[16];
        byte [] b = TokenProperties.PRIVATE_KEY.getBytes(UTF_8);
        int len = b.length;
        if (len > keyBytes.length) {
            len = keyBytes.length;
        }
        System.arraycopy(b, 0, keyBytes, 0, len);
        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, AES);

        this.iv = TokenProperties.PRIVATE_KEY.substring(0, 16);
        this.keySpec = keySpec;
    }

    @SneakyThrows
    public String encode(String str) {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, new IvParameterSpec(iv.getBytes()));
        byte [] encoded = cipher.doFinal(str.getBytes(UTF_8));

        return new String(Base64.encodeBase64(encoded));
    }

    @SneakyThrows
    public String decode(String str) {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, new IvParameterSpec(iv.getBytes()));
        byte [] byteStr = Base64.decodeBase64(str.getBytes());

        return new String(cipher.doFinal(byteStr), UTF_8);
    }

    @SneakyThrows
    public String privateEncode(String str) {
        MessageDigest md = MessageDigest.getInstance(SHA_256);

        final byte[] digest = md.digest(str.getBytes());

        return String.format("%064x", new BigInteger(1, digest));
    }
}
