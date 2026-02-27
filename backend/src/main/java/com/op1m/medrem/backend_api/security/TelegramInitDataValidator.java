package com.op1m.medrem.backend_api.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import java.util.stream.Collectors;

public final class TelegramInitDataValidator {

    private static final Logger log = LoggerFactory.getLogger(TelegramInitDataValidator.class);

    private TelegramInitDataValidator() {}

    public static DebugInfo validateWithDebug(Map<String, String> initDataRaw, String botToken) {
        DebugInfo dbg = new DebugInfo();
        if (initDataRaw == null) {
            dbg.ok = false;
            dbg.error = "initData map is null";
            return dbg;
        }
        Map<String, String> initData = new HashMap<>(initDataRaw);

        if (initData.size() == 1 && initData.containsKey("initData")) {
            String raw = initData.get("initData");
            initData.clear();
            if (raw != null) {
                String[] pairs = raw.split("&");
                for (String p : pairs) {
                    int idx = p.indexOf('=');
                    if (idx <= 0) continue;
                    String k = urlDecodeSafe(p.substring(0, idx));
                    String v = urlDecodeSafe(p.substring(idx + 1));
                    initData.put(k, v);
                }
            }
            dbg.note = "unpacked initData string into params";
        }

        dbg.receivedKeys = new ArrayList<>(initData.keySet());
        String providedHash = initData.get("hash");
        dbg.providedHash = providedHash == null ? null : providedHash.trim();

        if (providedHash == null || providedHash.isEmpty()) {
            dbg.ok = false;
            dbg.error = "missing hash";
            return dbg;
        }

        Map<String, String> copy = new HashMap<>(initData);
        copy.remove("hash");

        List<String> partsRaw = copy.entrySet().stream()
                .filter(e -> e.getValue() != null)
                .map(e -> e.getKey() + "=" + e.getValue())
                .sorted()
                .collect(Collectors.toList());

        dbg.parts = partsRaw;

        List<String> normalizedParts = new ArrayList<>(partsRaw.size());
        for (String kv : partsRaw) {
            int idx = kv.indexOf('=');
            String k = kv.substring(0, idx);
            String v = kv.substring(idx + 1);
            String normalized = normalizeValueForComparison(v);
            normalizedParts.add(k + "=" + normalized);
        }

        dbg.normalizedParts = normalizedParts;
        String dataCheckString = String.join("\n", normalizedParts);
        dbg.dataCheckString = dataCheckString;

        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            byte[] secretKey = sha256.digest(botToken.getBytes(StandardCharsets.UTF_8));
            dbg.secretKeyHex = bytesToHexLower(secretKey);

            Mac hmac = Mac.getInstance("HmacSHA256");
            hmac.init(new SecretKeySpec(secretKey, "HmacSHA256"));
            byte[] hmacBytes = hmac.doFinal(dataCheckString.getBytes(StandardCharsets.UTF_8));
            dbg.calcHex = bytesToHexLower(hmacBytes);

            String providedLower = providedHash.toLowerCase(Locale.ROOT);
            dbg.ok = constantTimeEquals(dbg.calcHex, providedLower);

            if (!dbg.ok) {
                dbg.error = "hash_mismatch";
                return dbg;
            }

            if (copy.containsKey("auth_date")) {
                try {
                    long auth = Long.parseLong(copy.get("auth_date"));
                    long now = System.currentTimeMillis() / 1000L;
                    if (Math.abs(now - auth) > 86400L) {
                        dbg.ok = false;
                        dbg.error = "auth_date_expired";
                    }
                } catch (NumberFormatException ex) {
                    dbg.ok = false;
                    dbg.error = "invalid_auth_date";
                }
            }

            return dbg;
        } catch (Exception e) {
            dbg.ok = false;
            dbg.error = "exception: " + e.getMessage();
            log.debug("validator exception", e);
            return dbg;
        }
    }

    public static boolean validate(Map<String, String> initData, String botToken) throws Exception {
        DebugInfo d = validateWithDebug(initData, botToken);
        if (!d.ok) throw new Exception("telegram init data invalid: " + (d.error != null ? d.error : "hash mismatch"));
        return true;
    }

    private static String normalizeValueForComparison(String v) {
        if (v == null) return "";
        String s = v;
        if ((s.startsWith("{") && s.endsWith("}")) || (s.startsWith("[") && s.endsWith("]"))) {
            s = s.replace("\\/", "/");
            s = s.replace("\r", "");
        }
        return s;
    }

    private static String urlDecodeSafe(String s) {
        try {
            return URLDecoder.decode(s, StandardCharsets.UTF_8.name());
        } catch (Exception e) {
            return s;
        }
    }

    private static String bytesToHexLower(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) sb.append(String.format("%02x", b & 0xff));
        return sb.toString();
    }

    private static boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) return false;
        if (a.length() != b.length()) return false;
        int r = 0;
        for (int i = 0; i < a.length(); i++) r |= a.charAt(i) ^ b.charAt(i);
        return r == 0;
    }

    public static final class DebugInfo {
        public boolean ok;
        public String error;
        public String note;
        public List<String> receivedKeys;
        public List<String> parts;
        public List<String> normalizedParts;
        public String dataCheckString;
        public String providedHash;
        public String calcHex;
        public String secretKeyHex;
    }
}
