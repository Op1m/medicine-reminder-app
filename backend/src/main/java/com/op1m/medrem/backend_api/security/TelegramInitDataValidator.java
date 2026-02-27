package com.op1m.medrem.backend_api.security;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

public class TelegramInitDataValidator {

    public static boolean validateInitData(String initData, String botToken) {
        try {
            System.out.println("=== TelegramInitDataValidator ===");
            System.out.println("initData: " + initData);
            System.out.println("botToken starts with: " + (botToken != null && botToken.length() > 5 ? botToken.substring(0, 5) + "..." : "null"));
            
            Map<String, String> rawParams = parseRawPairs(initData);
            System.out.println("Raw params keys: " + rawParams.keySet());

            if (!rawParams.containsKey("hash")) {
                System.out.println("❌ No 'hash' parameter found");
                return false;
            }
            String receivedHash = rawParams.remove("hash");
            System.out.println("receivedHash: " + receivedHash);

            List<String> keys = new ArrayList<>(rawParams.keySet());
            Collections.sort(keys);
            System.out.println("Sorted keys: " + keys);

            StringBuilder dataCheckString = new StringBuilder();
            for (int i = 0; i < keys.size(); i++) {
                String key = keys.get(i);
                String value = rawParams.get(key);
                dataCheckString.append(key).append("=").append(value);
                if (i < keys.size() - 1) dataCheckString.append("\n");
            }
            String checkString = dataCheckString.toString();
            System.out.println("Data-check-string (raw): " + checkString);

            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            byte[] secretKey = sha256.digest(botToken.getBytes(StandardCharsets.UTF_8));
            System.out.println("Secret key generated (hex): " + bytesToHex(secretKey).substring(0, 10) + "...");

            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secretKey, "HmacSHA256"));
            byte[] hashBytes = mac.doFinal(checkString.getBytes(StandardCharsets.UTF_8));
            String computedHash = bytesToHex(hashBytes);
            System.out.println("Computed hash: " + computedHash);
            System.out.println("Received hash:  " + receivedHash);
            System.out.println("Hashes match: " + computedHash.equals(receivedHash));

            return computedHash.equals(receivedHash);

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static Map<String, String> parseRawPairs(String initData) {
        Map<String, String> map = new HashMap<>();
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
        if (json.charAt(start) == '"') {
            int end = json.indexOf('"', start + 1);
            return json.substring(start + 1, end);
        } else {
            int end = json.indexOf(',', start);
            if (end == -1) end = json.indexOf('}', start);
            return json.substring(start, end).trim();
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
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
}