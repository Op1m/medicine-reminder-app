package com.op1m.medrem.backend_api.security;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.*;

public class TelegramInitDataValidator {

    private static final String HMAC_ALGO = "HmacSHA256";

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

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }

    private static String bytesToBase64(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }

    private static String maskToken(String token) {
        if (token == null) return "null";
        if (token.length() < 8) return "****";
        return token.substring(0,4) + "..." + token.substring(token.length()-4);
    }

    public static class DebugResult {
        public final boolean valid;
        public final String message;
        public DebugResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }
    }

    public static DebugResult validateWithLogging(String initData, String botToken) {
        System.out.println("----- TelegramInitDataValidator START -----");
        try {
            System.out.println("botToken preview: " + maskToken(botToken));
            if (initData == null || initData.isBlank()) {
                System.out.println("initData null/blank");
                return new DebugResult(false, "initData missing");
            }

            Map<String,String> decodedParams = parseDecodedParams(initData);
            Map<String,String> rawParams = parseRawPairs(initData);

            System.out.println("decodedParams keys: " + decodedParams.keySet());
            System.out.println("rawParams keys: " + rawParams.keySet());

            String receivedHash = decodedParams.get("hash");
            if (receivedHash == null) {
                System.out.println("No 'hash' param present in decoded params -> invalid");
                return new DebugResult(false, "no hash param");
            }
            System.out.println("received hash param: " + receivedHash);

            Map<String,String> workingDecoded = new HashMap<>(decodedParams);
            workingDecoded.remove("hash");
            workingDecoded.remove("signature");

            List<String> keys = new ArrayList<>(workingDecoded.keySet());
            Collections.sort(keys);
            StringBuilder sbDecoded = new StringBuilder();
            for (int i=0;i<keys.size();i++){
                String k = keys.get(i);
                sbDecoded.append(k).append("=").append(workingDecoded.get(k));
                if (i<keys.size()-1) sbDecoded.append("\n");
            }
            String dataCheckDecoded = sbDecoded.toString();
            System.out.println("data_check_string (decoded values):");
            System.out.println(dataCheckDecoded);

            Map<String,String> workingRaw = new HashMap<>(rawParams);
            workingRaw.remove("hash");
            workingRaw.remove("signature");
            List<String> rkeys = new ArrayList<>(workingRaw.keySet());
            Collections.sort(rkeys);
            StringBuilder sbRaw = new StringBuilder();
            for (int i=0;i<rkeys.size();i++){
                String k = rkeys.get(i);
                sbRaw.append(k).append("=").append(workingRaw.get(k));
                if (i<rkeys.size()-1) sbRaw.append("\n");
            }
            String dataCheckRaw = sbRaw.toString();
            System.out.println("data_check_string (raw values / urlencoded for user):");
            System.out.println(dataCheckRaw);

            List<byte[]> secretCandidates = new ArrayList<>();
            try {
                MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
                byte[] sha = sha256.digest(botToken.getBytes(StandardCharsets.UTF_8));
                System.out.println("candidate secret: sha256(botToken) preview hex: " + bytesToHex(sha).substring(0,12) + "...");
                secretCandidates.add(sha);
            } catch (Exception ex){ ex.printStackTrace(); }

            try {
                Mac mac = Mac.getInstance(HMAC_ALGO);
                mac.init(new SecretKeySpec(botToken.getBytes(StandardCharsets.UTF_8), HMAC_ALGO));
                byte[] candidate = mac.doFinal("WebAppData".getBytes(StandardCharsets.UTF_8));
                System.out.println("candidate secret: HMAC(botToken, 'WebAppData') preview hex: " + bytesToHex(candidate).substring(0,12) + "...");
                secretCandidates.add(candidate);
            } catch (Exception ex){ ex.printStackTrace(); }

            try {
                byte[] plain = botToken.getBytes(StandardCharsets.UTF_8);
                System.out.println("candidate secret: plain(botToken) bytes preview hex: " + bytesToHex(Arrays.copyOf(plain, Math.min(12, plain.length))) + "...");
                secretCandidates.add(plain);
            } catch (Exception ex){ ex.printStackTrace(); }

            for (int s=0; s<secretCandidates.size(); s++){
                byte[] secret = secretCandidates.get(s);
                System.out.println("Trying secret candidate #" + (s+1) + " preview hex: " + bytesToHex(Arrays.copyOf(secret, Math.min(secret.length, 12))) + "...");
                for (int t=0; t<2; t++){
                    String variant = (t==0) ? "decoded" : "raw";
                    String dataCheck = (t==0) ? dataCheckDecoded : dataCheckRaw;
                    try {
                        Mac mac2 = Mac.getInstance(HMAC_ALGO);
                        mac2.init(new SecretKeySpec(secret, HMAC_ALGO));
                        byte[] h = mac2.doFinal(dataCheck.getBytes(StandardCharsets.UTF_8));
                        String hex = bytesToHex(h);
                        String b64 = bytesToBase64(h);
                        System.out.println("computed ("+variant+") hex: " + hex);
                        System.out.println("computed ("+variant+") base64: " + b64);
                        if (hex.equalsIgnoreCase(receivedHash)) {
                            System.out.println("MATCH found: hex equals receivedHash (variant="+variant+", secretCandidate="+(s+1)+")");
                            return new DebugResult(true, "ok");
                        }
                        if (b64.equals(receivedHash)) {
                            System.out.println("MATCH found: base64 equals receivedHash (variant="+variant+", secretCandidate="+(s+1)+")");
                            return new DebugResult(true, "ok");
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }

            System.out.println("NO MATCH (all attempts failed)");
            return new DebugResult(false, "no match");
        } catch (Exception e) {
            e.printStackTrace();
            return new DebugResult(false, "exception");
        } finally {
            System.out.println("----- TelegramInitDataValidator END -----");
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
