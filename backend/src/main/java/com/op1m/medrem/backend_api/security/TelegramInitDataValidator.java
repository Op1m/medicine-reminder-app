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
                    String vRaw = p.substring(idx + 1);
                    String vDecoded = urlDecodeSafe(vRaw);
                    params.put(k, vDecoded);
                    dbg.rawPairs.put(k, vRaw);
                    dbg.decodedPairs.put(k, vDecoded);
                }
            }
            dbg.note = "unpacked initData string into params";
            dbg.originalRawString = originalRawString;
        } else {
            for (Map.Entry<String, String> e : params.entrySet()) {
                dbg.decodedPairs.put(e.getKey(), e.getValue());
            }
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

        List<String> keys = copy.keySet().stream().sorted().collect(Collectors.toList());

        List<String> baseParts = keys.stream().map(k -> k + "=" + (copy.get(k) == null ? "" : copy.get(k))).collect(Collectors.toList());
        dbg.parts = baseParts;
        dbg.dataCheckString = String.join("\n", baseParts);

        List<Candidate> candidates = new ArrayList<>();

        {
            List<String> parts = new ArrayList<>();
            for (String k : keys) parts.add(k + "=" + normalizeValueForComparison(copy.get(k)));
            candidates.add(new Candidate("decoded_normalized", parts));
        }

        {
            List<String> parts = new ArrayList<>();
            for (String k : keys) {
                String v = copy.get(k);
                if (looksLikeJson(v)) v = escapeJsonSlashes(v);
                parts.add(k + "=" + v);
            }
            candidates.add(new Candidate("decoded_with_escaped_slashes", parts));
        }

        if (dbg.rawPairs.size() > 0) {
            List<String> parts = new ArrayList<>();
            for (String k : keys) {
                String raw = dbg.rawPairs.get(k);
                if (raw == null) raw = urlEncodeSafe(copy.get(k));
                parts.add(k + "=" + raw);
            }
            candidates.add(new Candidate("raw_percent_encoded", parts));
        }

        {
            List<String> parts = new ArrayList<>();
            for (String k : keys) {
                String v = copy.get(k);
                if (v != null) v = v.replace("\\\"", "\"").replace("\\/", "/");
                parts.add(k + "=" + (v == null ? "" : v));
            }
            candidates.add(new Candidate("decoded_unescape_backslashes", parts));
        }

        {
            List<String> parts = new ArrayList<>();
            for (String k : keys) {
                String v = copy.get(k);
                if (k.equals("user") && v != null && v.contains("photo_url")) {
                    v = v.replace("https://", "https:\\/\\/");
                }
                parts.add(k + "=" + (v == null ? "" : v));
            }
            candidates.add(new Candidate("decoded_photourl_escaped", parts));
        }

        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            byte[] secretKey = sha256.digest(botToken.getBytes(StandardCharsets.UTF_8));
            dbg.secretKeyHex = bytesToHexLower(secretKey);

            Mac hmac = Mac.getInstance("HmacSHA256");
            hmac.init(new SecretKeySpec(secretKey, "HmacSHA256"));

            for (Candidate c : candidates) {
                String dcs = String.join("\n", c.parts);
                byte[] mac = hmac.doFinal(dcs.getBytes(StandardCharsets.UTF_8));
                String hex = bytesToHexLower(mac);
                c.calcHex = hex;
                c.dataCheckString = dcs;
                boolean match = constantTimeEquals(hex, dbg.providedHash.toLowerCase(Locale.ROOT));
                c.match = match;
                dbg.attempts.add(c);
                if (match) {
                    dbg.ok = true;
                    dbg.matchedCandidate = c.name;
                    dbg.calcHex = hex;
                    dbg.dataCheckString = dcs;
                    break;
                }
            }

            if (!dbg.ok) {
                dbg.ok = false;
                dbg.error = "hash_mismatch";
                dbg.calcHex = dbg.attempts.isEmpty() ? null : dbg.attempts.get(0).calcHex;
            } else {
                // auth_date check
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

    private static boolean looksLikeJson(String s) {
        return s != null && ((s.startsWith("{") && s.endsWith("}")) || (s.startsWith("[") && s.endsWith("]")));
    }

    private static String escapeJsonSlashes(String s) {
        if (s == null) return s;
        return s.replace("/", "\\/");
    }

    private static String normalizeValueForComparison(String v) {
        if (v == null) return "";
        String s = v;
        if (looksLikeJson(s)) {
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

    private static String urlEncodeSafe(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder();
        for (byte b : s.getBytes(StandardCharsets.UTF_8)) {
            int v = b & 0xff;
            if ((v >= 0x30 && v <= 0x39) || (v >= 0x41 && v <= 0x5A) || (v >= 0x61 && v <= 0x7A) || v == 0x2D || v == 0x2E || v == 0x5F || v == 0x7E) {
                sb.append((char) v);
            } else {
                sb.append(String.format("%%%02X", v));
            }
        }
        return sb.toString();
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
        public List<Candidate> attempts = new ArrayList<>();
        public String matchedCandidate;
        public Map<String, String> rawPairs = new LinkedHashMap<>();
        public Map<String, String> decodedPairs = new LinkedHashMap<>();
    }

    public static final class Candidate {
        public String name;
        public List<String> parts;
        public String calcHex;
        public boolean match;
        public String dataCheckString;
        public Candidate(String name, List<String> parts) { this.name = name; this.parts = parts; }
        @Override public String toString() { return "Candidate{" + name + ", match=" + match + ", calcHex=" + calcHex + "}"; }
    }
}
