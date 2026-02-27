package com.op1m.medrem.backend_api.security;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class TelegramInitDataValidator {

    private static final String HMAC_ALGO = "HmacSHA256";

    public static Map<String, String> parseInitData(String initData) {
        Map<String, String> map = new HashMap<>();
        if (initData == null || initData.isBlank()) return map;

        String[] pairs = initData.split("&");

        for (String p : pairs) {
            int idx = p.indexOf('=');
            if (idx > 0) {
                String k = URLDecoder.decode(p.substring(0, idx), StandardCharsets.UTF_8);
                String v = URLDecoder.decode(p.substring(idx + 1), StandardCharsets.UTF_8);
                map.put(k, v);
            }
        }
        return map;
    }

    public static boolean validateInitData(String initData, String botToken) {

        try {
            Map<String, String> map = parseInitData(initData);

            if (!map.containsKey("hash")) {
                System.out.println("No hash in initData");
                return false;
            }

            String receivedHash = map.remove("hash");

            map.remove("signature");

            List<String> keys = new ArrayList<>(map.keySet());
            Collections.sort(keys);

            StringBuilder sb = new StringBuilder();

            for (int i = 0; i < keys.size(); i++) {
                String k = keys.get(i);
                sb.append(k).append("=").append(map.get(k));
                if (i < keys.size() - 1) sb.append("\n");
            }

            String dataCheckString = sb.toString();

            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            byte[] secretKey = sha256.digest(botToken.getBytes(StandardCharsets.UTF_8));

            Mac mac = Mac.getInstance(HMAC_ALGO);
            SecretKeySpec keySpec = new SecretKeySpec(secretKey, HMAC_ALGO);
            mac.init(keySpec);

            byte[] hmac = mac.doFinal(dataCheckString.getBytes(StandardCharsets.UTF_8));

            StringBuilder hex = new StringBuilder();
            for (byte b : hmac) {
                hex.append(String.format("%02x", b));
            }

            String computedHash = hex.toString();

            System.out.println("----- TELEGRAM VALIDATION -----");
            System.out.println("dataCheckString:\n" + dataCheckString);
            System.out.println("receivedHash:  " + receivedHash);
            System.out.println("computedHash:  " + computedHash);
            System.out.println("--------------------------------");

            return computedHash.equals(receivedHash);

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
