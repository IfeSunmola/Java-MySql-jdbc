package utilities;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

import java.io.IOException;

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
    private static String getVerificationCode() {
        StringBuilder result = new StringBuilder();
        int min = 0, max = 9;
        for (int i = 0; i < 5; i++) {
            result.append((int) Math.floor(Math.random() * (max - min + 1) + min));
        }
        return result.toString();
    }

    public static String sendVerificationCode(String userPhoneNumber) {
        final String ACCOUNT_SID = System.getenv("TWILIO_ACCOUNT_SID");
        final String AUTH_TOKEN = System.getenv("TWILIO_AUTH_TOKEN");
        final String PHONE_NUMBER = System.getenv("TWILIO_PHONE_NUMBER");
        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);

        String code = getVerificationCode(); // generate verification code to send to the user

        Message message = Message.creator(
                new PhoneNumber(userPhoneNumber),
                new PhoneNumber(PHONE_NUMBER),
                "Verification code is: " + code
        ).create(); // send the code
        return code;
    }

    // misc change later
    public static void clearScreen() throws IOException, InterruptedException {
        final String os = System.getProperty("os.name");
        if (os.contains("Windows"))
            new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
        else
            Runtime.getRuntime().exec("clear");
    }
}
