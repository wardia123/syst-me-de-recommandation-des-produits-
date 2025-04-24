package project;

public class User {
    private int id;
    private String username;
    private String password;
    private int age;

    public User(int id, String username, String password, int age) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.age = age;
    }

    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public int getAge() { return age; }
}
