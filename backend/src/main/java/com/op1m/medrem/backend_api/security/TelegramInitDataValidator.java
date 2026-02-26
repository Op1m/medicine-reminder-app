package com.op1m.medrem.backend_api.security;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class TelegramInitDataValidator {
    private static final String HMAC_ALGO = "HmacSHA256";

    public static Map<String,String> parseInitData(String initData) {
        Map<String, String> map = new HashMap<>();
        if (initData == null || initData.isEmpty()) return map;
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
            Map<String, String> data = parseInitData(initData);
            if (!data.containsKey("hash")) return false;
            String hash = data.get("hash");
            data.remove("hash");

            List<String> keys = new ArrayList<>(data.keySet());
            Collections.sort(keys);
            List<String> items = new ArrayList<>();
            for (String k : keys) {
                items.add(k + "=" + data.get(k));
            }
            String dataCheckString = String.join("\n", items);

            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            byte[] secretKey = sha256.digest(botToken.getBytes(StandardCharsets.UTF_8));

            Mac mac = Mac.getInstance(HMAC_ALGO);
            SecretKeySpec keySpec = new SecretKeySpec(secretKey, HMAC_ALGO);
            mac.init(keySpec);
            byte[] hmac = mac.doFinal(dataCheckString.getBytes(StandardCharsets.UTF_8));

            StringBuilder sb = new StringBuilder();
            for (byte b : hmac) {
                sb.append(String.format("%02x", b));
            }
            String computed = sb.toString();

            return computed.equals(hash);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}