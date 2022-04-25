package utilities;

import java.sql.*;

public class Helpers {


    /**
     * Method to connect to the database. The data needed (driver, url, etc.) is saved in environment variables.
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
        // if users_table does not exist in the database, create it
        PreparedStatement create = connection.prepareStatement(
                """ 
                        CREATE TABLE IF NOT EXISTS users_table(
                        phone_number VARCHAR(10) PRIMARY KEY UNIQUE NOT NULL,
                        user_name VARCHAR(10) NOT NULL,
                        date_of_birth DATE NOT NULL,
                        age INT NOT NULL,
                        gender VARCHAR(10) NOT NULL,
                        date_of_reg Date NOT NULL,
                        time_of_reg TIME NOT NULL,
                        last_login_time DATETIME DEFAULT '2000-11-24 01:01:01'   
                        );""");
        create.executeUpdate();
//    set the last login time to an old date as the default value so the user won't get logged in automatically if
//    they haven't logged in before
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
                //get the user's phone number from db.
                "SELECT phone_number FROM users_table WHERE phone_number= '" + userPhoneNumber + "';");
        ResultSet result = getPhoneNumber.executeQuery();// store the result
        String numberInDb = "";
        if (result.next()) {//.next returns true if there is a data in the ResultSet.
            numberInDb = result.getString("phone_number");// phone_number is the column name
        }
        return numberInDb.equals(userPhoneNumber);
    }
}
