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
    private static final Connection CONNECTION;

    static {
        try {
            CONNECTION = getConnection();
        }
        catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static void doMainMenu() throws IOException, SQLException {
        BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in));
        boolean selectionWasValid = false;
        String userInput;
        System.out.println(showMainMenu());
        while (!selectionWasValid) {

            System.out.print("Your response: ");
            userInput = inputReader.readLine().strip();
            switch (userInput) {
                case "1" -> {
                    createAccount(inputReader, CONNECTION);
                    selectionWasValid = true;
                }
                case "2" -> {
                    login(inputReader, CONNECTION);
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
    public static void doLoginMenu(String userPhoneNumber) throws IOException, SQLException {
        BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in));
        boolean selectionWasValid = false;
        String userInput;
        System.out.println(showLoginMenu());
        while (!selectionWasValid) {
            System.out.print("Your response: ");
            userInput = inputReader.readLine().strip();
            if (userInput.equals("1")) {
                // view your profile
                showProfile(CONNECTION, userPhoneNumber);
                selectionWasValid = true;
            }
            else if (userInput.equals("2")) {
                deleteAccount(inputReader, CONNECTION, userPhoneNumber);
                selectionWasValid = true;
            }
        }
        inputReader.close();
    }
}
