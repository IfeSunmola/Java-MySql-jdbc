import utilities.Menu;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import static utilities.DatabaseUtil.createUsersTable;
import static utilities.DatabaseUtil.getConnection;

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
 * Main/Driver class for local server
 *
 * @author Ife Sunmola
 */
public class Main {
    public static void main(String[] args) {

        // no need to close inputReader and connection since the try is used like this
        try (Connection connection = getConnection()) {
            if (connection == null) {
                System.err.println("Connection failed.");
                return;
            }
            createUsersTable(connection);
            Menu.doMainMenu(connection);
        }
        catch (IOException | SQLException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }


}
