import java.io.BufferedReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import static utilities.Helpers.*;
import static utilities.UserInputUtil.getPhoneNumber;

public class DeleteAccount {
    /**
     * Method to allow the user to delete their account.
     *
     * @param inputReader to get the user input
     * @param connection  the connection to the database
     * @throws IOException  if the user input could not be read
     * @throws SQLException if there was a problem with the sql server itself
     */
    public static void runDeleteAccount(BufferedReader inputReader, Connection connection) throws IOException, SQLException {
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
                    MainMenu.runMainMenu(connection);
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
