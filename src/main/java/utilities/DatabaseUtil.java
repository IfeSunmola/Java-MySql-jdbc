package utilities;

import java.io.*;
import java.sql.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;

import static utilities.UserInputUtil.*;
import static utilities.ValidateUtil.sendVerificationCode;

/**
 * This class contains all the functions related to MySql operations<br>All the related exceptions will be caught in
 * the Main class
 *
 * @author Ife Sunmola
 */
public final class DatabaseUtil {
    // for keeping the user logged in
    private static final int MAX_DAYS_FOR_LOGIN = 5;
    private static final String FAKE_COOKIE_FILENAME = "fakeCookies.youWereHacked";
    // for date/time formatting
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    // database info. Using constants in case I need to change any name
    private static final String USERS_TABLE = "users_table";
    private static final String PHONE_NUMBER = "phone_number";
    private static final String USER_NAME = "user_name";
    private static final String DATE_OF_BIRTH = "date_of_birth";
    private static final String Age = "age";
    private static final String GENDER = "gender";
    private static final String DATE_OF_REG = "date_of_reg";
    private static final int NUM_COLUMNS = 7;

    /**
     * Method to connect to the database and create the table needed.
     * The data needed (driver, url, etc.) is saved in environment variables.
     *
     * @return Connection object a connection was formed OR null if a connection could not be formed.
     * @throws SQLException           if there was a problem with the sql server itself
     * @throws ClassNotFoundException if the jdbc class could not be found
     */
    public static Connection setup() throws SQLException, ClassNotFoundException {
        Connection connection; //null will be returned if it could not be connected

        String driver = System.getenv("SQL_DRIVER");
        String url = System.getenv("SQL_URL");
        String username = System.getenv("SQL_USERNAME");
        String password = System.getenv("SQL_PASSWORD");

        Class.forName(driver);
        connection = DriverManager.getConnection(url, username, password);

        if (connection != null) {
            createUsersTable(connection);
        }
        return connection;
    }

    //methods to create tables

    /**
     * Method to create a table for the users using the database connection.
     *
     * @param connection the connection to the database
     * @throws SQLException if there was a problem with the sql server itself.
     */
    private static void createUsersTable(Connection connection) throws SQLException {
        // if users_table does not exist in the database, create it
        PreparedStatement create = connection.prepareStatement(
                "CREATE TABLE IF NOT EXISTS " + USERS_TABLE + "("
                        + PHONE_NUMBER + " VARCHAR(10) PRIMARY KEY UNIQUE NOT NULL,"
                        + USER_NAME + " VARCHAR(10) NOT NULL,"
                        + DATE_OF_BIRTH + " DATE NOT NULL,"
                        + Age + " INT NOT NULL,"
                        + GENDER + " VARCHAR(10) NOT NULL,"
                        + DATE_OF_REG + " DATETIME NOT NULL);");
        create.executeUpdate();
    }

    /**
     * method to create a file that contains the date (and time) the user will need to log in next; so the user
     * won't have to login everytime
     * todo: login directly at the beginning of the program
     *
     * @throws IOException if the file could not be written to
     */
    private static void makeFakeCookies(String phoneNumber) throws IOException {
        FileWriter cookies = new FileWriter(FAKE_COOKIE_FILENAME);//create file
        LocalDateTime dateTime = LocalDateTime.now().plus(Duration.of(MAX_DAYS_FOR_LOGIN, ChronoUnit.DAYS));// add days to it

        cookies.write(phoneNumber + " " + DATE_TIME_FORMATTER.format(dateTime) + "\nWho is your favourite character in " +
                "the last airbender? I like Toph.");
        cookies.close();
    }

    /**
     * method to check if a user needs to log in. A user needs to log in if the current date >= the date in the
     * cookies file. A user also needs to log in if the cookies file was not found
     *
     * @param userPhoneNumber the phone number of the user to login
     * @return true if the user should log in or false if not.
     */
    private static boolean needsToLogin(String userPhoneNumber) {
        boolean needsToLogin = false;
        try (BufferedReader fileReader = new BufferedReader(new FileReader(FAKE_COOKIE_FILENAME))) {
            String cookieDetails = fileReader.readLine();
            String numberInCookie = cookieDetails.substring(0, userPhoneNumber.length());
            String dateInCookie = cookieDetails.substring(userPhoneNumber.length() + 1);

            LocalDateTime dateTimeFromFile = LocalDateTime.parse(dateInCookie, DATE_TIME_FORMATTER);

            long timeElapsed = ChronoUnit.MINUTES.between(LocalDateTime.now(), dateTimeFromFile);
            if (!numberInCookie.equals(userPhoneNumber)) { // there's a cookie, but for another user
                System.out.println("Invalid session. Cookie for another user detected");
                needsToLogin = true;
            }
            else if (timeElapsed <= 0) {
                System.out.println("Session has expired, log in again");
                needsToLogin = true;
            }
        }
        catch (Exception e) {// catching all exceptions because the file can be unpredictable
            needsToLogin = true;
            System.out.println("Fake cookies not found.");
        }
        return needsToLogin;
    }

