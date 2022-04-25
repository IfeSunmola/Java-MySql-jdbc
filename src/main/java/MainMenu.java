import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;

public class MainMenu {

    /**
     * @return String containing the main menu options
     */
    private static String showMainMenu() {
        return """
                Select an option, 1 or 2 (enter 0 to quit):
                1. Create an account
                2. Log in to an existing account
                3. Delete an account
                """;
    }

    public static void runMainMenu(Connection connection) throws IOException, SQLException {
        BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in));
        boolean selectionWasValid = false;
        String userInput;

        while (!selectionWasValid) {
            System.out.println(showMainMenu());
            System.out.print("Your response: ");
            userInput = inputReader.readLine().strip();
            switch (userInput) {
                case "1" -> {
                    CreateAnAccount.runCreateAccount(inputReader, connection);
                    selectionWasValid = true;
                }
                case "2" -> {
                    Login.runLogin(inputReader, connection);
                    selectionWasValid = true;
                }
                case "3" -> {
                    DeleteAccount.runDeleteAccount(inputReader, connection);
                    selectionWasValid = true;
                }
                case "0" -> {
                    System.out.println("Have a nice day");
                    selectionWasValid = true;
                }
                default -> {
//                    String[] cmd = {"C:\\WINDOWS\\system32\\cmd.exe", "/c", "start"};
//                    Runtime runtime = Runtime.getRuntime();
//                    Process p = runtime.exec(cmd);
                    System.out.println("Make a valid selection\n");
                }
            }
        }
    }
}
