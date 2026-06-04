package alicanteweb.erp.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

/**
 * Utilidades de hashing (SHA-256) y codificación.
 */
public final class HashUtils {
    private static final String ALGORITHM = "SHA-256";

    private HashUtils() {}

    public static byte[] sha256(byte[] input) throws Exception {
        MessageDigest digest = MessageDigest.getInstance(ALGORITHM);
        return digest.digest(input);
    }

    public static byte[] sha256(String input) throws Exception {
        return sha256(input.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * SHA-256 + Base64 (standard)
     */
    public static String sha256Base64(String input) throws Exception {
        byte[] hash = sha256(input);
        return Base64.getEncoder().encodeToString(hash);
    }

    /**
     * SHA-256 + Base64 (standard) from bytes
     */
    public static String sha256Base64(byte[] input) throws Exception {
        byte[] hash = sha256(input);
        return Base64.getEncoder().encodeToString(hash);
    }

    /**
     * SHA-256 + Base64 URL-safe (no padding) from String
     */
    public static String sha256Base64UrlSafe(String input) throws Exception {
        byte[] hash = sha256(input);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
    }

    /**
     * SHA-256 + Base64 URL-safe (no padding) from bytes
     */
    public static String sha256Base64UrlSafe(byte[] input) throws Exception {
        byte[] hash = sha256(input);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
    }

    /**
     * Hex representation (lowercase)
     */
    public static String sha256Hex(String input) throws Exception {
        byte[] hash = sha256(input);
        return toHex(hash);
    }

    public static String sha256Hex(byte[] input) throws Exception {
        byte[] hash = sha256(input);
        return toHex(hash);
    }

    private static String toHex(byte[] hash) {
        StringBuilder sb = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
