import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static utilities.DatabaseUtil.*;
import static utilities.UserInputUtil.*;
import static utilities.ValidateUtil.getVerificationCode;

/*
 * Todo:
 *  create account/log in with github
 *  don't let the user have to keep logging in everytime
 *  change gender to show all options
 *  move createUsersTable to private class in DatabaseUtil
 *  login by password
 *  name should be in format Firstname
 * */

/**
 * Main/Driver class for local server
 *
 * @author Ife Sunmola
 */
public class Main {
    public static void main(String[] args) {
        System.out.println(mainMenu());
        // no need to close inputReader and connection since the try is used like this
        try (BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in)); Connection connection = getConnection()) {
            if (connection == null) {
                System.out.println("Connection failed.");
                return;
            }
            createUsersTable(connection);
            boolean selectionWasValid = false;
            String userInput;

            while (!selectionWasValid) {
                System.out.print("Your response: ");
                userInput = inputReader.readLine().strip();
                switch (userInput) {
                    case "1" -> {
                        createAccount(inputReader, connection);
                        selectionWasValid = true;
                    }
                    case "2" -> {
                        login(inputReader, connection);
                        selectionWasValid = true;
                    }
                    case "3" -> {
                        deleteAccount(inputReader, connection);
                        selectionWasValid = true;
                    }
                    case "0" -> {
                        System.out.println("Have a nice day");
                        selectionWasValid = true;
                    }
                    default -> System.out.println("Make a valid selection");
                }
            }
        }
        catch (IOException | SQLException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Method to create an account for a user. The account will not be created if an account has already been
     * created with the same phone number.<br> The age will be calculated in the database, rather than directly in the
     * code
     *
     * @param inputReader to get the user input
     * @param connection  the connection to the database
     * @throws IOException  if the user input could not be read
     * @throws SQLException if there was a problem with the sql server itself
     */
    private static void createAccount(BufferedReader inputReader, Connection connection) throws IOException, SQLException {
        System.out.println("** Creating an account **");
        String name = getName(inputReader);
        System.out.println("--------------");
        String dateOfBirth = getDateOfBirth(inputReader);
        System.out.println("--------------");
        String phoneNumber = getPhoneNumber(inputReader);
        System.out.println("--------------");
        String gender = getGenderIdentity(inputReader);
        String timeRegistered = getCurrentTime();
        String dateRegistered = getCurrentDate();

        if (!numberExistsInDB(phoneNumber, connection)) {// the user does not have an account, create one
            PreparedStatement addUser = connection.prepareStatement(
                    "INSERT INTO users_table " +
                            "(phone_number, user_name, date_of_birth, age, gender, date_of_reg, time_of_reg) " +
                            "VALUES('" + phoneNumber + "', '" + name + "', '" + dateOfBirth + "', TIMESTAMPDIFF(YEAR, date_of_birth, CURDATE()), " +
                            "'" + gender + "', '" + dateRegistered + "', '" + timeRegistered + "');");
            // executeUpdate returns the amount of rows that was updated
            if (addUser.executeUpdate() == 1) {// the account was created if the number of rows updated is 1
                System.out.println("------------------------------------------");
                System.out.println("Account created Successfully");
                System.out.println("------------------------------------------");
            }
            else {// failed
                // shouldn't happen but just in case
                System.err.println("Account could not be created (executeUpdate returned number != 1)");
            }
        }
        else {
            System.out.println("You already have an account. Log in instead.");
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
    private static void login(BufferedReader inputReader, Connection connection) throws IOException, SQLException {
        System.out.println("** Login to an existing account **");
        String userPhoneNumber = getPhoneNumber(inputReader);// ask for the user's phone number to log in

        if (numberExistsInDB(userPhoneNumber, connection)) { // user has an account
            LocalDateTime lastLoginTime = getLastLoginTime(connection, userPhoneNumber);
            long elapsed = ChronoUnit.MINUTES.between(lastLoginTime, LocalDateTime.now());

            if (elapsed >= 720) {
                System.out.println("Your session has timed out, log in again");
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
            else {
                System.out.println("Still in session, no need to log in.");

            }


        }
        else { // user does not have an account
            System.out.println("Account not found. Log in failed");
        }
    }

    /**
     * Method to allow the user to delete their account.
     *
     * @param inputReader to get the user input
     * @param connection  the connection to the database
     * @throws IOException  if the user input could not be read
     * @throws SQLException if there was a problem with the sql server itself
     */
    private static void deleteAccount(BufferedReader inputReader, Connection connection) throws IOException, SQLException {
        System.out.println("** Deleting an account **");
        String phoneNumber = getPhoneNumber(inputReader);// get account to delete

        if (numberExistsInDB(phoneNumber, connection)) {// there is an account to delete
            String userInput = "";
            while (!userInput.equals("Y") && !userInput.equals("N")) {// confirm if the user wants to delete their account
                System.out.println("YOUR ACCOUNT CANNOT BE RECOVERED AFTER DELETION");
                System.out.print("Are you sure you want to delete your account? This process is IRREVERSIBLE (y/n): ");
                userInput = inputReader.readLine().toUpperCase().strip();
            }
            if (userInput.equals("Y")) {// delete the account if confirmed
                PreparedStatement deleteUser = connection.prepareStatement(
                        "DELETE FROM users_table WHERE phone_number = '" + phoneNumber + "';");
                // executeUpdate returns the amount of rows that was updated
                if (deleteUser.executeUpdate() == 1) {// the account was deleted if the number of rows updated is 1
                    System.out.println("Account deleted successfully");
                }
                else {
                    // shouldn't happen but just in case
                    System.err.println("Account could not be deleted (executeUpdate returned number != 1)");
                }
            }
            else {// not confirmed, don't delete the account
                System.out.println("Account not deleted");
            }
        }
        else {// there is no account to delete
            System.out.println("Account not found. Delete failed");
        }
    }

}
