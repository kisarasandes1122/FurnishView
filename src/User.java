import java.io.Serializable;

public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    private String username;
    private String password;
    private UserType userType;

    public enum UserType {
        ADMIN, CUSTOMER
    }

    public User(String username, String password, UserType userType) {
        this.username = username;
        this.password = password;
        this.userType = userType;
    }

    // Getters
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public UserType getUserType() { return userType; }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        User user = (User) obj;
        return username.equals(user.username);
    }

    @Override
    public int hashCode() {
        return username.hashCode();
    }
}