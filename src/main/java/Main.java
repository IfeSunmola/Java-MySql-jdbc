import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.*;

import static utilities.Util.*;

/*
 * Todo:
 *  add error checking, validations when creating accounts
 *  create account/log in with github
 *  don't let the user have to keep logging in everytime
 *  strip names and user input
 *  change gender to show all options
 * */

public class Main {
    public static void main(String[] args) {
        Connection connection = getConnection();
        createTable(connection);


        System.out.println(mainMenu());
        try (BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in))) {
            boolean selectionWasValid = false;
            String userInput;

            while (!selectionWasValid) {
                System.out.print("Your response: ");
                userInput = inputReader.readLine();
                switch (userInput) {
                    case "1" -> {
                        createAnAccount(inputReader, connection);
                        selectionWasValid = true;
                    }
                    case "2" -> {
                        login(inputReader, connection);
                        selectionWasValid = true;
                    }
                    case "0" -> {
                        System.out.println("Have a nice day");
                        selectionWasValid = true;
                    }
                    default -> System.out.println("Make a valid selection");
                }
            }
            connection.close();
        }
        catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }

    private static void createAnAccount(BufferedReader inputReader, Connection connection) throws IOException, SQLException {
        System.out.println("** Creating an account **");
        String name = getName(inputReader);
        System.out.println("--------------");
        String dateOfBirth = getDateOfBirth(inputReader);
        System.out.println("--------------");
        String phoneNumber = getPhoneNumber(inputReader);
        System.out.println("--------------");
        String gender = getGenderIdentity(inputReader);

        if (!numberExistsInDB(phoneNumber, connection)) {
            PreparedStatement addUser = connection.prepareStatement(
                    "INSERT INTO users_table " +
                            "(user_name, date_of_birth, age, phone_number, gender) " +
                            "VALUES('" + name + "', '" + dateOfBirth + "', TIMESTAMPDIFF(YEAR, date_of_birth, CURDATE()),  '" +
                            phoneNumber + "', '" + gender + "');");
            addUser.executeUpdate();

            System.out.println("------------------------------------------");
            System.out.println("Account created Successfully");
            System.out.println("------------------------------------------");
        }
        else {
            System.out.println("You already have an account. Log in instead.");
        }

    }

    private static void login(BufferedReader inputReader, Connection connection) throws IOException, SQLException {
        System.out.println("** Login to an existing account **");
        String userPhoneNumber = getPhoneNumber(inputReader);

        if (numberExistsInDB(userPhoneNumber, connection)) {
            final String ACCOUNT_SID = System.getenv("TWILIO_ACCOUNT_SID");
            final String AUTH_TOKEN = System.getenv("TWILIO_AUTH_TOKEN");
            final String PHONE_NUMBER = System.getenv("TWILIO_PHONE_NUMBER");
            Twilio.init(ACCOUNT_SID, AUTH_TOKEN);

            String code = getVerificationCode();

            Message message = Message.creator(
                    new PhoneNumber(userPhoneNumber),
                    new PhoneNumber(PHONE_NUMBER),
                    "Verification code is: " + code
            ).create();
            System.out.print("Enter the verification code that was sent: ");
            String userCode = inputReader.readLine();

            if (userCode.equals(code)) {
                System.out.println("Account found, Log in successful");
            }
            else {
                System.out.println("Wrong code. Log in failed.");
            }
        }
        else {
            System.out.println("Account not found. Log in failed");
        }
    }

}
