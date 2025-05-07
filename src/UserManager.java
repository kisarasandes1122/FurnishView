import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class UserManager {
    private static final String USERS_FILE = "./users.dat";
    private static List<User> users;

    static {
        // Initialize with default admin and user accounts if no users exist
        users = loadUsers();
        if (users.isEmpty()) {
            // Add default admin user
            users.add(new User("admin", "password", User.UserType.ADMIN));
            // Add default customer user
            users.add(new User("designer", "password", User.UserType.CUSTOMER));
            saveUsers();
        }
    }

    public static boolean authenticateUser(String username, String password) {
        for (User user : users) {
            if (user.getUsername().equals(username) && user.getPassword().equals(password)) {
                return true;
            }
        }
        return false;
    }

    public static User getUser(String username) {
        for (User user : users) {
            if (user.getUsername().equals(username)) {
                return user;
            }
        }
        return null;
    }

    public static boolean addUser(User user) {
        // Check if username already exists
        if (getUser(user.getUsername()) != null) {
            return false;
        }

        users.add(user);
        return saveUsers();
    }

    public static boolean isAdmin(String username) {
        User user = getUser(username);
        return user != null && user.getUserType() == User.UserType.ADMIN;
    }

    private static List<User> loadUsers() {
        List<User> userList = new ArrayList<>();
        File file = new File(USERS_FILE);

        if (file.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                userList = (List<User>) ois.readObject();
            } catch (Exception e) {
                System.err.println("Error loading users: " + e.getMessage());
            }
        }

        return userList;
    }

    private static boolean saveUsers() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(USERS_FILE))) {
            oos.writeObject(users);
            return true;
        } catch (Exception e) {
            System.err.println("Error saving users: " + e.getMessage());
            return false;
        }
    }
}