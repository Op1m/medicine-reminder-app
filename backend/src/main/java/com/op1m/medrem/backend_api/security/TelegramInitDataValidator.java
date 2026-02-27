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

            String hash = params.remove("hash");

            List<String> keys = new ArrayList<>(params.keySet());
            Collections.sort(keys);

            StringBuilder dataCheckString = new StringBuilder();

            for (int i = 0; i < keys.size(); i++) {
                String key = keys.get(i);
                dataCheckString.append(key)
                        .append("=")
                        .append(params.get(key));

                if (i < keys.size() - 1) {
                    dataCheckString.append("\n");
                }
            }

            Mac mac = Mac.getInstance("HmacSHA256");

            SecretKeySpec secretKey = new SecretKeySpec(
                    mac.doFinal(botToken.getBytes(StandardCharsets.UTF_8)),
                    "HmacSHA256"
            );

            Mac mac2 = Mac.getInstance("HmacSHA256");
            mac2.init(secretKey);

            byte[] computedHash = mac2.doFinal(
                    dataCheckString.toString().getBytes(StandardCharsets.UTF_8)
            );

            StringBuilder hex = new StringBuilder();
            for (byte b : computedHash) {
                hex.append(String.format("%02x", b));
            }

            return hex.toString().equals(hash);

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static Map<String, String> parseInitData(String initData) {
        Map<String, String> map = new HashMap<>();

        String[] pairs = initData.split("&");

        for (String pair : pairs) {
            int idx = pair.indexOf('=');
            if (idx > 0) {
                String key = URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8);
                String value = URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8);
                map.put(key, value);
            }
        }

        return map;
    }
}