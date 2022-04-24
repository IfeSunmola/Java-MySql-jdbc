package utilities;

import java.sql.*;

/**
 * This class contains all the functions related to MySql operations<br>All the related exceptions will be caught in
 * the Main class
 *
 * @author Ife Sunmola
 */
public final class DatabaseUtil {
    /**
     * Method to connect to the database. The data needed (driver, url, etc.) is saved in environment variables.
     * An error message will print the connection status.
     *
     * @return Connection object a connection was formed OR null if a connection could not be formed.
     * @throws SQLException           if there was a problem with the sql server itself
     * @throws ClassNotFoundException if the jdbc class could not be found
     */
    public static Connection getConnection() throws SQLException, ClassNotFoundException {
        Connection connection; //null will be returned if it could not be connected

        String driver = System.getenv("SQL_DRIVER");
        String url = System.getenv("SQL_URL");
        String username = System.getenv("SQL_USERNAME");
        String password = System.getenv("SQL_PASSWORD");

        Class.forName(driver);

        connection = DriverManager.getConnection(url, username, password);
        if (connection == null) {
            System.out.println("Connection FAILED");
        }
        else {
            System.out.println("Connection SUCCESSFUL");
        }
        return connection;
    }

    /**
     * Method to create a table for the users using the database connection.
     * todo: make this private
     *
     * @param connection the connection to the database
     * @throws SQLException if there was a problem with the sql server itself.
     */
    public static void createUsersTable(Connection connection) throws SQLException {
        // if this table does not exist in the database, create it
        PreparedStatement create = connection.prepareStatement(
                """
                        CREATE TABLE IF NOT EXISTS users_table(
                        user_id INT PRIMARY KEY AUTO_INCREMENT UNIQUE,
                        user_name VARCHAR(20) NOT NULL,
                        date_of_birth DATE NOT NULL,
                        age INT NOT NULL,
                        phone_number VARCHAR(10) UNIQUE,
                        gender VARCHAR(10) NOT NULL
                        );""");
        create.executeUpdate();

    }

    /**
     * Method to check if a phone number exists in the database
     *
     * @param userPhoneNumber the phone number to check
     * @param connection      the connection to the database
     * @return true if the phone number is already in the database OR false if it's not
     * @throws SQLException if there was a problem with the sql server itself
     */
    public static boolean numberExistsInDB(String userPhoneNumber, Connection connection) throws SQLException {
        PreparedStatement getPhoneNumber = connection.prepareStatement(
                //get all the userPhoneNumber. It should only return one number
                "SELECT * FROM users_table WHERE phone_number= '" + userPhoneNumber + "';");
        ResultSet result = getPhoneNumber.executeQuery();// store the result
        String numberInDb = "";
        if (result.next()) {//.next returns true if there is a data in the ResultSet.
            numberInDb = result.getString("phone_number");// phone_number is the column name
        }
        return numberInDb.equals(userPhoneNumber);
    }
}