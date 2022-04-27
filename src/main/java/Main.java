import utilities.Menus;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import static utilities.DatabaseUtil.createUsersTable;
import static utilities.DatabaseUtil.getConnection;
import static utilities.ValidateUtil.clearScreen;

/*
 * Todo:
 *  create account/log in with github
 *  change gender to show all options
 *  move createUsersTable to private class in DatabaseUtil
 *  login by password
 *  name should be in format Firstname
 *  change api keys to store in db
 *  name should not have spaces, clean up user input
 * */

/**
 * Main/Driver class. All the exceptions thrown will be caught here
 *
 * @author Ife Sunmola
 */
public class Main {
    public static void main(String[] args) {

        try (Connection connection = getConnection()) {
            clearScreen();
            if (connection == null) {
                System.err.println("Connection failed.");
                return;
            }
            createUsersTable(connection);

            Menus.doMainMenu();
        }
        catch (IOException | SQLException | InterruptedException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }


}
