package com.op1m.medrem.backend_api.security;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import java.util.Base64;

public final class TelegramInitDataValidator {

    private static final String HMAC_ALGO = "HmacSHA256";
    private static final String WEBAPP_DATA_STRING = "WebAppData";

    public static boolean validateInitData(String initData, String botToken) {
        System.out.println("----- TelegramInitDataValidator START -----");
        System.out.println("raw initData (length=" + (initData == null ? 0 : initData.length()) + "): " + initData);
        if (initData == null || initData.isBlank() || botToken == null || botToken.isBlank()) {
            System.out.println("❌ initData or botToken empty");
            System.out.println("----- TelegramInitDataValidator END -----");
            return false;
        }

        try {
            Map<String, String> rawParams = parseRawPairs(initData);
            Map<String, String> decodedParams = parseDecodedParams(initData);

            System.out.println("raw params keys: " + rawParams.keySet());
            System.out.println("decoded params keys: " + decodedParams.keySet());

            if (!decodedParams.containsKey("hash")) {
                System.out.println("❌ missing 'hash' param");
                System.out.println("----- TelegramInitDataValidator END -----");
                return false;
            }
            String receivedHash = decodedParams.get("hash");
            decodedParams.remove("hash");
            decodedParams.remove("signature");

            List<String> keys = new ArrayList<>(decodedParams.keySet());
            Collections.sort(keys);

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < keys.size(); i++) {
                String k = keys.get(i);
                String v = decodedParams.get(k);
                sb.append(k).append("=").append(v == null ? "" : v);
                if (i < keys.size() - 1) sb.append("\n");
            }
            String dataCheckString = sb.toString();

            System.out.println("data_check_string: ");
            System.out.println(dataCheckString);

            byte[] secret = hmacSha256(botToken.getBytes(StandardCharsets.UTF_8), WEBAPP_DATA_STRING.getBytes(StandardCharsets.UTF_8));
            System.out.println("secret key (hex preview): " + previewHex(secret) + " length=" + secret.length);

            byte[] signature = hmacSha256(secret, dataCheckString.getBytes(StandardCharsets.UTF_8));
            String computedHex = bytesToHex(signature);
            String computedBase64 = Base64.getEncoder().encodeToString(signature);

            System.out.println("computedHash (hex): " + computedHex);
            System.out.println("computedHash (base64): " + computedBase64);
            System.out.println("receivedHash:          " + receivedHash);

            boolean match = computedHex.equalsIgnoreCase(receivedHash);
            System.out.println("match(hex equalsIgnoreCase received): " + match);
            
            StringBuilder sbRaw = new StringBuilder();
            List<String> rawKeys = new ArrayList<>(rawParams.keySet());
            rawKeys.remove("signature");
            rawKeys.remove("hash");
            Collections.sort(rawKeys);
            for (int i = 0; i < rawKeys.size(); i++) {
                String k = rawKeys.get(i);
                String v = rawParams.get(k);
                sbRaw.append(k).append("=").append(v == null ? "" : v);
                if (i < rawKeys.size() - 1) sbRaw.append("\n");
            }
            String dataCheckStringRaw = sbRaw.toString();
            byte[] signatureRaw = hmacSha256(secret, dataCheckStringRaw.getBytes(StandardCharsets.UTF_8));
            String computedHexRaw = bytesToHex(signatureRaw);
            System.out.println("computedHash (hex, using raw/urlencoded-values): " + computedHexRaw);
            System.out.println("data_check_string (raw values / urlencoded for user): ");
            System.out.println(dataCheckStringRaw);

            String authDateStr = decodedParams.get("auth_date");
            if (authDateStr != null && !authDateStr.isBlank()) {
                try {
                    long auth = Long.parseLong(authDateStr);
                    long now = Instant.now().getEpochSecond();
                    System.out.println("auth_date = " + auth + "  server_time = " + now + "  diff(s) = " + (now - auth));
                } catch (Exception e) {
                    System.out.println("cannot parse auth_date: " + authDateStr);
                }
            }

            System.out.println("----- TelegramInitDataValidator END -----");
            return match;
        } catch (Exception ex) {
            System.out.println("Exception during validateInitData:");
            ex.printStackTrace();
            System.out.println("----- TelegramInitDataValidator END -----");
            return false;
        }
    }

    private static byte[] hmacSha256(byte[] key, byte[] data) throws Exception {
        Mac mac = Mac.getInstance(HMAC_ALGO);
        SecretKeySpec keySpec = new SecretKeySpec(key, HMAC_ALGO);
        mac.init(keySpec);
        return mac.doFinal(data);
    }

    public static Map<String, String> parseRawPairs(String initData) {
        Map<String, String> map = new HashMap<>();
        if (initData == null) return map;
        String[] pairs = initData.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf('=');
            if (idx > 0) {
                String key = pair.substring(0, idx);
                String value = pair.substring(idx + 1);
                map.put(key, value);
            }
        }
        return map;
    }

    public static Map<String, String> parseDecodedParams(String initData) {
        Map<String, String> map = new HashMap<>();
        if (initData == null) return map;
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

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }

    private static String previewHex(byte[] bytes) {
        String h = bytesToHex(bytes);
        return h.length() <= 24 ? h : h.substring(0, 24) + "...";
    }
}
