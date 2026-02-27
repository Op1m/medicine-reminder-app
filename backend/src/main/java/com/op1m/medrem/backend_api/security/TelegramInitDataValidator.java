package com.op1m.medrem.backend_api.security;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class TelegramInitDataValidator {

    private static final String HMAC_ALGO = "HmacSHA256";
    private static final String SECRET_KEY_LABEL = "WebAppData";

    public static boolean validateInitData(String initData, String botToken) {
        try {
            Map<String, String> params = parseDecodedParams(initData);
            if (!params.containsKey("hash")) return false;

            String receivedHash = params.remove("hash");
            params.remove("signature");

            List<String> keys = new ArrayList<>(params.keySet());
            Collections.sort(keys);

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < keys.size(); i++) {
                String k = keys.get(i);
                sb.append(k).append("=").append(params.get(k));
                if (i < keys.size() - 1) sb.append("\n");
            }
            String dataCheckString = sb.toString();

            byte[] secretKey = hmacSha256(SECRET_KEY_LABEL.getBytes(StandardCharsets.UTF_8),
                    botToken.getBytes(StandardCharsets.UTF_8));

            byte[] computed = hmacSha256(secretKey, dataCheckString.getBytes(StandardCharsets.UTF_8));
            String computedHex = bytesToHex(computed);

            return computedHex.equalsIgnoreCase(receivedHash);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static Map<String, String> parseDecodedParams(String initData) {
        Map<String, String> map = new HashMap<>();
        if (initData == null) return map;
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

    private static byte[] hmacSha256(byte[] key, byte[] data) throws Exception {
        Mac mac = Mac.getInstance(HMAC_ALGO);
        SecretKeySpec spec = new SecretKeySpec(key, HMAC_ALGO);
        mac.init(spec);
        return mac.doFinal(data);
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) sb.append(String.format("%02x", b & 0xff));
        return sb.toString();
    }
}