    /**
     * method to log the user in by verifying them first (if needed)
     * */
    private static void doLogin(BufferedReader inputReader, Connection connection, String userPhoneNumber) throws SQLException, IOException {
        System.out.println("You need to log in");
        String code = sendVerificationCode(userPhoneNumber); //returns the verification code that was sent
        System.out.println(code);
        String userCode = "";
        int attempts = 5;
        while (!code.equals(userCode) && attempts > 0) {
            System.out.print("Verification code that was sent - " + attempts + " attempt(s): ");
            userCode = inputReader.readLine().strip();
            attempts--;
        }

        if (userCode.equals(code)) { // always true
            System.out.println("Account found, Log in successful");

            System.out.println("Do you want to stay logged in for the next " + MAX_DAYS_FOR_LOGIN + " days?: ");
            char userInput = getYorNChoice(inputReader);

            if (userInput == 'Y') {
                makeFakeCookies(userPhoneNumber);
                System.out.println("You will be logged in for " + MAX_DAYS_FOR_LOGIN + " days");
            }
            else {
                System.out.println("You will need to enter your password the next time you log in");
            }
            Menus.doLoginMenu(connection, userPhoneNumber);
        }
        else {
            System.out.println("Wrong code. Log in failed.");
            Menus.doMainMenu(connection);
        }
    }

    private static char getYorNChoice(BufferedReader inputReader) throws IOException {
        char userInput = '\0';
        while (userInput != 'Y' && userInput != 'N') {
            System.out.print("Your response (y/n): ");
            userInput = Character.toUpperCase((char) inputReader.read());
            inputReader.readLine();// remove the newline
        }
        return userInput;
    }

    private static String addQuotes(String string) {
        StringBuilder result = new StringBuilder(string);
        result.insert(0, "'");
        result.insert(result.length(), "'");
        return result.toString();
    }

    /**
     * Method to allow an existing user to log in to their account. A verification code will be sent to the user's
     * phone number with twilio. todo: log in with github, login with password, don't let user keep logging in everytime
     *
     * @param inputReader to get the user input
     * @param connection  the connection to the database
     * @throws IOException  if the user input could not be read
     * @throws SQLException if there was a problem with the sql server itself
     */
    public static void login(BufferedReader inputReader, Connection connection) throws IOException, SQLException {
        System.out.println("** Login to an existing account **");
        String userPhoneNumber = getPhoneNumber(inputReader);// ask for the user's phone number to log in

        if (numberExistsInDB(userPhoneNumber, connection)) { // user has an account
            if (needsToLogin(userPhoneNumber)) { // session has timed out
                doLogin(inputReader, connection, userPhoneNumber);// log the user in
            }
            else {// session has NOT timed out
                System.out.println("Still in session, no need to log in.");
                Menus.doLoginMenu(connection, userPhoneNumber);
            }
        }
        else { // user does not have an account
            System.out.println("Account not found. Log in failed");
            Menus.doMainMenu(connection);
        }
    }

    // methods to create an account

    /**
     * Method to create an account for a user. The account will not be created if an account has already been
     * created with the same phone number.<br> The age will be calculated in the database, rather than directly in the
     * code
     *
     * @param inputReader to get the user input
     * @param connection  the connection to the database
     * @throws IOException  if the user input could not be read
     * @throws SQLException if there was a problem with the sql server itself
     */
    public static void createAccount(BufferedReader inputReader, Connection connection) throws IOException, SQLException {
        System.out.println("** Creating an account **");
        String name = getName(inputReader);
        System.out.println("--------------");
        String dateOfBirth = getDateOfBirth(inputReader);
        System.out.println("--------------");
        String userPhoneNumber = getPhoneNumber(inputReader);
        String gender = getGenderIdentity(inputReader);

        if (!numberExistsInDB(userPhoneNumber, connection)) {// the user does not have an account, create one
            PreparedStatement addUser = connection.prepareStatement(
                    "INSERT INTO " + USERS_TABLE + " (" + PHONE_NUMBER + ", " + USER_NAME + ", " + DATE_OF_BIRTH + ", " + Age + ", " + GENDER + ", " + DATE_OF_REG + ") " +
                            "VALUES(" + addQuotes(userPhoneNumber) + ", " + addQuotes(name) + ", " + addQuotes(dateOfBirth) + ", TIMESTAMPDIFF(YEAR, date_of_birth, CURDATE()), "
                            + addQuotes(gender) + ", " + addQuotes(getCurrentDateTime()) + ");");
            // executeUpdate returns the amount of rows that was updated
            if (addUser.executeUpdate() == 1) {// the account was created if the number of rows updated is 1
                System.out.println("------------------------------------------");
                System.out.println("Account created Successfully");
                doLogin(inputReader, connection, userPhoneNumber);
                System.out.println("------------------------------------------");
            }
            else {// failed
                // shouldn't happen but just in case
                System.out.println("Account could not be created (executeUpdate returned number != 1)");
            }
        }
        else {
            System.out.println("You already have an account. Log in instead.");
            login(inputReader, connection);
        }
    }

