package com.op1m.medrem.backend_api.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import java.util.Base64;

public class TelegramInitDataValidator {

    private static final String HMAC_ALGO = "HmacSHA256";
    private static final ObjectMapper mapper = new ObjectMapper();

    public static boolean validateInitData(String initData, String botToken) {
        try {
            if (initData == null || botToken == null || botToken.isBlank()) {
                System.out.println("TelegramInitDataValidator: missing initData or botToken");
                return false;
            }

            String token = botToken.trim();

            Map<String, String> decoded = parseDecodedParams(initData);
            Map<String, String> raw = parseRawPairs(initData);

            String receivedHash = decoded.getOrDefault("hash", raw.get("hash"));
            if (receivedHash == null) {
                System.out.println("TelegramInitDataValidator: no hash param found");
                return false;
            }

            List<Map<String, String>> candidates = new ArrayList<>();
            candidates.add(new HashMap<>(decoded));
            candidates.add(new HashMap<>(raw));

            byte[] secretSha = MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8));
            byte[] secretPlain = token.getBytes(StandardCharsets.UTF_8);

            List<byte[]> secrets = List.of(secretSha, secretPlain);
            List<String> secretNames = List.of("sha256(token)", "plain(tokenBytes)");

            for (int mi = 0; mi < candidates.size(); mi++) {
                Map<String, String> m = candidates.get(mi);
                m.remove("signature");
                String hashFromMap = m.remove("hash");

                List<String> keys = new ArrayList<>(m.keySet());
                Collections.sort(keys);

                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < keys.size(); i++) {
                    String k = keys.get(i);
                    String v = m.get(k);
                    if (v == null) v = "";
                    sb.append(k).append("=").append(v);
                    if (i < keys.size() - 1) sb.append("\n");
                }
                String dataCheckString = sb.toString();

                for (int si = 0; si < secrets.size(); si++) {
                    byte[] secret = secrets.get(si);
                    String secretLabel = secretNames.get(si);

                    Mac mac = Mac.getInstance(HMAC_ALGO);
                    mac.init(new SecretKeySpec(secret, HMAC_ALGO));
                    byte[] hmac = mac.doFinal(dataCheckString.getBytes(StandardCharsets.UTF_8));


                    String hex = bytesToHex(hmac);
                    String b64 = Base64.getEncoder().encodeToString(hmac);

                    String shortSecret = (secretLabel.equals("sha256(token)") && secret.length > 4)
                            ? bytesToHex(secret).substring(0, 10) + "..."
                            : "<plain-token-bytes>";
                    System.out.println("TelegramInitDataValidator TRY: candidate=" + (mi==0 ? "decoded" : "raw")
                            + " secret=" + secretLabel + " secretPreview=" + shortSecret);
                    System.out.println("data_check_string:\n" + dataCheckString);
                    System.out.println("computed hex: " + hex);
                    System.out.println("computed base64: " + b64);
                    System.out.println("received hash param: " + receivedHash);

                    if (hex.equalsIgnoreCase(receivedHash)) {
                        System.out.println("TelegramInitDataValidator: MATCH on hex with " + secretLabel + " using " + (mi==0 ? "decoded" : "raw"));
                        return true;
                    }
                    if (b64.equals(receivedHash)) {
                        System.out.println("TelegramInitDataValidator: MATCH on base64 with " + secretLabel + " using " + (mi==0 ? "decoded" : "raw"));
                        return true;
                    }
                    if (hex.equalsIgnoreCase(receivedHash.trim())) {
                        System.out.println("TelegramInitDataValidator: MATCH (trimmed) on hex");
                        return true;
                    }
                }
            }

            System.out.println("TelegramInitDataValidator: NO MATCH (all attempts failed)");
            return false;

        } catch (Exception ex) {
            ex.printStackTrace();
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

    public static Map<String, String> parseRawPairs(String initData) {
        Map<String, String> map = new HashMap<>();
        if (initData == null) return map;
        String[] pairs = initData.split("&");
        for (String p: pairs) {
            int idx = p.indexOf('=');
            if (idx > 0) {
                String k = p.substring(0, idx);
                String v = p.substring(idx + 1);
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
            String first = node.has("first_name") ? node.get("first_name").asText("") : "";
            String last = node.has("last_name") ? node.get("last_name").asText("") : "";
            String uname = node.has("username") ? node.get("username").asText("") : "";
            String photo = node.has("photo_url") ? node.get("photo_url").asText("") : "";
            if (id == -1) return null;
            return new TelegramUserData(id, first, last, uname, photo);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length*2);
        for (byte b: bytes) sb.append(String.format("%02x", b));
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
