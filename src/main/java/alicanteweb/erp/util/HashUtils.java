package alicanteweb.erp.util;

import alicanteweb.erp.exception.ErpException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public final class HashUtils {
    private static final String ALGORITHM = "SHA-256";

    private HashUtils() {}

    public static byte[] sha256(byte[] input) {
        try {
            MessageDigest digest = MessageDigest.getInstance(ALGORITHM);
            return digest.digest(input);
        } catch (NoSuchAlgorithmException e) {
            throw new ErpException("Algoritmo SHA-256 no disponible", e);
        }
    }

    public static byte[] sha256(String input) {
        return sha256(input.getBytes(StandardCharsets.UTF_8));
    }

    public static String sha256Base64(String input) {
        return Base64.getEncoder().encodeToString(sha256(input));
    }

    public static String sha256Base64(byte[] input) {
        return Base64.getEncoder().encodeToString(sha256(input));
    }

    public static String sha256Base64UrlSafe(String input) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(sha256(input));
    }

    public static String sha256Base64UrlSafe(byte[] input) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(sha256(input));
    }

    public static String sha256Hex(String input) {
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
