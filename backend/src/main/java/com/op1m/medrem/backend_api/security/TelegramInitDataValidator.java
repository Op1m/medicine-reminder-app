package com.op1m.medrem.backend_api.security;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.*;

public class TelegramInitDataValidator {

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

    public static boolean validateInitData(String initData, String botToken) {
        try {
            if (initData == null || botToken == null || botToken.isBlank()) return false;

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

            byte[] botTokenBytes = botToken.getBytes(StandardCharsets.UTF_8);
            Mac mac1 = Mac.getInstance("HmacSHA256");
            mac1.init(new SecretKeySpec(botTokenBytes, "HmacSHA256"));
            byte[] secretKey = mac1.doFinal("WebAppData".getBytes(StandardCharsets.UTF_8));

            Mac mac2 = Mac.getInstance("HmacSHA256");
            mac2.init(new SecretKeySpec(secretKey, "HmacSHA256"));
            byte[] hmac = mac2.doFinal(dataCheckString.getBytes(StandardCharsets.UTF_8));

            String computedHex = bytesToHex(hmac);

            if (!computedHex.equalsIgnoreCase(receivedHash)) return false;

            if (params.containsKey("auth_date")) {
                try {
                    long authDate = Long.parseLong(params.get("auth_date"));
                    long now = Instant.now().getEpochSecond();
                    long allowedSeconds = 86400L;
                    if (Math.abs(now - authDate) > allowedSeconds) return false;
                } catch (Exception ignored) {}
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }

    public static class TelegramUserData {
        public final long id;
        public final String firstName;
        public final String lastName;
        public final String username;
        public final String photoUrl;

        public TelegramUserData(long id, String firstName, String lastName, String username, String photoUrl) {
            this.id = id;
            this.firstName = firstName;
            this.lastName = lastName;
            this.username = username;
            this.photoUrl = photoUrl;
        }
    }

    public static TelegramUserData extractUser(Map<String, String> decodedParams) {
        String userJson = decodedParams.get("user");
        if (userJson == null) return null;
        try {
            long id = Long.parseLong(extractField(userJson, "id"));
            String firstName = extractField(userJson, "first_name");
            String lastName = extractField(userJson, "last_name");
            String username = extractField(userJson, "username");
            String photoUrl = extractField(userJson, "photo_url");
            return new TelegramUserData(id, firstName, lastName, username, photoUrl);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String extractField(String json, String field) {
        String key = "\"" + field + "\":";
        int start = json.indexOf(key);
        if (start == -1) return "";
        start += key.length();
        if (start >= json.length()) return "";
        char ch = json.charAt(start);
        if (ch == '"') {
            int end = json.indexOf('"', start + 1);
            if (end == -1) return "";
            return json.substring(start + 1, end);
        } else {
            int end = json.indexOf(',', start);
            if (end == -1) end = json.indexOf('}', start);
            if (end == -1) end = json.length();
            return json.substring(start, end).trim();
        }
    }
}
