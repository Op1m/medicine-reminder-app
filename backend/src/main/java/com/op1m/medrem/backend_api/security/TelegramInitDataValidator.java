package com.op1m.medrem.backend_api.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

public class TelegramInitDataValidator {

    private static final String HMAC_ALGO = "HmacSHA256";
    private static final ObjectMapper mapper = new ObjectMapper();

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

            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            byte[] secretKey = sha256.digest(botToken.getBytes(StandardCharsets.UTF_8));

            Mac mac = Mac.getInstance(HMAC_ALGO);
            mac.init(new SecretKeySpec(secretKey, HMAC_ALGO));
            byte[] hmac = mac.doFinal(dataCheckString.getBytes(StandardCharsets.UTF_8));

            String computedHex = bytesToHex(hmac);

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
        for (String pair : pairs) {
            int idx = pair.indexOf('=');
            if (idx > 0) {
                String k = URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8);
                String v = URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8);
                map.put(k, v);
            }
        }
        return map;
    }

    public static TelegramUserData extractUser(Map<String, String> decodedParams) {
        try {
            if (decodedParams == null) return null;
            String userJson = decodedParams.get("user");
            if (userJson == null) return null;

            JsonNode node = mapper.readTree(userJson);
            long id = node.has("id") ? node.get("id").asLong() : -1;
            String firstName = node.has("first_name") ? node.get("first_name").asText("") : "";
            String lastName = node.has("last_name") ? node.get("last_name").asText("") : "";
            String username = node.has("username") ? node.get("username").asText("") : "";
            String photoUrl = node.has("photo_url") ? node.get("photo_url").asText("") : "";

            if (id == -1) return null;
            return new TelegramUserData(id, firstName, lastName, username, photoUrl);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
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

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }
}
