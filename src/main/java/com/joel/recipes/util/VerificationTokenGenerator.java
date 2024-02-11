package com.joel.recipes.util;

import com.bastiaanjansen.otp.HMACAlgorithm;
import com.bastiaanjansen.otp.HOTPGenerator;
import com.bastiaanjansen.otp.SecretGenerator;

public class VerificationTokenGenerator {
    private static byte[] secret = SecretGenerator.generate(521);

    public static String generateToken() {
        final int COUNTER = 5;
        HOTPGenerator hotp = new HOTPGenerator.Builder(secret).withPasswordLength(6).withAlgorithm(HMACAlgorithm.SHA256).build();
        return hotp.generate(COUNTER);
    }
}
