package chayes.guzzle.Account;

/**
 * Simplifies the process of adding user info to the firebase database
 */
public class User {
    public String firstName;
    public String lastName;
    public String username;
    public int age;
    public String gender;
    public String country;

    public User(String firstName, String lastName, String username, int age, String gender,
                String country){
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.age = age;
        this.gender = gender;
        this.country = country;
    }
}
