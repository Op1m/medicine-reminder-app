package com.op1m.medrem.backend_api.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public final class TelegramInitDataValidator {

    private static final Logger log = LoggerFactory.getLogger(TelegramInitDataValidator.class);
    private static final String WEBAPP_DATA_KEY_LITERAL = "WebAppData";

    private TelegramInitDataValidator() {}

    public static DebugInfo validateWithDebug(Map<String, String> initDataRaw, String botToken) {
        DebugInfo dbg = new DebugInfo();
        if (initDataRaw == null) {
            dbg.ok = false;
            dbg.error = "initData map is null";
            return dbg;
        }

        String originalRawString = null;
        Map<String, String> params = new HashMap<>(initDataRaw);

        if (params.size() == 1 && params.containsKey("initData")) {
            originalRawString = params.get("initData");
            params.clear();
            if (originalRawString != null) {
                String[] pairs = originalRawString.split("&");
                for (String p : pairs) {
                    int idx = p.indexOf('=');
                    if (idx <= 0) continue;
                    String k = urlDecodeSafe(p.substring(0, idx));
                    String vDecoded = urlDecodeSafe(p.substring(idx + 1));
                    params.put(k, vDecoded);
                    dbg.rawPairs.put(k, p.substring(idx + 1));
                    dbg.decodedPairs.put(k, vDecoded);
                }
            }
            dbg.note = "unpacked initData string into params";
            dbg.originalRawString = originalRawString;
        } else {
            for (Map.Entry<String, String> e : params.entrySet()) dbg.decodedPairs.put(e.getKey(), e.getValue());
        }

        dbg.receivedKeys = new ArrayList<>(params.keySet());
        String providedHash = params.get("hash");
        dbg.providedHash = providedHash == null ? null : providedHash.trim();
        if (providedHash == null || providedHash.isEmpty()) {
            dbg.ok = false;
            dbg.error = "missing hash";
            return dbg;
        }

        Map<String, String> copy = new HashMap<>(params);
        copy.remove("hash");
        List<String> parts = copy.entrySet().stream()
                .filter(e -> e.getValue() != null)
                .map(e -> e.getKey() + "=" + e.getValue())
                .sorted()
                .collect(Collectors.toList());

        dbg.parts = parts;
        String dataCheckString = String.join("\n", parts);
        dbg.dataCheckString = dataCheckString;

        try {
            Mac mac1 = Mac.getInstance("HmacSHA256");
            mac1.init(new SecretKeySpec(WEBAPP_DATA_KEY_LITERAL.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] secretKey = mac1.doFinal(botToken.getBytes(StandardCharsets.UTF_8));
            dbg.secretKeyHex = bytesToHexLower(secretKey);

            Mac mac2 = Mac.getInstance("HmacSHA256");
            mac2.init(new SecretKeySpec(secretKey, "HmacSHA256"));
            byte[] calc = mac2.doFinal(dataCheckString.getBytes(StandardCharsets.UTF_8));
            String calcHex = bytesToHexLower(calc);
            dbg.calcHex = calcHex;

            String providedLower = providedHash.toLowerCase(Locale.ROOT);
            dbg.ok = constantTimeEquals(calcHex, providedLower);

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
        public String originalRawString;
        public List<String> receivedKeys;
        public List<String> parts;
        public String dataCheckString;
        public String providedHash;
        public String calcHex;
        public String secretKeyHex;
        public Map<String, String> rawPairs = new LinkedHashMap<>();
        public Map<String, String> decodedPairs = new LinkedHashMap<>();
    }
}
