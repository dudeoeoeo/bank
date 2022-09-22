package com.bank.project.common.encode;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.apache.commons.codec.binary.Hex;

import javax.annotation.PostConstruct;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Slf4j
@Converter
public class EncodingConverter implements AttributeConverter<String, String> {

    private static final String UTF_8 = "UTF-8";
    private static final String SHA_512 = "SHA-512";
    private static final String AES = "AES";
    private static String SECRET_KEY;

    @Autowired
    private Environment env;

    @PostConstruct
    void init() {
        try {
            SECRET_KEY = env.getProperty("db.secret");
        } catch (Exception e) {
            log.warn(e.getMessage());
        }
    }

    private static String getSHA512() {
        String toReturn = null;

        try {
            MessageDigest digest = MessageDigest.getInstance(SHA_512);
            digest.reset();
            digest.update(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
            toReturn = String.format("%0128x", new BigInteger(1, digest.digest()));
        } catch (Exception e) {
            e.printStackTrace();
            log.warn(e.getMessage());
        }

        return toReturn;
    }

    @SneakyThrows
    @Override
    public String convertToDatabaseColumn(String plainText) {
        if (plainText == null)
            return null;

        String strKey = getSHA512();
        final Cipher cipher = Cipher.getInstance(AES);
        cipher.init(Cipher.ENCRYPT_MODE, generateMySQLAESKey(strKey, UTF_8));
        return new String(Hex.encodeHex(cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8))));
    }

    @SneakyThrows
    @Override
    public String convertToEntityAttribute(String encodedText) {
        if (encodedText == null)
            return null;

        String strKey = getSHA512();
        final Cipher decryptCipher = Cipher.getInstance(AES);
        decryptCipher.init(Cipher.DECRYPT_MODE, generateMySQLAESKey(strKey, UTF_8));
        return new String(decryptCipher.doFinal(Hex.decodeHex(encodedText.toCharArray())));
    }

    private static SecretKeySpec generateMySQLAESKey(String key, String encoding) {
        try {
            final byte[] finalKey = new byte[16];
            int i = 0;
            for (byte b : key.getBytes(encoding))
                finalKey[i++ % 16] ^= b;
            return new SecretKeySpec(finalKey, AES);
        } catch (UnsupportedEncodingException e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
