package com.op1m.medrem.backend_api.security;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class TelegramInitDataValidator {

    public static boolean validateInitData(String initData, String botToken) {
        try {
            Map<String, String> params = parseInitData(initData);

            if (!params.containsKey("hash")) return false;

            String receivedHash = params.remove("hash");

            List<String> keys = new ArrayList<>(params.keySet());
            Collections.sort(keys);

            StringBuilder dataCheckString = new StringBuilder();

            for (int i = 0; i < keys.size(); i++) {
                String key = keys.get(i);
                dataCheckString.append(key)
                        .append("=")
                        .append(params.get(key));

                if (i < keys.size() - 1) dataCheckString.append("\n");
            }

            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");

            byte[] secretKey = sha256.digest(
                    botToken.getBytes(StandardCharsets.UTF_8)
            );

            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secretKey, "HmacSHA256"));

            byte[] hashBytes = mac.doFinal(
                    dataCheckString.toString().getBytes(StandardCharsets.UTF_8)
            );

            StringBuilder hex = new StringBuilder();

            for (byte b : hashBytes) {
                hex.append(String.format("%02x", b));
            }

            return hex.toString().equals(receivedHash);

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static Map<String, String> parseInitData(String initData) {
        Map<String, String> map = new HashMap<>();

        for (String pair : initData.split("&")) {
            int idx = pair.indexOf('=');

            if (idx > 0) {
                map.put(
                        URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8),
                        URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8)
                );
            }
        }

        return map;
    }
}