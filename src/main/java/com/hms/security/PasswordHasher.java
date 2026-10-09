package com.hms.security;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Stores passwords safely using PBKDF2 (built into Java, no extra library).
 *
 * <p>A password is never stored. Instead we store a random salt and the result of running
 * the password + salt through HMAC-SHA256 hundreds of thousands of times. The salt makes
 * equal passwords hash differently; the repetitions make guessing slow for an attacker.
 *
 * <p>Stored format: {@code pbkdf2$<iterations>$<salt>$<hash>} (salt and hash in Base64).
 * Keeping the iteration count in the string means it can be raised later without
 * breaking old passwords.
 */
public final class PasswordHasher {

    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final String PREFIX = "pbkdf2";
    /** OWASP's recommended count for PBKDF2-HMAC-SHA256; about 75 ms per login on a laptop. */
    public static final int DEFAULT_ITERATIONS = 600_000;
    private static final int SALT_BYTES = 16;
    private static final int HASH_BITS = 256;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final int iterations;

    public PasswordHasher() {
        this(DEFAULT_ITERATIONS);
    }

    /** Tests use a small iteration count so they run fast. */
    public PasswordHasher(int iterations) {
        if (iterations < 1) {
            throw new IllegalArgumentException("iterations must be positive");
        }
        this.iterations = iterations;
    }

    public String hash(char[] password) {
        byte[] salt = new byte[SALT_BYTES];
        RANDOM.nextBytes(salt);
        byte[] hash = pbkdf2(password, salt, iterations, HASH_BITS);
        Base64.Encoder base64 = Base64.getEncoder();
        return PREFIX + "$" + iterations + "$" + base64.encodeToString(salt) + "$" + base64.encodeToString(hash);
    }

    /** Re-hashes the attempt with the stored salt and compares. Malformed input simply fails. */
    public boolean verify(char[] password, String stored) {
        if (stored == null) {
            return false;
        }
        String[] parts = stored.split("\\$");
        if (parts.length != 4 || !parts[0].equals(PREFIX)) {
            return false;
        }
        try {
            int storedIterations = Integer.parseInt(parts[1]);
            byte[] salt = Base64.getDecoder().decode(parts[2]);
            byte[] expected = Base64.getDecoder().decode(parts[3]);
            if (storedIterations < 1 || expected.length == 0) {
                return false;
            }
            byte[] actual = pbkdf2(password, salt, storedIterations, expected.length * 8);
            // Constant-time comparison: the time taken doesn't reveal how many bytes matched.
            return MessageDigest.isEqual(expected, actual);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static byte[] pbkdf2(char[] password, byte[] salt, int iterations, int bits) {
        PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, bits);
        try {
            return SecretKeyFactory.getInstance(ALGORITHM).generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(ALGORITHM + " is not available in this Java runtime", e);
        } finally {
            spec.clearPassword();
        }
    }

    /** Prints a hash for a password: {@code java -cp target/hms.jar com.hms.security.PasswordHasher <password>}. */
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: PasswordHasher <password>");
            System.exit(2);
        }
        System.out.println(new PasswordHasher().hash(args[0].toCharArray()));
    }
}
