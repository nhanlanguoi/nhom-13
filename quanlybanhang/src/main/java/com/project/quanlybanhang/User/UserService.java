package com.project.quanlybanhang.User;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

@Service
public class UserService {

    private static final String USER_DATA_RELATIVE_PATH = "static/data-user/users.json";
    private final ObjectMapper mapper;
    private final Path srcUserJsonFilePath;
    private final Path targetUserJsonFilePath;

    public UserService() {
        this.mapper = new ObjectMapper();
        this.mapper.enable(SerializationFeature.INDENT_OUTPUT);
        this.mapper.registerModule(new JavaTimeModule());

        String projectDir = System.getProperty("user.dir");
        this.srcUserJsonFilePath = Paths.get(projectDir, "src", "main", "resources", USER_DATA_RELATIVE_PATH);
        this.targetUserJsonFilePath = Paths.get(projectDir, "target", "classes", USER_DATA_RELATIVE_PATH);
    }

    private void ensureUserDirectoriesExist() throws IOException {
        Path srcParentDir = srcUserJsonFilePath.getParent();
        if (srcParentDir != null && !Files.exists(srcParentDir)) {
            Files.createDirectories(srcParentDir);
            System.out.println("[UserService] Created directory: " + srcParentDir.toAbsolutePath());
        }
        Path targetParentDir = targetUserJsonFilePath.getParent();
        if (targetParentDir != null && !Files.exists(targetParentDir)) {
            Files.createDirectories(targetParentDir);
            System.out.println("[UserService] Created directory: " + targetParentDir.toAbsolutePath());
        }
    }

