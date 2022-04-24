package utilities;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

public final class Util {
    public static Connection getConnection() {
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

    public static String mainMenu() {
        return """
                Select an option, 1 or 2 (enter 0 to quit):
                1. Create an account
                2. Log in to an existing account
                3. Delete an account
                """;
    }

    private static String genderMenu() {
        return """
                How do you identify?
                Select an option
                1. Non binary
                2. Woman
                3. Man
                4. Other (type below)
                """;
    }

    // asking for user input
    public static String getName(BufferedReader inputReader) throws IOException {
        String name = "";
        while (!isValidName(name)) {
            System.out.print("Name (> 2 characters and < 10 characters): ");
            name = inputReader.readLine();
        }
        return name;
    }

    public static String getDateOfBirth(BufferedReader inputReader) throws IOException {
        String dateOfBirth = "";
        while (!isValidDob(dateOfBirth)) {
            System.out.print("Date of Birth (YYYY-MM-DD, must be between 18 and 99): ");
            dateOfBirth = inputReader.readLine();
        }
        return dateOfBirth;
    }

    public static String getPhoneNumber(BufferedReader inputReader) throws IOException {
        String phoneNumber = "";
        while (!isValidNumber(phoneNumber)) {
            System.out.print("Phone Number for verification (10 digits, no separators): ");
            phoneNumber = inputReader.readLine();
        }
        return phoneNumber;
    }

    public static String getGenderIdentity(BufferedReader inputReader) throws IOException {
        String gender = "";
        String userInput;
        System.out.println(genderMenu());
        boolean selectionWasValid = false;
        while (!selectionWasValid) {
            System.out.print("Your response: ");
            userInput = inputReader.readLine();
            switch (userInput) {
                case "1" -> {
                    gender = "Non binary";
                    selectionWasValid = true;
                }
                case "2" -> {
                    gender = "Woman";
                    selectionWasValid = true;
                }
                case "3" -> {
                    gender = "Man";
                    selectionWasValid = true;
                }
                case "4" -> {
                    System.out.print("Gender identity (< 10 characters): ");
                    gender = inputReader.readLine().strip();
                    selectionWasValid = true;
                }
                default -> System.out.println("Make a valid selection");
            }
        }
        return gender;
    }

    // validating user input
    private static boolean isValidName(String name) {
        return name.length() >= 2;
    }

    private static boolean isValidDob(String dateOfBirth) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        // checking if the date format is valid
        boolean isValidDate = true;
        try {
            LocalDate.parse(dateOfBirth, formatter);
        }
        catch (DateTimeParseException e) {
            isValidDate = false;
        }
        if (isValidDate) {
            isValidDate = dateIsInBounds(dateOfBirth);
        }
        return isValidDate;
    }

    private static boolean dateIsInBounds(String dateOfBirth) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        long age = ChronoUnit.YEARS.between(LocalDate.parse(dateOfBirth, formatter), LocalDate.now());
        return age >= 18 && age <= 99;
    }

    private static boolean isValidNumber(String phoneNumber) {
        boolean isValidNumber = true;
        try {
            Integer.parseInt(phoneNumber);
        }
        catch (NumberFormatException e) {
            isValidNumber = false;
        }
        if (isValidNumber) {
            if (phoneNumber.length() != 10) {
                isValidNumber = false;
            }
        }
        return isValidNumber;
    }

    // misc
    public static String getVerificationCode() {
        StringBuilder result = new StringBuilder();
        int min = 0, max = 9;
        for (int i = 0; i < 5; i++) {
            result.append((int) Math.floor(Math.random() * (max - min + 1) + min));
        }
        return result.toString();
    }
}