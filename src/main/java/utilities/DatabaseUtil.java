package utilities;

import java.sql.*;

/**
 * Contains all the methods related to database operations
 */
public final class DatabaseUtil {
    public static Connection getConnection() {
        Connection connection; //null will be returned if it could not be connected
        try {
            String driver = System.getenv("SQL_DRIVER");
            String url = System.getenv("SQL_URL");
            String username = System.getenv("SQL_USERNAME");
            String password = System.getenv("SQL_PASSWORD");

            Class.forName(driver);

            connection = DriverManager.getConnection(url, username, password);
            System.out.println("Connection successful");
        }
        catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException(e);
        }
        return connection;
    }

    public static void createTable(Connection connection) {
        try {
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
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean numberExistsInDB(String userPhoneNumber, Connection connection) throws SQLException {
        PreparedStatement getPhoneNumber = connection.prepareStatement("SELECT * FROM users_table WHERE phone_number= '" + userPhoneNumber + "';");
        ResultSet result = getPhoneNumber.executeQuery();
        String numberInDb = "";
        if (result.next()) {
            numberInDb = result.getString("phone_number");
        }
        return numberInDb.equals(userPhoneNumber);
    }
}
