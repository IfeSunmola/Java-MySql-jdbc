package utilities;

/**
 * This class contains all the methods that will be used to validate the user
 *
 * @author Ife Sunmola
 */
public final class ValidateUtil {
    /**
     * Method to generate a 5 number verification code
     *
     * @return String containing a random 5 digits code
     */
    public static String getVerificationCode() {
        StringBuilder result = new StringBuilder();
        int min = 0, max = 9;
        for (int i = 0; i < 5; i++) {
            result.append((int) Math.floor(Math.random() * (max - min + 1) + min));
        }
        return result.toString();
    }
}
