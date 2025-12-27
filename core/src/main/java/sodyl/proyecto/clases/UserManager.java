package sodyl.proyecto.clases;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter.OutputType;

import java.util.HashMap;

public class UserManager {
    private static final String USERS_FILE = "users.json";
    private HashMap<String, String> users;
    private Json json;
    private static String currentUser;

    public static void setCurrentUser(String username) {
        currentUser = username;
    }

    public static String getCurrentUser() {
        return currentUser;
    }

    public UserManager() {
        json = new Json();
        json.setOutputType(OutputType.json);
        users = new HashMap<>();
        loadUsers();
    }

    @SuppressWarnings("unchecked")
    private void loadUsers() {
        FileHandle file = Gdx.files.local(USERS_FILE);
        if (file.exists()) {
            try {
                users = json.fromJson(HashMap.class, file);
            } catch (Exception e) {
                Gdx.app.error("UserManager", "Error loading users", e);
                users = new HashMap<>();
            }
        }
    }

    private void saveUsers() {
        FileHandle file = Gdx.files.local(USERS_FILE);
        file.writeString(json.prettyPrint(users), false);
    }

    public String register(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            return "El usuario no puede estar vacío.";
        }
        if (password == null || password.trim().isEmpty()) {
            return "La contraseña no puede estar vacía.";
        }
        if (password.length() < 8) {
            return "La clave debe tener al menos 8 caracteres.";
        }
        if (!password.chars().anyMatch(Character::isUpperCase)) {
            return "La clave debe tener al menos una mayúscula.";
        }
        if (!password.chars().anyMatch(Character::isDigit)) {
            return "La clave debe tener al menos un número.";
        }
        if (users.containsKey(username)) {
            return "El usuario ya existe.";
        }
        users.put(username, password);
        saveUsers();
        return "SUCCESS";
    }

    public boolean login(String username, String password) {
        if (username == null || !users.containsKey(username)) {
            return false;
        }
        String storedPassword = users.get(username);
        return storedPassword.equals(password);
    }
}
