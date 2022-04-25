import java.io.BufferedReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import static utilities.UserInputUtil.*;
import static utilities.ValidateUtil.sendVerificationCode;
import static utilities.Helpers.*;

public class Login {
    private static final int MAX_ELAPSED_MINUTES = 720;

    private static LocalDateTime getLastLoginTime(Connection connection, String userPhoneNumber) throws SQLException {
        PreparedStatement getLastLoginTime = connection.prepareStatement(
                "SELECT last_login_time FROM users_table WHERE phone_number= '" + userPhoneNumber + "';");
        ResultSet result = getLastLoginTime.executeQuery();
        LocalDateTime lastTime = null;
        if (result.next()) {
            String temp = result.getString("last_login_time");
            lastTime = LocalDateTime.parse(temp, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
        return lastTime;
    }

    private static boolean sessionTimedOut(Connection connection, String userPhoneNumber) throws SQLException {
        // returns true if the session has timed out and false if not
        // session has timed out if 720 minutes (12 hours)  has passed since the last login time
        LocalDateTime lastLoginTime = getLastLoginTime(connection, userPhoneNumber);
        long elapsed = ChronoUnit.MINUTES.between(lastLoginTime, LocalDateTime.now());
        return elapsed >= MAX_ELAPSED_MINUTES;
    }

    private static void doLogin(BufferedReader inputReader, Connection connection, String userPhoneNumber) throws SQLException, IOException {
        System.out.println("Your session has timed out, log in again");
        String code = sendVerificationCode(userPhoneNumber); //returns the verification code that was sent

        System.out.print("Enter the verification code that was sent: ");
        String userCode = inputReader.readLine().strip();

        if (userCode.equals(code)) {
            System.out.println("Account found, Log in successful");
            PreparedStatement setLastLoginTime = connection.prepareStatement(
                    "UPDATE users_table SET last_login_time= '" + getCurrentDate() + " " + getCurrentTime() +
                            "' WHERE phone_number='" + userPhoneNumber + "'");
            setLastLoginTime.executeUpdate();
        }
        else {
            System.out.println("Wrong code. Log in failed.");
        }
    }

    /**
     * Method to allow an existing user to log in to their account. A verification code will be sent to the user's
     * phone number with twilio. todo: log in with github, login with password, don't let user keep logging in everytime
     *
     * @param inputReader to get the user input
     * @param connection  the connection to the database
     * @throws IOException  if the user input could not be read
     * @throws SQLException if there was a problem with the sql server itself
     */
    public static void runLogin(BufferedReader inputReader, Connection connection) throws IOException, SQLException {
        System.out.println("** Login to an existing account **");
        String userPhoneNumber = getPhoneNumber(inputReader);// ask for the user's phone number to log in

        if (numberExistsInDB(userPhoneNumber, connection)) { // user has an account
            if (sessionTimedOut(connection, userPhoneNumber)) { // session has timed out
                doLogin(inputReader, connection, userPhoneNumber);// log the user in
            }
            else {// session has NOT timed out
                System.out.println("Still in session, no need to log in.");
            }
        }
        else { // user does not have an account
            System.out.println("Account not found. Log in failed");
        }
    }
}
