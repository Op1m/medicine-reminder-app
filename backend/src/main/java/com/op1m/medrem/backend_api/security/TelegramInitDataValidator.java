package com.op1m.medrem.backend_api.security;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

public final class TelegramInitDataValidator {

    private TelegramInitDataValidator() {}

    public static boolean validate(Map<String, String> initData, String botToken) throws Exception {
        if (initData == null) return false;
        String providedHash = initData.get("hash");
        if (providedHash == null || providedHash.isEmpty()) return false;

        Map<String, String> copy = new HashMap<>(initData);
        copy.remove("hash");

        List<String> parts = copy.entrySet().stream()
                .filter(e -> e.getValue() != null)
                .map(e -> e.getKey() + "=" + e.getValue())
                .sorted()
                .collect(Collectors.toList());

        String dataCheckString = String.join("\n", parts);

        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        byte[] secretKey = sha256.digest(botToken.getBytes(StandardCharsets.UTF_8));

        Mac hmac = Mac.getInstance("HmacSHA256");
        hmac.init(new SecretKeySpec(secretKey, "HmacSHA256"));
        byte[] hmacBytes = hmac.doFinal(dataCheckString.getBytes(StandardCharsets.UTF_8));
        String calcHex = bytesToHexLower(hmacBytes);

        if (constantTimeEquals(calcHex, providedHash.toLowerCase(Locale.ROOT))) {
            if (copy.containsKey("auth_date")) {
                try {
                    long auth = Long.parseLong(copy.get("auth_date"));
                    long now = Instant.now().getEpochSecond();
                    if (Math.abs(now - auth) > 86400L) return false;
                } catch (NumberFormatException ex) {
                    return false;
                }
            }
            return true;
        }

        return false;
    }

    private static String bytesToHexLower(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) sb.append(String.format("%02x", b & 0xff));
        return sb.toString();
    }

    private static boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) return false;
        if (a.length() != b.length()) return false;
        int result = 0;
        for (int i = 0; i < a.length(); i++) result |= a.charAt(i) ^ b.charAt(i);
        return result == 0;
    }
}
