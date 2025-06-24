package com.esprit.utils;

import java.security.SecureRandom;
import java.math.BigInteger;

public class TokenUtil {
    private static final SecureRandom random = new SecureRandom();

    /**
     * Generate a secure random token string.
     * @return token string
     */
    public static String generateToken() {
        return new BigInteger(130, random).toString(32);
    }
}
