import com.mysql.cj.log.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static utilities.UserInputUtil.*;
import static utilities.UserInputUtil.getCurrentDate;
import static utilities.Helpers.*;


public class CreateAnAccount {

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
    public static void runCreateAccount(BufferedReader inputReader, Connection connection) throws IOException, SQLException {
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
                Login.runLogin(inputReader, connection);
                System.out.println("------------------------------------------");
            }
            else {// failed
                // shouldn't happen but just in case
                System.err.println("Account could not be created (executeUpdate returned number != 1)");
            }
        }
        else {
            System.out.println("You already have an account. Log in instead.");
            Login.runLogin(inputReader, connection);;
        }
    }

}
