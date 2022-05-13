public class User {
    private final int ID;
    private String name, dateOfBirth, phoneNumber, gender;

    //todo: add id
    public User(String name, String dateOfBirth, String phoneNumber, String gender) {
        this.name = name;
        this.dateOfBirth = dateOfBirth;
        this.phoneNumber = phoneNumber;
        this.gender = gender;
        ID = 2;
    }
}
