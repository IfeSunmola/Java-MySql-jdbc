package utilities;

import java.io.BufferedReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

/**
 * This class contains all the functions related to getting user input.<br>All the related exceptions will be caught in
 * the Main class
 *
 * @author Ife Sunmola
 */
public final class UserInputUtil {
    private static final int MIN_AGE = 18;// minimum and
    private static final int MAX_AGE = 99;// maximum age a user should be
    private static final String CHAR_LIMIT = "(>= 2 characters and <= 10 characters)";

    // menu options

    /**
     * @return String containing the main menu options
     */
    public static String showMainMenu() {
        return """
                -------------------------------------------
                |Select an option, 1 or 2 (enter 0 to quit)|
                |1. Create an account                      |
                |2. Log in to an existing account          |
                --------------------------------------------
                """;
    }

    /**
     * @return String containing the gender options
     */
    private static String showGenderMenu() {
        return """
                -----------------------
                |How do you identify? |
                |Select an option     |
                |1. Non Binary        |
                |2. Woman             |
                |3. Man               |
                |4. Other (type below)|
                -----------------------
                """;
    }

    public static String showLoginMenu() {
        return """
                ------------------------
                |1. View your profile  |
                |2. Delete your account|
                ------------------------
                """;
    }
    // asking for user input

    /**
     * Method to keep asking for the user's name till a valid name is entered. A name is only valid if its length
     * is > 2 and less than 10 characters.
     *
     * @param inputReader to get the user input
     * @return String containing the user's name
     * @throws IOException if the user input could not be read
     */
    public static String getName(BufferedReader inputReader) throws IOException {
        String name = "";
        while (!isValidNameOrGender(name)) {// keep asking for a name till a valid name is entered
            System.out.print("Name " + CHAR_LIMIT + ": ");
            name = inputReader.readLine().strip();
        }
        return name;
    }

    /**
     * Method to keep asking for the user's date of birth till a valid date of birth is entered. A date is only valid
     * if it's in format YYYY-MM-DD and the age is between 18 and 99 inclusive
     *
     * @param inputReader to get the user input
     * @return String containing the user's date of birth
     * @throws IOException if the user input could not be read
     */
    public static String getDateOfBirth(BufferedReader inputReader) throws IOException {
        String dateOfBirth = "";
        while (!isValidDob(dateOfBirth)) {// keep asking till a valid date of birth is entered
            System.out.print("Date of Birth (YYYY-MM-DD, must be between 18 and 99): ");
            dateOfBirth = inputReader.readLine().strip();
        }
        return dateOfBirth;
    }

    /**
     * Method to keep asking for the user's phone number till a valid number is entered. A number is only valid
     * if it contains only numbers and has 10 characters
     *
     * @param inputReader to get the user input
     * @return String containing the user's phone number
     * @throws IOException if the user input could not be read
     */
    public static String getPhoneNumber(BufferedReader inputReader) throws IOException {
        String phoneNumber = "";
        while (!isValidNumber(phoneNumber)) {// keep asking till a valid number is entered
            System.out.print("Phone Number for verification (10 digits, no separators): ");
            phoneNumber = inputReader.readLine().strip();
        }
        return phoneNumber;
    }

    /**
     * Method to ask for the user's gender identity. <br> 3 options are shown. If the user doesn't identify with one
     * of the three, they can make their own input. todo: show all gender options with "Enter to see more..."
     *
     * @param inputReader to get the user input
     * @return String containing the user's gender identity
     * @throws IOException if the user input could not be read
     */
    public static String getGenderIdentity(BufferedReader inputReader) throws IOException {
        String gender = "";
        String userInput;
        System.out.println(showGenderMenu());//print list of genders
        boolean selectionWasValid = false;// used to end the loop
        while (!selectionWasValid) {// keep looping till a valid selection is made or till the user enters their identity
            System.out.print("Your response: ");
            userInput = inputReader.readLine().strip();
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
                    while (!isValidNameOrGender(gender)) {
                        System.out.print("Gender identity " + CHAR_LIMIT + ": ");
                        gender = inputReader.readLine().strip();
                    }
                    selectionWasValid = true;
                }
                default -> System.out.println("Make a valid selection");
            }
        }
        return gender;
    }

    /**
     * @return the current date and time
     */
    public static String getCurrentDateTime() {
        return ZonedDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd: HH:mm:ss"));
    }

    // validating user input

    /**
     * Method to check if the name or gender passed has length > 2 and < 10
     *
     * @param toCheck Either a name or gender
     * @return True if the parameter's length > 2 and < 10
     */
    private static boolean isValidNameOrGender(String toCheck) {
        return toCheck.length() >= 2 && toCheck.length() <= 10;
    }

    /**
     * Method to check if a date of birth is valid. It first checks if the format is correct. If it is, it checks
     * if the age is between 18 and 99 inclusive
     *
     * @param dateOfBirth the date of birth to check
     * @return true if the date of birth is valid and false if it's not
     */
    private static boolean isValidDob(String dateOfBirth) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        boolean isValidDate = true;
        try {
            // checking if the date format is valid
            LocalDate.parse(dateOfBirth, formatter);
        }
        catch (DateTimeParseException e) {
            isValidDate = false;
        }

        if (isValidDate) {// if the date is valid, check the age
            long age = ChronoUnit.YEARS.between(LocalDate.parse(dateOfBirth, formatter), LocalDate.now());
            isValidDate = age >= MIN_AGE && age <= MAX_AGE;
        }
        return isValidDate;
    }

    /**
     * Method to check if a phone number is valid. It first checks if the phone number has any integers. If it DOES not,
     * it checks the length
     *
     * @param phoneNumber the phone number to check
     * @return true if the phone number is valid and false if it's not
     */
    private static boolean isValidNumber(String phoneNumber) {
        // For some reason, Integer.parseInt doesn't work on some phone number
        boolean isValidNumber = true;
        for (int i = 0; i < phoneNumber.length() && isValidNumber; i++) {
            if (!Character.isDigit(phoneNumber.charAt(i))) {
                isValidNumber = false;
            }
        }
        if (isValidNumber) {// number has only integers, check the length
            if (phoneNumber.length() != 10) {
                isValidNumber = false;
            }
        }
        return isValidNumber;
    }
}
