package com.example.kolonnawabarbellgym.Mail;

import java.security.SecureRandom;

public class OtpGenerator {
    public static String generateOtp(int length) {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }
}
