package utilities;

/**
 * Contains all the methods that will be used in validating a user
 */
public final class ValidateUtil {
    public static String getVerificationCode() {
        StringBuilder result = new StringBuilder();
        int min = 0, max = 9;
        for (int i = 0; i < 5; i++) {
            result.append((int) Math.floor(Math.random() * (max - min + 1) + min));
        }
        return result.toString();
    }
}
