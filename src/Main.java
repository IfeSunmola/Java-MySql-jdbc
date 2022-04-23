import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.*;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

//set classpath=mysql-connector-java-8.0.28.jar;twilio-8.29.1-jar-with-dependencies.jar
//set classpath=mysql-connector-java-8.0.28.jar
/*
 *Todo:
 * add error checking, validations when creating accounts
 * create account/log in with github
 * */

public class Main {
    public static void main(String[] args) {
        Connection connection = getConnection();
        createTable(connection);

        System.out.println(mainMenu());
        try (BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in))) {
            boolean selectionWasValid = false;
            String userInput;

            while (!selectionWasValid) {
                System.out.print("Your response: ");
                userInput = inputReader.readLine();
                switch (userInput) {
                    case "1" -> {
                        createAnAccount(inputReader, connection);
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
        }
        catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }

    private static Connection getConnection() {
        Connection connection; //null will be returned if it could not be connected
        try {
            String driver = "com.mysql.cj.jdbc.Driver";
            String url = "jdbc:mysql://localhost:3306/users_db";
            String username = "root";
            String password = "password";

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
                            phone_number VARCHAR(20) UNIQUE,
                            gender VARCHAR(20) NOT NULL
                            );""");
            create.executeUpdate();
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static String mainMenu() {
        return """
                ----Make Meaningful Conversations----
                Select an option, 1 or 2 (enter 0 to quit):\s
                1. Create an account
                2. Log in to an existing account
                """;
    }

    private static void createAnAccount(BufferedReader inputReader, Connection connection) throws IOException, SQLException {
        System.out.println("**Creating an account**");

        System.out.print("Name: ");
        String name = inputReader.readLine();

        System.out.print("Date of Birth (YYYY-MM-DD): ");
        String dateOfBirth = inputReader.readLine();

        System.out.print("Phone Number(verification code will be sent here): ");
        String phoneNumber = inputReader.readLine();

        System.out.print("Gender that best suits you: ");
        String gender = inputReader.readLine();

        PreparedStatement addUser = connection.prepareStatement(
                "INSERT INTO users_table " +
                        "(user_name, date_of_birth, age, phone_number, gender) " +
                        "VALUES('" + name + "', '" + dateOfBirth + "', TIMESTAMPDIFF(YEAR, date_of_birth, CURDATE()),  '" +
                        phoneNumber + "', '" + gender + "');");
        addUser.executeUpdate();

        System.out.println("------------------------------------------");
        System.out.println("Account created Successfully");
        System.out.println("------------------------------------------");
    }

    private static void login(BufferedReader inputReader, Connection connection) throws IOException, SQLException {
        System.out.println("**Login to an existing account**");

        System.out.print("Enter your phone number: ");
        String userPhoneNumber = inputReader.readLine();

        PreparedStatement getPhoneNumber = connection.prepareStatement("SELECT * FROM users_table WHERE phone_number= '" + userPhoneNumber + "';");

        ResultSet result = getPhoneNumber.executeQuery();
        String numberInDb = "";
        if (result.next()) {
            numberInDb = result.getString("phone_number");
        }

        if (numberInDb.equals(userPhoneNumber)) {
            System.out.println("Log in successful");
        }
        else {
            System.out.println("Account not found");
        }
    }
}