    // methods to delete an account

    /**
     * Method to allow the user to delete their account.
     *
     * @param inputReader     used to confirm if the user wants to delete their account or not
     * @param connection      the connection to the database
     * @param userPhoneNumber the phone number of the account to delete
     * @throws IOException  if the user input could not be read
     * @throws SQLException if there was a problem with the sql server itself
     */
    public static void deleteAccount(BufferedReader inputReader, Connection connection, String userPhoneNumber) throws IOException, SQLException {
        System.out.println("** Deleting an account **");
        if (numberExistsInDB(userPhoneNumber, connection)) {// there is an account to delete
            System.out.println("YOUR ACCOUNT CANNOT BE RECOVERED AFTER DELETION");
            System.out.println("Are you sure you want to delete your account? This process is IRREVERSIBLE ");

            char userInput = getYorNChoice(inputReader);

            if (userInput == 'Y') {// delete the account if confirmed
                new File(FAKE_COOKIE_FILENAME).delete();
                PreparedStatement deleteUser = connection.prepareStatement(
                        "DELETE FROM " + USERS_TABLE + " WHERE " + PHONE_NUMBER + " = " + addQuotes(userPhoneNumber) + ";");
                deleteUser.executeUpdate();
                System.out.println("Account deleted successfully");
                Menus.doMainMenu(connection); // go back to main menu in both scenarios
            }
            else {// not confirmed, don't delete the account
                System.out.println("Didn't confirm, account not deleted");
                Menus.doLoginMenu(connection, userPhoneNumber);
            }
        }
        else {// there is no account to delete. Shouldn't work since the user is already logged in but oh well
            System.out.println("Account not found. Delete failed");
            Menus.doMainMenu(connection);
        }
    }

    // method to show the profile
    public static void showProfile(Connection connection, String userPhoneNumber) throws SQLException, IOException {
        HashMap<String, String> result = getUserDetails(connection, userPhoneNumber);

        System.out.println("** Showing profile for " + result.get(USER_NAME) + ": **");
        System.out.println("Phone number: " + result.get(PHONE_NUMBER));
        System.out.println("Date of birth (Age): " + result.get(DATE_OF_BIRTH) + " (" + result.get(Age) + ")");
        System.out.println("Gender: " + result.get(GENDER));
        System.out.println("Date registered: " + formatDateAndTime(result.get(DATE_OF_REG)));
        Menus.doLoginMenu(connection, userPhoneNumber);
    }

    private static HashMap<String, String> getUserDetails(Connection connection, String userPhoneNumber) throws SQLException {
        PreparedStatement getUserDetails = connection.prepareStatement(
                "SELECT * FROM " + USERS_TABLE + " WHERE " + PHONE_NUMBER + " = " + addQuotes(userPhoneNumber) + "");
        ResultSet resultSet = getUserDetails.executeQuery();
        HashMap<String, String> userDetails = new HashMap<>();
        while (resultSet.next()) {
            for (int i = 1; i < NUM_COLUMNS; i++) {
                userDetails.put(resultSet.getMetaData().getColumnName(i), resultSet.getString(i));
            }
        }
        return userDetails;
    }

    public static void logout(Connection connection) throws SQLException, IOException {
        System.out.println("You are now logged out");
        Menus.doMainMenu(connection);
    }

    //misc
    private static String formatDateAndTime(String dateAndTimeOfReg) {
        LocalDateTime date = LocalDateTime.parse(dateAndTimeOfReg, DATE_TIME_FORMATTER);// convert the input to a DateTime
        DateTimeFormatter out = DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' h:mm a"); // convert the DateTime to the needed to be output
        return date.format(out);
    }


    /**
     * Method to check if a phone number exists in the database
     *
     * @param userPhoneNumber the phone number to check
     * @param connection      the connection to the database
     * @return true if the phone number is already in the database OR false if it's not
     * @throws SQLException if there was a problem with the sql server itself
     */
    private static boolean numberExistsInDB(String userPhoneNumber, Connection connection) throws SQLException {
        PreparedStatement getPhoneNumber = connection.prepareStatement(
                //get the user's phone number from db.
                "SELECT " + PHONE_NUMBER + " FROM " + USERS_TABLE + " WHERE " + PHONE_NUMBER + " = " + addQuotes(userPhoneNumber) + ";");
        ResultSet result = getPhoneNumber.executeQuery();// store the result
        String numberInDb = "";
        if (result.next()) {//.next returns true if there is a data in the ResultSet.
            numberInDb = result.getString(PHONE_NUMBER);// phone_number is the column name
        }
        return numberInDb.equals(userPhoneNumber);
    }

}
