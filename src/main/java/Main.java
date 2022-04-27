import utilities.Menus;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import static utilities.DatabaseUtil.*;

/*
 * Todo:
 *  create account/log in with github
 *  change gender to show all options
 *  move createUsersTable to private class in DatabaseUtil
 *  login by password
 *  name should be in format Firstname
 *  change api keys to store in db
 *  name should not have spaces, clean up user input
 *  make everything regarding logging in/out much better
 *  change all error messages to be err
 * */

/**
 * Main/Driver class. All the exceptions thrown will be caught here
 *
 * @author Ife Sunmola
 */
public class Main {
    public static void main(String[] args) {
        // no need to close connection since the try is used like this
        try (Connection connection = getConnection()) {
            if (connection == null) {
                System.out.println("Connection failed.");
                return;
            }
            createUsersTable(connection);
            Menus.doMainMenu(connection);
        }
        catch (IOException | SQLException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
