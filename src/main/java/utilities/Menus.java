package utilities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;

import static utilities.DatabaseUtil.*;
import static utilities.UserInputUtil.showLoginMenu;
import static utilities.UserInputUtil.showMainMenu;

public class Menus {
    public static void doMainMenu(Connection connection) throws IOException, SQLException {
        BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in));
        boolean selectionWasValid = false;
        String userInput;
        System.out.println(showMainMenu());
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
                case "0" -> {
                    System.out.println("Have a nice day");
                    selectionWasValid = true;
                }
                default -> System.out.println("Make a valid selection");
            }
        }
        inputReader.close();
    }

    // menu that shows when the user has been logged in
    public static void doLoginMenu(Connection connection, String userPhoneNumber) throws IOException, SQLException {
        BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in));
        boolean selectionWasValid = false;
        String userInput;
        System.out.println(showLoginMenu());
        while (!selectionWasValid) {
            System.out.print("Your response: ");
            userInput = inputReader.readLine().strip();
            switch (userInput) {
                case "1" -> {
                    showProfile(connection, userPhoneNumber);
                    selectionWasValid = true;
                }
                case "2" -> {
                    deleteAccount(inputReader, connection, userPhoneNumber);
                    selectionWasValid = true;
                }

                case "3" ->{
                    logout(connection);
                    selectionWasValid = true;
                }
                case "0" -> {
                    System.out.println("Have a nice day");
                    selectionWasValid = true;
                }
            }
        }
        inputReader.close();
    }
}