    public List<User> getAllUsers() throws IOException {
        System.out.println("[UserService] Attempting to load users, prioritizing src.");
        ensureUserDirectoriesExist();
        List<User> users = null;
        boolean loadedFromSrc = false;

        // 1. Ưu tiên đọc từ src/main/resources
        if (Files.exists(srcUserJsonFilePath)) {
            try (InputStream srcIs = Files.newInputStream(srcUserJsonFilePath)) {
                if (srcIs.available() > 0) {
                    users = mapper.readValue(srcIs, new TypeReference<List<User>>() {});
                    System.out.println("[UserService] Successfully loaded " + (users != null ? users.size() : 0) + " users from src: " + srcUserJsonFilePath.toAbsolutePath());
                    loadedFromSrc = true;

                    // Nếu đọc từ src thành công, đồng bộ sang target
                    if (users != null) { // Chỉ đồng bộ nếu thực sự đọc được danh sách
                        try {
                            // Tạo OutputStream cho target và ghi đè
                            try (OutputStream targetOs = Files.newOutputStream(targetUserJsonFilePath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
                                mapper.writeValue(targetOs, users);
                                System.out.println("[UserService] Synced user data from src to target: " + targetUserJsonFilePath.toAbsolutePath());
                            }
                        } catch (IOException e_sync) {
                            System.err.println("[UserService] WARNING: Failed to sync user data from src to target. Error: " + e_sync.getMessage());
                        }
                    }
                } else {
                    System.out.println("[UserService] User data file in src is empty: " + srcUserJsonFilePath.toAbsolutePath());
                    users = new ArrayList<>(); // src rỗng, coi như là danh sách rỗng
                    loadedFromSrc = true; // Đã kiểm tra src
                    // Đồng bộ danh sách rỗng sang target nếu target khác
                    try (OutputStream targetOs = Files.newOutputStream(targetUserJsonFilePath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
                        mapper.writeValue(targetOs, users);
                        System.out.println("[UserService] Synced empty user list from src to target: " + targetUserJsonFilePath.toAbsolutePath());
                    } catch (IOException e_sync) {
                        System.err.println("[UserService] WARNING: Failed to sync empty user list to target. Error: " + e_sync.getMessage());
                    }
                }
            } catch (IOException e_src) {
                System.err.println("[UserService] Error reading user data from src path: " + srcUserJsonFilePath.toAbsolutePath() + ". Error: " + e_src.getMessage());
                // Không ném lỗi, sẽ thử đọc từ target
            }
        } else {
            System.out.println("[UserService] User data file not found in src: " + srcUserJsonFilePath.toAbsolutePath());
        }

        // 2. Nếu không đọc được từ src (hoặc src không có file), thử đọc từ target (classpath) như một fallback
        if (!loadedFromSrc) {
            System.out.println("[UserService] Could not load from src or src was empty. Attempting to load from target/classpath.");
            ClassPathResource classpathResource = new ClassPathResource(USER_DATA_RELATIVE_PATH);
            if (classpathResource.exists()) {
                try (InputStream cpIs = classpathResource.getInputStream()) {
                    if (cpIs.available() > 0) {
                        try (InputStream freshCpIs = new ClassPathResource(USER_DATA_RELATIVE_PATH).getInputStream()) {
                            users = mapper.readValue(freshCpIs, new TypeReference<List<User>>() {});
                            System.out.println("[UserService] Successfully loaded " + (users != null ? users.size() : 0) + " users from classpath (target): " + targetUserJsonFilePath.toAbsolutePath());
                        }
                    } else {
                        System.out.println("[UserService] User data file in classpath (target) is empty: " + targetUserJsonFilePath.toAbsolutePath());
                        users = new ArrayList<>();
                    }
                } catch (IOException e_cp) {
                    System.err.println("[UserService] Error reading user data from classpath (target): " + targetUserJsonFilePath.toAbsolutePath() + ". Error: " + e_cp.getMessage());
                    users = new ArrayList<>();
                }
            } else {
                System.out.println("[UserService] User data file also not found in classpath (target).");
                users = new ArrayList<>();
            }
        }

        // Nếu cả src và target đều không có/rỗng, đảm bảo file rỗng được tạo ở cả 2 nơi cho lần lưu sau
        if (users == null) users = new ArrayList<>(); // Đảm bảo users không null

        if (!Files.exists(srcUserJsonFilePath) && !Files.exists(targetUserJsonFilePath)) {
            System.out.println("[UserService] Neither src nor target user file exists. Initializing empty files.");
            saveUsersToFile(new ArrayList<>(), "initialize empty user file if both missing");
        }


        return users;
    }

    private boolean saveUsersToFile(List<User> users, String operation) throws IOException {
        String actionLog = operation != null ? "(" + operation + ")" : "";
        System.out.println("[UserService] Attempting to save users to file" + actionLog + ".");
        ensureUserDirectoriesExist();

        // 1. Ghi vào src/main/resources
        try (OutputStream outputStream = Files.newOutputStream(srcUserJsonFilePath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            mapper.writeValue(outputStream, users);
            System.out.println("[UserService] User data successfully written to src path: " + srcUserJsonFilePath.toAbsolutePath());
        } catch (IOException e) {
            System.err.println("[UserService] CRITICAL: Failed to write user data to src path: " + srcUserJsonFilePath.toAbsolutePath() + " - " + e.getMessage());
            e.printStackTrace();
            throw e;
        }

        // 2. Ghi vào target/classes
        try (OutputStream outputStream = Files.newOutputStream(targetUserJsonFilePath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            mapper.writeValue(outputStream, users);
            System.out.println("[UserService] User data successfully written to target path: " + targetUserJsonFilePath.toAbsolutePath());
        } catch (IOException e) {
            System.err.println("[UserService] WARNING: Failed to write user data to target path: " + targetUserJsonFilePath.toAbsolutePath() + " - " + e.getMessage());
        }
        return true;
    }

    public User getUserByUsername(String username) throws IOException {
        if (username == null || username.trim().isEmpty()) {
            return null;
        }
        return getAllUsers().stream()
                .filter(user -> username.equals(user.getUsername()))
                .findFirst()
                .orElse(null);
    }

    public boolean updateUser(User updatedUser) throws IOException {
        if (updatedUser == null || updatedUser.getUsername() == null || updatedUser.getUsername().trim().isEmpty()) {
            System.err.println("[UserService] Cannot update user with null or empty username.");
            return false;
        }
        List<User> users = getAllUsers();
        boolean found = false;
        for (int i = 0; i < users.size(); i++) {
            if (updatedUser.getUsername().equals(users.get(i).getUsername())) {
                User existingUser = users.get(i);
                existingUser.setFullname(updatedUser.getFullname());
                existingUser.setEmail(updatedUser.getEmail());
                // Giả sử User class đã có getPhoneNumber và setPhoneNumber
                // Nếu không, bạn cần thêm vào User.java
                // existingUser.setPhoneNumber(updatedUser.getPhoneNumber());
                // existingUser.setAddress(updatedUser.getAddress());
                existingUser.setBan(updatedUser.getBan());
                users.set(i, existingUser);
                found = true;
                break;
            }
        }
        if (found) {
            return saveUsersToFile(users, "updateUser");
        }
        System.out.println("[UserService] User with username: " + updatedUser.getUsername() + " not found for update.");
        return false;
    }

    public boolean deleteUser(String username) throws IOException {
        if (username == null || username.trim().isEmpty()) {
            System.err.println("[UserService] Cannot delete user with null or empty username.");
            return false;
        }
        List<User> users = getAllUsers();
        if ("admin".equalsIgnoreCase(username)) {
            System.err.println("[UserService] Attempt to delete admin account denied.");
            return false;
        }
        boolean removed = users.removeIf(user -> username.equals(user.getUsername()));
        if (removed) {
            System.out.println("[UserService] User '" + username + "' removed from list.");
            return saveUsersToFile(users, "deleteUser");
        }
        System.out.println("[UserService] User with username: " + username + " not found for deletion.");
        return false;
    }

    public boolean addUserByAdmin(User newUser) throws IOException {
        if (newUser == null || newUser.getUsername() == null || newUser.getUsername().trim().isEmpty()) {
            System.err.println("[UserService] Cannot add user with null or empty username.");
            return false;
        }
        List<User> users = getAllUsers();
        if (users.stream().anyMatch(user -> newUser.getUsername().equals(user.getUsername()))) {
            System.err.println("[UserService] Username " + newUser.getUsername() + " already exists. Cannot add by admin.");
            return false;
        }
        users.add(newUser);
        System.out.println("[UserService] User '" + newUser.getUsername() + "' added by admin.");
        return saveUsersToFile(users, "addUserByAdmin");
    }
}